package ch.fork.adhocrailway.android.events;

import ch.fork.AdHocRailway.controllers.LocomotiveController;
import ch.fork.AdHocRailway.controllers.PowerController;
import ch.fork.AdHocRailway.controllers.RouteController;
import ch.fork.AdHocRailway.controllers.TurnoutController;
import ch.fork.AdHocRailway.model.power.PowerSupply;
import de.dermoba.srcp.client.SRCPSession;

/**
 * Created by fork on 4/24/14.
 */
public class ConnectedToRailwayDeviceEvent {
    private final TurnoutController turnoutController;
    private final RouteController routeController;
    private final LocomotiveController locomotiveController;
    private final PowerController powerController;
    private final PowerSupply powerSupply;
    private final SRCPSession session;
    private boolean connected;

    public ConnectedToRailwayDeviceEvent(boolean connected) {

        this.connected = connected;
        turnoutController = null;
        routeController = null;
        powerController = null;
        locomotiveController = null;
        powerSupply = null;
    }

    public ConnectedToRailwayDeviceEvent(boolean connected, TurnoutController turnoutController, RouteController routeController, LocomotiveController locomotiveController, PowerController powerController, PowerSupply powerSupply, SRCPSession session) {
        this.connected = connected;
        this.turnoutController = turnoutController;
        this.routeController = routeController;
        this.locomotiveController = locomotiveController;
        this.powerController = powerController;
        this.powerSupply = powerSupply;
        this.session = session;
    }

    public boolean isConnected() {
        return connected;
    }

    public TurnoutController getTurnoutController() {
        return turnoutController;
    }

    public RouteController getRouteController() {
        return routeController;
    }

    public PowerController getPowerController() {
        return powerController;
    }

    public LocomotiveController getLocomotiveController() {
        return locomotiveController;
    }

    public PowerSupply getPowerSupply() {
        return powerSupply;
    }

    public SRCPSession getSession() {
        return session;
    }
}
