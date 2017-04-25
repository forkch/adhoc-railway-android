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

import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;

import javax.inject.Inject;

import ch.fork.AdHocRailway.controllers.LocomotiveController;
import ch.fork.AdHocRailway.controllers.PowerController;
import ch.fork.AdHocRailway.controllers.RouteController;
import ch.fork.AdHocRailway.controllers.TurnoutController;
import ch.fork.AdHocRailway.manager.LocomotiveManager;
import ch.fork.AdHocRailway.manager.LocomotiveManagerListener;
import ch.fork.AdHocRailway.manager.RouteManager;
import ch.fork.AdHocRailway.manager.RouteManagerListener;
import ch.fork.AdHocRailway.manager.TurnoutManager;
import ch.fork.AdHocRailway.manager.TurnoutManagerListener;
import ch.fork.AdHocRailway.manager.impl.LocomotiveManagerImpl;
import ch.fork.AdHocRailway.manager.impl.RouteManagerImpl;
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
import ch.fork.AdHocRailway.services.AdHocServiceException;
import ch.fork.AdHocRailway.services.LocomotiveServiceListener;
import ch.fork.adhocrailway.android.activities.SettingsActivity;
import ch.fork.adhocrailway.android.events.ConnectedToRailwayDeviceEvent;
import ch.fork.adhocrailway.android.events.ExceptionEvent;
import ch.fork.adhocrailway.android.jobs.ConnectToPersistenceJob;
import ch.fork.adhocrailway.android.jobs.ConnectToRailwayDeviceJob;
import dagger.ObjectGraph;
import de.dermoba.srcp.client.CommandDataListener;
import de.dermoba.srcp.client.InfoDataListener;
import de.dermoba.srcp.client.SRCPSession;
import de.dermoba.srcp.common.exception.SRCPException;
import timber.log.Timber;

/**
 * Created by fork on 4/16/14.
 */
public class AdHocRailwayApplication extends Application implements LocomotiveServiceListener, TurnoutManagerListener, RouteManagerListener, LocomotiveManagerListener, CommandDataListener, InfoDataListener {
    public final static String TAG = AdHocRailwayApplication.class.getSimpleName();
    //private static final String SERVER_HOST = "adhocserver";
    public static final String SERVER_HOST = "forkch.dyndns.org";
    protected ObjectGraph objectGraph;
    @Inject
    Bus bus;
    @Inject
    RailwayDeviceContext railwayDeviceContext;
    @Inject
    PersistenceContext persistenceContext;
    private SortedSet<LocomotiveGroup> locomotiveGroups;
    private Locomotive selectedLocomotive;
    private JobManager jobManager;

    @Override
    public void onCreate() {
        super.onCreate();

        //setupStrictModePolicies();

        setupDagger();

        //ConfigureLog4J.configure();
        bus.register(this);
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

    protected void setupDagger() {
        Object[] modules = getModules().toArray();
        objectGraph = ObjectGraph.create(modules);
        objectGraph.inject(this);
    }

    private void setupStrictModePolicies() {
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectNetwork().detectCustomSlowCalls().detectDiskReads().detectDiskWrites().penaltyLog().penaltyDeath().build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectActivityLeaks()
                .penaltyLog().build());
    }

    protected List<Object> getModules() {
        return Arrays.<Object>asList(
                new AdHocRailwayModule(this)
        );
    }

    public <T> T inject(T obj) {
        return objectGraph.inject(obj);
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

    public void clearServers() {
        if (persistenceContext.getTurnoutManager() != null)
            persistenceContext.getTurnoutManager().disconnect();
        if (persistenceContext.getRouteManager() != null)
            persistenceContext.getRouteManager().disconnect();
        if (persistenceContext.getLocomotiveManager() != null)
            persistenceContext.getLocomotiveManager().disconnect();

        try {
            if (railwayDeviceContext.getSRCPSession() != null)
                railwayDeviceContext.getSRCPSession().disconnect();
        } catch (SRCPException e) {
            e.printStackTrace();
        }
    }

    @Subscribe
    public void connectedEvent(ConnectedToRailwayDeviceEvent event) {

        if(railwayDeviceContext.getSRCPSession() != null) {
            railwayDeviceContext.getSRCPSession().getCommandChannel().addCommandDataListener(this);
            railwayDeviceContext.getSRCPSession().getInfoChannel().addInfoDataListener(this);
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
    }

    public JobManager getJobManager() {
        return jobManager;
    }
    @Override
    public void commandDataReceived(String response) {
        Timber.d("recv: %s", response);
    }

    @Override
    public void commandDataSent(String request) {
        Timber.d("sent: %s", request);
    }

    @Override
    public void infoDataReceived(String infoData) {
        Timber.i("infodata: recv %s", infoData);

    }

    @Override
    public void infoDataSent(String infoData) {
        Timber.i("infodata sent: %s", infoData);

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
