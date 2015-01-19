package ch.fork.adhocrailway.android.jobs;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import ch.fork.AdHocRailway.controllers.LocomotiveController;
import ch.fork.AdHocRailway.controllers.PowerController;
import ch.fork.AdHocRailway.controllers.RouteController;
import ch.fork.AdHocRailway.controllers.TaskExecutor;
import ch.fork.AdHocRailway.controllers.TurnoutController;
import ch.fork.AdHocRailway.controllers.impl.dummy.DummyLocomotiveController;
import ch.fork.AdHocRailway.controllers.impl.dummy.DummyPowerController;
import ch.fork.AdHocRailway.controllers.impl.dummy.DummyRouteController;
import ch.fork.AdHocRailway.controllers.impl.dummy.DummyTurnoutController;
import ch.fork.AdHocRailway.model.power.PowerSupply;
import ch.fork.AdHocRailway.railway.srcp.SRCPLocomotiveControlAdapter;
import ch.fork.AdHocRailway.railway.srcp.SRCPPowerControlAdapter;
import ch.fork.AdHocRailway.railway.srcp.SRCPRouteControlAdapter;
import ch.fork.AdHocRailway.railway.srcp.SRCPTurnoutControlAdapter;
import ch.fork.adhocrailway.android.AdHocRailwayApplication;
import ch.fork.adhocrailway.android.PersistenceContext;
import ch.fork.adhocrailway.android.R;
import ch.fork.adhocrailway.android.RailwayDeviceContext;
import ch.fork.adhocrailway.android.activities.SettingsActivity;
import ch.fork.adhocrailway.android.events.ConnectedToRailwayDeviceEvent;
import ch.fork.adhocrailway.android.events.ExceptionEvent;
import de.dermoba.srcp.client.SRCPSession;
import de.dermoba.srcp.common.exception.SRCPException;

/**
 * Created by fork on 27.05.14.
 */
public class ConnectToRailwayDeviceJob extends NetworkJob {
    private final AdHocRailwayApplication adHocRailwayApplication;
    private final boolean useDummyServices;
    private final PersistenceContext persistenceContext;
    private RailwayDeviceContext railwayDeviceContext;

    public ConnectToRailwayDeviceJob(AdHocRailwayApplication adHocRailwayApplication, boolean useDummyServices, RailwayDeviceContext railwayDeviceContext, PersistenceContext persistenceContext) {
        super();
        this.adHocRailwayApplication = adHocRailwayApplication;
        this.useDummyServices = useDummyServices;
        this.railwayDeviceContext = railwayDeviceContext;
        this.persistenceContext = persistenceContext;
    }

    @Override
    public void onRun() throws Throwable {
        if (useDummyServices) {
            connectToDummySrcpService();
        } else {
            connectToSrcpd();
        }
    }

    private void connectToDummySrcpService() {
        TurnoutController turnoutController = new DummyTurnoutController();
        RouteController routeController = new DummyRouteController(turnoutController, persistenceContext.getTurnoutManager());
        LocomotiveController locomotiveController = new DummyLocomotiveController();
        PowerController powerController = new DummyPowerController();

        adHocRailwayApplication.postEvent(new ConnectedToRailwayDeviceEvent(true));
    }

    private void connectToSrcpd() {

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(adHocRailwayApplication);
        String srcpServerHost = sharedPref.getString(SettingsActivity.KEY_SRCP_SERVER_HOST, AdHocRailwayApplication.SERVER_HOST);

        try {
            SRCPSession session = new SRCPSession(srcpServerHost, 4303);
            session.connect();

            final TaskExecutor taskExecutor = new TaskExecutor();

            SRCPTurnoutControlAdapter srcpTurnoutControlAdapter = new SRCPTurnoutControlAdapter(taskExecutor);
            SRCPRouteControlAdapter srcpRouteControlAdapter = new SRCPRouteControlAdapter(srcpTurnoutControlAdapter, persistenceContext.getTurnoutManager());
            SRCPLocomotiveControlAdapter srcpLocomotiveControlAdapter = new SRCPLocomotiveControlAdapter(taskExecutor);
            SRCPPowerControlAdapter srcpPowerControlAdapter = new SRCPPowerControlAdapter();

            srcpTurnoutControlAdapter.setSession(session);
            srcpRouteControlAdapter.setSession(session);
            srcpLocomotiveControlAdapter.setSession(session);
            srcpPowerControlAdapter.setSession(session);

            PowerSupply powerSupply = new PowerSupply(1);
            srcpPowerControlAdapter.addOrUpdatePowerSupply(powerSupply);


            srcpRouteControlAdapter.setRoutingDelay(500);

            LocomotiveController locomotiveController = srcpLocomotiveControlAdapter;
            TurnoutController turnoutController = srcpTurnoutControlAdapter;
            RouteController routeController = srcpRouteControlAdapter;
            PowerController powerController = srcpPowerControlAdapter;


            railwayDeviceContext.setLocomotiveController(locomotiveController);
            railwayDeviceContext.setTurnoutController(turnoutController);
            railwayDeviceContext.setRouteController(routeController);
            railwayDeviceContext.setPowerController(powerController);
            railwayDeviceContext.setPowerSupply(powerSupply);
            railwayDeviceContext.setSRCPSession(session);

            adHocRailwayApplication.postEvent(new ConnectedToRailwayDeviceEvent(true));
        } catch (SRCPException e) {
            adHocRailwayApplication.postEvent(new ExceptionEvent(adHocRailwayApplication.getString(R.string.error_failed_to_connect_srcpserver), e));
            adHocRailwayApplication.postEvent(new ConnectedToRailwayDeviceEvent(false));
        }

    }
}
