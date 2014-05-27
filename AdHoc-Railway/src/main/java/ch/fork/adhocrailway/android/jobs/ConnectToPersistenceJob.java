package ch.fork.adhocrailway.android.jobs;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import ch.fork.AdHocRailway.manager.LocomotiveManager;
import ch.fork.AdHocRailway.manager.RouteManager;
import ch.fork.AdHocRailway.manager.TurnoutManager;
import ch.fork.AdHocRailway.persistence.adhocserver.impl.rest.RestLocomotiveService;
import ch.fork.AdHocRailway.persistence.adhocserver.impl.rest.RestRouteService;
import ch.fork.AdHocRailway.persistence.adhocserver.impl.rest.RestTurnoutService;
import ch.fork.AdHocRailway.persistence.adhocserver.impl.socketio.SIOService;
import ch.fork.AdHocRailway.persistence.adhocserver.impl.socketio.ServiceListener;
import ch.fork.AdHocRailway.persistence.xml.XMLServiceHelper;
import ch.fork.AdHocRailway.persistence.xml.impl.XMLLocomotiveService;
import ch.fork.AdHocRailway.persistence.xml.impl.XMLRouteService;
import ch.fork.AdHocRailway.persistence.xml.impl.XMLTurnoutService;
import ch.fork.AdHocRailway.services.AdHocServiceException;
import ch.fork.adhocrailway.android.AdHocRailwayApplication;
import ch.fork.adhocrailway.android.R;
import ch.fork.adhocrailway.android.activities.SettingsActivity;
import ch.fork.adhocrailway.android.events.ExceptionEvent;
import ch.fork.adhocrailway.android.events.InfoEvent;

/**
 * Created by fork on 27.05.14.
 */
public class ConnectToPersistenceJob extends Job implements ServiceListener {
    public final static String TAG = AdHocRailwayApplication.class.getSimpleName();
    private final TurnoutManager turnoutManager;
    private final RouteManager routeManager;
    private final LocomotiveManager locomotiveManager;
    private AdHocRailwayApplication adHocRailwayApplication;
    private boolean useDummyServices;

    public ConnectToPersistenceJob(AdHocRailwayApplication adHocRailwayApplication, boolean useDummyServices, TurnoutManager turnoutManager, RouteManager routeManager, LocomotiveManager locomotiveManager) {
        super(new Params(1).requireNetwork());
        this.adHocRailwayApplication = adHocRailwayApplication;
        this.useDummyServices = useDummyServices;
        this.turnoutManager = turnoutManager;
        this.routeManager = routeManager;
        this.locomotiveManager = locomotiveManager;
    }

    @Override
    public void onAdded() {
    }

    @Override
    public void onRun() throws Throwable {

        if (useDummyServices) {
            loadXml();
        } else {
            connectToAdHocServer();
        }
    }

    @Override
    protected void onCancel() {

    }

    @Override
    protected boolean shouldReRunOnThrowable(Throwable throwable) {
        return false;
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

        InputStream inputStream = adHocRailwayApplication.getResources().openRawResource(R.raw.weekend_2014);

        xmlServiceHelper.loadFile(xmlLocomotiveService, xmlTurnoutService, xmlRouteService, inputStream);
        try {
            inputStream.close();
        } catch (IOException e) {
            adHocRailwayApplication.postEvent(new ExceptionEvent(adHocRailwayApplication.getString(R.string.error_load_xml), e));
        }
    }

    private void connectToAdHocServer() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(adHocRailwayApplication);
        String adhocServerHost = sharedPref.getString(SettingsActivity.KEY_ADHOC_SERVER_HOST, AdHocRailwayApplication.SERVER_HOST);
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

        SIOService.getInstance().connect(url, this);
    }

    @Override
    public void connected() {
        Log.i(TAG, "connected to adhocserver");
        adHocRailwayApplication.postEvent(new InfoEvent(adHocRailwayApplication.getString(R.string.info_disconnected_to_adhocserver)));
    }

    @Override
    public void connectionError(AdHocServiceException ex) {
        Log.e(TAG, "failed to connect to adhocserver", ex);
        adHocRailwayApplication.postEvent(new ExceptionEvent(adHocRailwayApplication.getString(R.string.error_connection_adhocserver), ex));
    }

    @Override
    public void disconnected() {
        Log.i(TAG, "disconnected from adhocserver");
        adHocRailwayApplication.postEvent(new InfoEvent(adHocRailwayApplication.getString(R.string.info_disconnected_from_adhocserver)));
    }

}
