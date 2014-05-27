package ch.fork.adhocrailway.android;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.util.Log;

import com.path.android.jobqueue.JobManager;
import com.path.android.jobqueue.config.Configuration;
import com.path.android.jobqueue.log.CustomLogger;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.squareup.otto.ThreadEnforcer;

import java.io.IOException;
import java.io.InputStream;
import java.util.SortedSet;
import java.util.UUID;

import ch.fork.AdHocRailway.controllers.LocomotiveController;
import ch.fork.AdHocRailway.controllers.PowerController;
import ch.fork.AdHocRailway.controllers.RouteController;
import ch.fork.AdHocRailway.controllers.TurnoutController;
import ch.fork.AdHocRailway.controllers.impl.dummy.DummyLocomotiveController;
import ch.fork.AdHocRailway.controllers.impl.dummy.DummyPowerController;
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
import ch.fork.AdHocRailway.model.power.PowerSupply;
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
import ch.fork.AdHocRailway.railway.srcp.SRCPPowerControlAdapter;
import ch.fork.AdHocRailway.railway.srcp.SRCPRouteControlAdapter;
import ch.fork.AdHocRailway.railway.srcp.SRCPTurnoutControlAdapter;
import ch.fork.AdHocRailway.services.AdHocServiceException;
import ch.fork.AdHocRailway.services.LocomotiveServiceListener;
import ch.fork.adhocrailway.android.activities.SettingsActivity;
import ch.fork.adhocrailway.android.events.ConnectedToRailwayDeviceEvent;
import ch.fork.adhocrailway.android.events.ExceptionEvent;
import ch.fork.adhocrailway.android.events.InfoEvent;
import ch.fork.adhocrailway.android.jobs.ConnectToPersistenceJob;
import ch.fork.adhocrailway.android.jobs.ConnectToRailwayDeviceJob;
import ch.fork.adhocrailway.android.jobs.SetSepeedJob;
import de.dermoba.srcp.client.SRCPSession;
import de.dermoba.srcp.common.exception.SRCPException;
import timber.log.Timber;

/**
 * Created by fork on 4/16/14.
 */
public class AdHocRailwayApplication extends Application implements LocomotiveServiceListener, TurnoutManagerListener, RouteManagerListener, LocomotiveManagerListener {
    public final static String TAG = AdHocRailwayApplication.class.getSimpleName();
    //private static final String SERVER_HOST = "adhocserver";
    public static final String SERVER_HOST = "forkch.dyndns.org";
    private SortedSet<LocomotiveGroup> locomotiveGroups;
    private Locomotive selectedLocomotive;

    private LocomotiveController locomotiveController;
    private TurnoutController turnoutController;
    private RouteController routeController;
    private PowerController powerController;

    private TurnoutManager turnoutManager;
    private RouteManager routeManager;
    private LocomotiveManager locomotiveManager;

    private Bus bus;
    private Handler handler;
    private SRCPSession session;
    private String srcpServerHost;
    private PowerSupply powerSupply;
    private JobManager jobManager;

