package ch.fork.adhocrailway.android;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;

import com.squareup.otto.Bus;

import java.io.InputStream;
import java.util.SortedSet;
import java.util.UUID;

import ch.fork.AdHocRailway.controllers.LocomotiveController;
import ch.fork.AdHocRailway.controllers.RouteController;
import ch.fork.AdHocRailway.controllers.TurnoutController;
import ch.fork.AdHocRailway.controllers.impl.dummy.DummyLocomotiveController;
import ch.fork.AdHocRailway.controllers.impl.dummy.DummyRouteController;
import ch.fork.AdHocRailway.controllers.impl.dummy.DummyTurnoutController;
import ch.fork.AdHocRailway.manager.LocomotiveManager;
import ch.fork.AdHocRailway.manager.LocomotiveManagerListener;
import ch.fork.AdHocRailway.manager.RouteManager;
import ch.fork.AdHocRailway.manager.RouteManagerListener;
import ch.fork.AdHocRailway.manager.TurnoutManager;
import ch.fork.AdHocRailway.manager.TurnoutManagerListener;
import ch.fork.AdHocRailway.manager.impl.LocomotiveManagerImpl;
import ch.fork.AdHocRailway.manager.impl.RouteManagerImpl;
import ch.fork.AdHocRailway.manager.impl.TurnoutManagerImpl;
import ch.fork.AdHocRailway.manager.impl.events.LocomotivesUpdatedEvent;
import ch.fork.AdHocRailway.manager.impl.events.RoutesUpdatedEvent;
import ch.fork.AdHocRailway.manager.impl.events.TurnoutsUpdatedEvent;
import ch.fork.AdHocRailway.model.locomotives.Locomotive;
import ch.fork.AdHocRailway.model.locomotives.LocomotiveGroup;
import ch.fork.AdHocRailway.model.turnouts.Route;
import ch.fork.AdHocRailway.model.turnouts.RouteGroup;
import ch.fork.AdHocRailway.model.turnouts.Turnout;
import ch.fork.AdHocRailway.model.turnouts.TurnoutGroup;
import ch.fork.AdHocRailway.persistence.adhocserver.impl.rest.RestLocomotiveService;
import ch.fork.AdHocRailway.persistence.adhocserver.impl.rest.RestRouteService;
import ch.fork.AdHocRailway.persistence.adhocserver.impl.rest.RestTurnoutService;
import ch.fork.AdHocRailway.persistence.adhocserver.impl.socketio.SIOService;
import ch.fork.AdHocRailway.persistence.adhocserver.impl.socketio.ServiceListener;
import ch.fork.AdHocRailway.persistence.xml.XMLServiceHelper;
import ch.fork.AdHocRailway.persistence.xml.impl.XMLLocomotiveService;
import ch.fork.AdHocRailway.persistence.xml.impl.XMLRouteService;
import ch.fork.AdHocRailway.persistence.xml.impl.XMLTurnoutService;
import ch.fork.AdHocRailway.railway.srcp.SRCPLocomotiveControlAdapter;
import ch.fork.AdHocRailway.railway.srcp.SRCPRouteControlAdapter;
import ch.fork.AdHocRailway.railway.srcp.SRCPTurnoutControlAdapter;
import ch.fork.AdHocRailway.services.AdHocServiceException;
import ch.fork.AdHocRailway.services.LocomotiveServiceListener;
import de.dermoba.srcp.client.SRCPSession;
import de.dermoba.srcp.common.exception.SRCPException;
import timber.log.Timber;

/**
 * Created by fork on 4/16/14.
 */
public class AdHocRailwayApplication extends Application implements LocomotiveServiceListener, ServiceListener, TurnoutManagerListener, RouteManagerListener, LocomotiveManagerListener {
    //private static final String SERVER_HOST = "adhocserver";
    public static final String SERVER_HOST = "forkch.dyndns.org";
    private SortedSet<LocomotiveGroup> locomotiveGroups;
    private Locomotive selectedLocomotive;

    private LocomotiveController locomotiveController;
    private TurnoutController turnoutController;
    private RouteController routeController;

    private TurnoutManager turnoutManager;
    private RouteManager routeManager;
    private LocomotiveManager locomotiveManager;

    private Bus bus;
    private Handler handler;
    private SRCPSession session;
    private String adhocServerHost;
    private String srcpServerHost;

