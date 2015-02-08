package ch.fork.adhocrailway.android.jobs;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.squareup.otto.Produce;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import ch.fork.AdHocRailway.manager.LocomotiveManager;
import ch.fork.AdHocRailway.manager.RouteManager;
import ch.fork.AdHocRailway.manager.TurnoutManager;
import ch.fork.AdHocRailway.manager.impl.LocomotiveManagerImpl;
import ch.fork.AdHocRailway.manager.impl.RouteManagerImpl;
import ch.fork.AdHocRailway.manager.impl.TurnoutManagerImpl;
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
import ch.fork.adhocrailway.android.PersistenceContext;
import ch.fork.adhocrailway.android.R;
import ch.fork.adhocrailway.android.activities.SettingsActivity;
import ch.fork.adhocrailway.android.events.ExceptionEvent;
import ch.fork.adhocrailway.android.events.InfoEvent;

/**
 * Created by fork on 27.05.14.
 */
public class ConnectToPersistenceJob extends NetworkJob implements ServiceListener {
    public final static String TAG = AdHocRailwayApplication.class.getSimpleName();
    private final PersistenceContext persistenceContext;
    private AdHocRailwayApplication adHocRailwayApplication;
    private boolean useDummyServices;

    public ConnectToPersistenceJob(AdHocRailwayApplication adHocRailwayApplication, boolean useDummyServices, PersistenceContext persistenceContext) {
        super();
        this.adHocRailwayApplication = adHocRailwayApplication;
        this.useDummyServices = useDummyServices;
        this.persistenceContext = persistenceContext;
    }


    @Override
    public void onRun() throws Throwable {

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

        LocomotiveManager locomotiveManager = new LocomotiveManagerImpl(xmlLocomotiveService);
        TurnoutManager turnoutManager = new TurnoutManagerImpl(xmlTurnoutService);
        RouteManager routeManager = new RouteManagerImpl(turnoutManager, xmlRouteService);

        setManagersOnContext(locomotiveManager, turnoutManager, routeManager);

        InputStream inputStream = adHocRailwayApplication.getResources().openRawResource(R.raw.weekend_2014);

        xmlServiceHelper.loadFile(xmlLocomotiveService, xmlTurnoutService, xmlRouteService, inputStream);
        try {
            inputStream.close();
        } catch (IOException e) {
            adHocRailwayApplication.postEvent(new ExceptionEvent(adHocRailwayApplication.getString(R.string.error_load_xml), e));
        }
    }

    private void setManagersOnContext(LocomotiveManager locomotiveManager, TurnoutManager turnoutManager, RouteManager routeManager) {
        locomotiveManager.initialize();
        turnoutManager.initialize();
        routeManager.initialize();
        persistenceContext.setTurnoutManager(turnoutManager);
        persistenceContext.setRouteManager(routeManager);
        persistenceContext.setLocomotiveManager(locomotiveManager);

        turnoutManager.addTurnoutManagerListener(adHocRailwayApplication);
        routeManager.addRouteManagerListener(adHocRailwayApplication);
        locomotiveManager.addLocomotiveManagerListener(adHocRailwayApplication);
    }

    private void connectToAdHocServer() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(adHocRailwayApplication);
        String adhocServerHost = sharedPref.getString(SettingsActivity.KEY_ADHOC_SERVER_HOST, AdHocRailwayApplication.SERVER_HOST);
        String url = "http://" + adhocServerHost + ":3000";
        final String uuid = UUID.randomUUID().toString();
        final SIOService sioService = new SIOService(uuid);
        sioService.connect(url, this);
        RestTurnoutService restTurnoutService = new RestTurnoutService(url, sioService, uuid);

        RestRouteService restRouteService = new RestRouteService(url, sioService, uuid);

        RestLocomotiveService restLocomotiveService = new RestLocomotiveService(url, sioService, uuid);


        LocomotiveManager locomotiveManager = new LocomotiveManagerImpl(restLocomotiveService);
        TurnoutManager turnoutManager = new TurnoutManagerImpl(restTurnoutService);
        RouteManager routeManager = new RouteManagerImpl(turnoutManager, restRouteService);

        setManagersOnContext(locomotiveManager, turnoutManager, routeManager);
    }

    @Override
    @Produce
    public void connected() {
        Log.i(TAG, "connected to adhocserver");
        adHocRailwayApplication.postEvent(new InfoEvent(adHocRailwayApplication.getString(R.string.info_disconnected_to_adhocserver)));
    }

    @Override
    @Produce
    public void connectionError(AdHocServiceException ex) {
        Log.e(TAG, "failed to connect to adhocserver", ex);
        adHocRailwayApplication.postEvent(new ExceptionEvent(adHocRailwayApplication.getString(R.string.error_connection_adhocserver), ex));
    }

    @Override
    @Produce
    public void disconnected() {
        Log.i(TAG, "disconnected from adhocserver");
        adHocRailwayApplication.postEvent(new InfoEvent(adHocRailwayApplication.getString(R.string.info_disconnected_from_adhocserver)));
    }

}
