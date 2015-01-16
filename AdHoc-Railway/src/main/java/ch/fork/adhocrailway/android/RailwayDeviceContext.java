package ch.fork.adhocrailway.android;

import ch.fork.AdHocRailway.controllers.LocomotiveController;
import ch.fork.AdHocRailway.controllers.PowerController;
import ch.fork.AdHocRailway.controllers.RouteController;
import ch.fork.AdHocRailway.controllers.TurnoutController;
import ch.fork.AdHocRailway.model.power.PowerSupply;
import de.dermoba.srcp.client.SRCPSession;

/**
 * Created with love by fork on 16.01.15.
 */
public class RailwayDeviceContext {
    private LocomotiveController locomotiveController;
    private TurnoutController turnoutController;
    private RouteController routeController;
    private PowerController powerController;
    private PowerSupply powerSupply;
    private de.dermoba.srcp.client.SRCPSession SRCPSession;

    public LocomotiveController getLocomotiveController() {
        return locomotiveController;
    }

    public void setLocomotiveController(LocomotiveController locomotiveController) {
        this.locomotiveController = locomotiveController;
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

    public PowerController getPowerController() {
        return powerController;
    }

    public void setPowerController(PowerController powerController) {
        this.powerController = powerController;
    }

    public PowerSupply getPowerSupply() {
        return powerSupply;
    }

    public void setPowerSupply(PowerSupply powerSupply) {
        this.powerSupply = powerSupply;
    }

    public SRCPSession getSRCPSession() {
        return SRCPSession;
    }

    public void setSRCPSession(SRCPSession SRCPSession) {
        this.SRCPSession = SRCPSession;
    }
}
