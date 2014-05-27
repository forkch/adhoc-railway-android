package ch.fork.adhocrailway.android.jobs;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

import ch.fork.AdHocRailway.controllers.LocomotiveController;
import ch.fork.AdHocRailway.controllers.PowerController;
import ch.fork.AdHocRailway.controllers.RouteController;
import ch.fork.AdHocRailway.controllers.TurnoutController;
import ch.fork.AdHocRailway.controllers.impl.dummy.DummyLocomotiveController;
import ch.fork.AdHocRailway.controllers.impl.dummy.DummyPowerController;
import ch.fork.AdHocRailway.controllers.impl.dummy.DummyRouteController;
import ch.fork.AdHocRailway.controllers.impl.dummy.DummyTurnoutController;
import ch.fork.AdHocRailway.manager.TurnoutManager;
import ch.fork.AdHocRailway.model.power.PowerSupply;
import ch.fork.AdHocRailway.railway.srcp.SRCPLocomotiveControlAdapter;
import ch.fork.AdHocRailway.railway.srcp.SRCPPowerControlAdapter;
import ch.fork.AdHocRailway.railway.srcp.SRCPRouteControlAdapter;
import ch.fork.AdHocRailway.railway.srcp.SRCPTurnoutControlAdapter;
import ch.fork.adhocrailway.android.AdHocRailwayApplication;
import ch.fork.adhocrailway.android.R;
import ch.fork.adhocrailway.android.activities.SettingsActivity;
import ch.fork.adhocrailway.android.events.ConnectedToRailwayDeviceEvent;
import ch.fork.adhocrailway.android.events.ExceptionEvent;
import de.dermoba.srcp.client.SRCPSession;
import de.dermoba.srcp.common.exception.SRCPException;

/**
 * Created by fork on 27.05.14.
 */
public class ConnectToRailwayDeviceJob extends Job {
    private final AdHocRailwayApplication adHocRailwayApplication;
    private final boolean useDummyServices;
    private TurnoutController turnoutController;
    private RouteController routeController;
    private LocomotiveController locomotiveController;
    private PowerController powerController;
    private TurnoutManager turnoutManager;
    private PowerSupply powerSupply;

    public ConnectToRailwayDeviceJob(AdHocRailwayApplication adHocRailwayApplication, boolean useDummyServices, TurnoutManager turnoutManager) {
        super(new Params(1).requireNetwork());
        this.adHocRailwayApplication = adHocRailwayApplication;
        this.useDummyServices = useDummyServices;
        this.turnoutManager = turnoutManager;
    }

    @Override
    public void onAdded() {

    }

    @Override
    public void onRun() throws Throwable {
        if (useDummyServices) {
            connectToDummySrcpService();
        } else {
            connectToSrcpd();
        }
    }

    @Override
    protected void onCancel() {

    }

    @Override
    protected boolean shouldReRunOnThrowable(Throwable throwable) {
        return false;
    }

    private void connectToDummySrcpService() {
        turnoutController = new DummyTurnoutController();
        routeController = new DummyRouteController(turnoutController, turnoutManager);
        locomotiveController = new DummyLocomotiveController();
        powerController = new DummyPowerController();

        adHocRailwayApplication.postEvent(new ConnectedToRailwayDeviceEvent(true, turnoutController, routeController, locomotiveController, powerController, new PowerSupply(1)));
    }

    private void connectToSrcpd() {

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(adHocRailwayApplication);
        String srcpServerHost = sharedPref.getString(SettingsActivity.KEY_SRCP_SERVER_HOST, AdHocRailwayApplication.SERVER_HOST);

        try {
            SRCPSession session = new SRCPSession(srcpServerHost, 4303);
            session.connect();

            SRCPTurnoutControlAdapter srcpTurnoutControlAdapter = new SRCPTurnoutControlAdapter();
            SRCPRouteControlAdapter srcpRouteControlAdapter = new SRCPRouteControlAdapter(srcpTurnoutControlAdapter, turnoutManager);
            SRCPLocomotiveControlAdapter srcpLocomotiveControlAdapter = new SRCPLocomotiveControlAdapter();
            SRCPPowerControlAdapter srcpPowerControlAdapter = new SRCPPowerControlAdapter();

            srcpTurnoutControlAdapter.setSession(session);
            srcpRouteControlAdapter.setSession(session);
            srcpLocomotiveControlAdapter.setSession(session);
            srcpPowerControlAdapter.setSession(session);

            powerSupply = new PowerSupply(1);
            srcpPowerControlAdapter.addOrUpdatePowerSupply(powerSupply);


            srcpRouteControlAdapter.setRoutingDelay(500);

            locomotiveController = srcpLocomotiveControlAdapter;
            turnoutController = srcpTurnoutControlAdapter;
            routeController = srcpRouteControlAdapter;
            powerController = srcpPowerControlAdapter;

            adHocRailwayApplication.postEvent(new ConnectedToRailwayDeviceEvent(true, turnoutController, routeController, locomotiveController, powerController, powerSupply));
        } catch (SRCPException e) {
            adHocRailwayApplication.postEvent(new ExceptionEvent(adHocRailwayApplication.getString(R.string.error_failed_to_connect_srcpserver), e));
            adHocRailwayApplication.postEvent(new ConnectedToRailwayDeviceEvent(false));
        }

    }
}