    @Override
    public void onCreate() {
        super.onCreate();


        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectNetwork().detectCustomSlowCalls().detectDiskReads().detectDiskWrites().penaltyLog().penaltyDeath().build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectActivityLeaks()
                .penaltyLog().build());
        //ConfigureLog4J.configure();
        bus = new Bus(ThreadEnforcer.ANY);
        bus.register(this);
        handler = new Handler();

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        } else {
            Timber.plant(new CrashReportingTree());
        }
        AsyncTask<Void, Void, Void> startJobManager = new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                configureJobManager();
                return null;
            }
        };
        startJobManager.execute();
    }


    private void configureJobManager() {
        Configuration configuration = new Configuration.Builder(this)
                .customLogger(new CustomLogger() {
                    private static final String TAG = "JOBS";

                    @Override
                    public boolean isDebugEnabled() {
                        return true;
                    }

                    @Override
                    public void d(String text, Object... args) {
                        Log.d(TAG, String.format(text, args));
                    }

                    @Override
                    public void e(Throwable t, String text, Object... args) {
                        Log.e(TAG, String.format(text, args), t);
                    }

                    @Override
                    public void e(String text, Object... args) {
                        Log.e(TAG, String.format(text, args));
                    }
                })
                .minConsumerCount(1)//always keep at least one consumer alive
                .maxConsumerCount(3)//up to 3 consumers at a time
                .loadFactor(3)//3 jobs per consumer
                .consumerKeepAlive(120)//wait 2 minute
                .build();

        jobManager = new JobManager(this, configuration);
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

        ConnectToRailwayDeviceJob connectToRailwayDeviceJob = new ConnectToRailwayDeviceJob(this, useDummyServices, turnoutManager);
        jobManager.addJobInBackground(connectToRailwayDeviceJob);
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

        ConnectToPersistenceJob connectToPersistenceJob = new ConnectToPersistenceJob(this, useDummyServices, turnoutManager, routeManager, locomotiveManager);

        jobManager.addJobInBackground(connectToPersistenceJob);
    }

    public TurnoutController getTurnoutController() {
        return turnoutController;
    }

    public void setTurnoutController(TurnoutController turnoutController) {
        this.turnoutController = turnoutController;
    }

    public RouteController getRouteController() {
        return routeController;
    }

    public void setRouteController(RouteController routeController) {
        this.routeController = routeController;
    }

    public LocomotiveController getLocomotiveController() {
        return locomotiveController;
    }

    public void setLocomotiveController(LocomotiveController locomotiveController) {
        this.locomotiveController = locomotiveController;
    }

    public PowerController getPowerController() {
        return powerController;
    }

    public void setPowerController(PowerController powerController) {
        this.powerController = powerController;
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

    public PowerSupply getPowerSupply() {
        return powerSupply;
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

    @Subscribe
    public void connectedEvent(ConnectedToRailwayDeviceEvent event) {
        if (event.isConnected()) {
            locomotiveController = event.getLocomotiveController();
            turnoutController = event.getTurnoutController();
            routeController = event.getRouteController();
            powerController = event.getPowerController();
            powerSupply = event.getPowerSupply();
        }
    }

    @Override
    public void locomotivesUpdated(final SortedSet<LocomotiveGroup> locomotiveGroups) {
        Log.i(TAG, "locomotives updated");
        setLocomotiveGroups(locomotiveGroups);
        postEvent(new LocomotivesUpdatedEvent(locomotiveGroups));
    }

    @Override
    public void turnoutsUpdated(final SortedSet<TurnoutGroup> turnoutGroups) {
        Log.i(TAG, "turnouts updated");
        postEvent(new TurnoutsUpdatedEvent(turnoutGroups));
    }

    @Override
    public void routesUpdated(final SortedSet<RouteGroup> allRouteGroups) {
        Log.i(TAG, "routes updated");
        postEvent(new RoutesUpdatedEvent(allRouteGroups));
    }

    @Override
    public void locomotiveAdded(Locomotive locomotive) {
        Log.i(TAG, "locomotive added: " + String.valueOf(locomotive));
    }

    @Override
    public void locomotiveUpdated(Locomotive locomotive) {
        Log.i(TAG, "locomotive updated: " + String.valueOf(locomotive));
    }

    @Override
    public void locomotiveRemoved(Locomotive locomotive) {
        Log.i(TAG, "locomotive removed: " + String.valueOf(locomotive));
    }

    @Override
    public void locomotiveGroupAdded(LocomotiveGroup group) {
        Log.i(TAG, "locomotive group added: " + group);
    }

    @Override
    public void locomotiveGroupUpdated(LocomotiveGroup group) {
        Log.i(TAG, "locomotive group updated: " + group);
    }

    @Override
    public void locomotiveGroupRemoved(LocomotiveGroup group) {
        Log.i(TAG, "locomotive group removed: " + group);
    }


    @Override
    public void turnoutAdded(Turnout turnout) {
        Log.i(TAG, "turnout added: " + turnout);
    }

    @Override
    public void turnoutUpdated(Turnout turnout) {
        Log.i(TAG, "turnout updated: " + turnout);
    }

    @Override
    public void turnoutRemoved(Turnout turnout) {
        Log.i(TAG, "turnout removed: " + turnout);
    }

    @Override
    public void turnoutGroupAdded(TurnoutGroup group) {
        Log.i(TAG, "turnout group added: " + group);
    }

    @Override
    public void turnoutGroupUpdated(TurnoutGroup group) {
        Log.i(TAG, "turnout group updated: " + group);
    }

    @Override
    public void turnoutGroupRemoved(TurnoutGroup group) {
        Log.i(TAG, "turnout group removed: " + group);
    }

    @Override
    public void routeAdded(Route route) {
        Log.i(TAG, "route added: " + route);
    }

    @Override
    public void routeUpdated(Route route) {
        Log.i(TAG, "route updated: " + route);
    }

    @Override
    public void routeRemoved(Route route) {
        Log.i(TAG, "route removed: " + route);
    }


    @Override
    public void routeGroupAdded(RouteGroup routeGroup) {
        Log.i(TAG, "route group added: " + routeGroup);

    }

    @Override
    public void routeGroupRemoved(RouteGroup routeGroup) {
        Log.i(TAG, "route group removed: " + routeGroup);
    }

    @Override
    public void routeGroupUpdated(RouteGroup routeGroup) {
        Log.i(TAG, "route group updated: " + routeGroup);
    }

    @Override
    public void failure(AdHocServiceException arg0) {
        Log.e(TAG, "failure in communication with adhocserver", arg0);
        postEvent(new ExceptionEvent(getString(R.string.error_communication_adhocserver), arg0));
    }


    public void postEvent(final Object event) {
        bus.post(event);
        handler.post(new Runnable() {
            @Override
            public void run() {
                bus.post(event);
            }
        });
    }

    public JobManager getJobManager(SetSepeedJob setSepeedJob) {
        return jobManager;
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