    @Override
    public void onCreate() {
        super.onCreate();
        bus = new Bus();
        bus.register(this);
        handler = new Handler();

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        } else {
            Timber.plant(new CrashReportingTree());
        }
    }

    public SortedSet<LocomotiveGroup> getLocomotiveGroups() {
        return locomotiveGroups;
    }

    public void setLocomotiveGroups(SortedSet<LocomotiveGroup> locomotiveGroups) {
        this.locomotiveGroups = locomotiveGroups;
    }

    public Locomotive getSelectedLocomotive() {
        return selectedLocomotive;
    }

    public void setSelectedLocomotive(Locomotive selectedLocomotive) {
        this.selectedLocomotive = selectedLocomotive;
    }

    public void connectToRailwayDevice() {

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean useDummyServices = sharedPref.getBoolean(SettingsActivity.KEY_USE_DUMMY_SERVICES, false);
        if (useDummyServices) {
            connectToDummySrcpService();
        } else {
            connectToSrcpd();
        }
    }

    private void connectToDummySrcpService() {
        turnoutController = new DummyTurnoutController();
        routeController = new DummyRouteController(turnoutController);
        locomotiveController = new DummyLocomotiveController();
        handler.post(new Runnable() {
            @Override
            public void run() {
                bus.post(new ConnectedToRailwayDeviceEvent(true));
            }
        });
    }

    private void connectToSrcpd() {

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        srcpServerHost = sharedPref.getString(SettingsActivity.KEY_SRCP_SERVER_HOST, SERVER_HOST);

        AsyncTask<Void, Void, Void> switchTurnout = new AsyncTask<Void, Void, Void>() {


            @Override
            protected Void doInBackground(Void... params) {
                try {
                    session = new SRCPSession(srcpServerHost, 4303);
                    session.connect();

                    SRCPTurnoutControlAdapter srcpTurnoutControlAdapter = new SRCPTurnoutControlAdapter();
                    SRCPRouteControlAdapter srcpRouteControlAdapter = new SRCPRouteControlAdapter(srcpTurnoutControlAdapter);
                    SRCPLocomotiveControlAdapter srcpLocomotiveControlAdapter = new SRCPLocomotiveControlAdapter();

                    srcpTurnoutControlAdapter.setSession(session);
                    srcpRouteControlAdapter.setSession(session);
                    srcpLocomotiveControlAdapter.setSession(session);

                    locomotiveController = srcpLocomotiveControlAdapter;
                    turnoutController = srcpTurnoutControlAdapter;
                    routeController = srcpRouteControlAdapter;

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            bus.post(new ConnectedToRailwayDeviceEvent(true));
                        }
                    });
                } catch (SRCPException e) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            bus.post(new ConnectedToRailwayDeviceEvent(false));

                        }
                    });
                }
                return null;
            }
        };

        switchTurnout.execute();
    }

    public void connectToPersistence() {
        turnoutManager = new TurnoutManagerImpl();
        turnoutManager.addTurnoutManagerListener(AdHocRailwayApplication.this);
        routeManager = new RouteManagerImpl(turnoutManager);
        routeManager.addRouteManagerListener(AdHocRailwayApplication.this);
        locomotiveManager = new LocomotiveManagerImpl();
        locomotiveManager.addLocomotiveManagerListener(AdHocRailwayApplication.this);


        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean useDummyServices = sharedPref.getBoolean(SettingsActivity.KEY_USE_DUMMY_SERVICES, false);
        if (useDummyServices) {
            loadXml();
        } else {
            connectToAdHocServer();
        }

    }

    private void loadXml() {
        final XMLLocomotiveService xmlLocomotiveService = new XMLLocomotiveService();
        final XMLTurnoutService xmlTurnoutService = new XMLTurnoutService();
        final XMLRouteService xmlRouteService = new XMLRouteService();
        final XMLServiceHelper xmlServiceHelper = new XMLServiceHelper();

        locomotiveManager.setLocomotiveService(xmlLocomotiveService);
        turnoutManager.setTurnoutService(xmlTurnoutService);
        routeManager.setRouteService(xmlRouteService);

        locomotiveManager.initialize();
        turnoutManager.initialize();
        routeManager.initialize();

        AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                InputStream inputStream = getApplicationContext().getResources().openRawResource(R.raw.weekend_2014);
                Log.i("", inputStream.toString());
                xmlServiceHelper.loadFile(xmlLocomotiveService, xmlTurnoutService, xmlRouteService, inputStream);

                return null;
            }
        };
        asyncTask.execute();
    }

    private void connectToAdHocServer() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        adhocServerHost = sharedPref.getString(SettingsActivity.KEY_ADHOC_SERVER_HOST, SERVER_HOST);
        AsyncTask<Void, Void, Void> rest = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                String url = "http://" + adhocServerHost + ":3000";
                RestTurnoutService restTurnoutService = new RestTurnoutService(url, UUID.randomUUID().toString());
                turnoutManager.setTurnoutService(restTurnoutService);
                turnoutManager.initialize();

                RestRouteService restRouteService = new RestRouteService(url, UUID.randomUUID().toString());
                routeManager.setRouteService(restRouteService);
                routeManager.initialize();

                RestLocomotiveService restLocomotiveService = new RestLocomotiveService(url, UUID.randomUUID().toString());
                locomotiveManager.setLocomotiveService(restLocomotiveService);
                locomotiveManager.initialize();

                SIOService.getInstance().connect(url, AdHocRailwayApplication.this);
                return null;
            }
        };

        rest.execute();
    }

    public TurnoutController getTurnoutController() {
        return turnoutController;
    }

    public RouteController getRouteController() {
        return routeController;
    }

    public LocomotiveController getLocomotiveController() {
        return locomotiveController;
    }

    public TurnoutManager getTurnoutManager() {
        return turnoutManager;
    }

    public RouteManager getRouteManager() {
        return routeManager;
    }

    public LocomotiveManager getLocomotiveManager() {
        return locomotiveManager;
    }


    public Bus getBus() {
        return bus;
    }

    public void clearServers() {
        if (turnoutManager != null)
            turnoutManager.disconnect();
        if (routeManager != null)
            routeManager.disconnect();
        if (locomotiveManager != null)
            locomotiveManager.disconnect();

        turnoutController = null;
        routeController = null;
        locomotiveController = null;

        try {
            if (session != null)
                session.disconnect();
        } catch (SRCPException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void locomotiveAdded(Locomotive locomotive) {
        Log.i("", String.valueOf(locomotive));
    }

    @Override
    public void locomotiveUpdated(Locomotive locomotive) {
        Log.i("", String.valueOf(locomotive));
    }

    @Override
    public void locomotiveRemoved(Locomotive locomotive) {

        Log.i("", String.valueOf(locomotive));
    }

    @Override
    public void locomotiveGroupAdded(LocomotiveGroup group) {

        Log.i("", String.valueOf(group));
    }

    @Override
    public void locomotiveGroupUpdated(LocomotiveGroup group) {

        Log.i("", String.valueOf(group));
    }

    @Override
    public void locomotiveGroupRemoved(LocomotiveGroup group) {

        Log.i("", String.valueOf(group));
    }

    @Override
    public void locomotivesUpdated(final SortedSet<LocomotiveGroup> locomotiveGroups) {
        Log.i("", String.valueOf(locomotiveGroups));
        setLocomotiveGroups(locomotiveGroups);
        handler.post(new Runnable() {
            @Override
            public void run() {
                bus.post(new LocomotivesUpdatedEvent(locomotiveGroups));

            }
        });
    }

    @Override
    public void turnoutsUpdated(final SortedSet<TurnoutGroup> turnoutGroups) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                bus.post(new TurnoutsUpdatedEvent(turnoutGroups));

            }
        });
    }

    @Override
    public void turnoutAdded(Turnout turnout) {

    }

    @Override
    public void turnoutUpdated(Turnout turnout) {

    }

    @Override
    public void turnoutRemoved(Turnout turnout) {

    }

    @Override
    public void turnoutGroupAdded(TurnoutGroup group) {

    }

    @Override
    public void turnoutGroupUpdated(TurnoutGroup group) {

    }

    @Override
    public void turnoutGroupRemoved(TurnoutGroup group) {

    }

    @Override
    public void routesUpdated(final SortedSet<RouteGroup> allRouteGroups) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                bus.post(new RoutesUpdatedEvent(allRouteGroups));

            }
        });
    }

    @Override
    public void routeRemoved(Route route) {

    }

    @Override
    public void routeAdded(Route route) {

    }

    @Override
    public void routeUpdated(Route route) {

    }

    @Override
    public void routeGroupAdded(RouteGroup routeGroup) {

    }

    @Override
    public void routeGroupRemoved(RouteGroup routeGroup) {

    }

    @Override
    public void routeGroupUpdated(RouteGroup routeGroup) {

    }

    @Override
    public void failure(AdHocServiceException arg0) {
        Log.e("", "error");
    }

    @Override
    public void connected() {
        Log.i("", "connected");
    }

    @Override
    public void connectionError(AdHocServiceException ex) {

        Log.e("", "error");
    }

    @Override
    public void disconnected() {

        Log.i("", "disconnected");
    }


    /**
     * A tree which logs important information for crash reporting.
     */
    private static class CrashReportingTree extends Timber.HollowTree {
        @Override
        public void i(String message, Object... args) {
            // TODO e.g., Crashlytics.log(String.format(message, args));
        }

        @Override
        public void i(Throwable t, String message, Object... args) {
            i(message, args); // Just add to the log.
        }

        @Override
        public void e(String message, Object... args) {
            i("ERROR: " + message, args); // Just add to the log.
        }

        @Override
        public void e(Throwable t, String message, Object... args) {
            e(message, args);
        }
    }
}
