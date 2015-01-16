package ch.fork.adhocrailway.android;

import ch.fork.AdHocRailway.manager.LocomotiveManager;
import ch.fork.AdHocRailway.manager.RouteManager;
import ch.fork.AdHocRailway.manager.TurnoutManager;

/**
 * Created with love by fork on 16.01.15.
 */
public class PersistenceContext {
    private final AdHocRailwayApplication adHocRailwayApplication;
    private TurnoutManager turnoutManager;
    private RouteManager routeManager;
    private LocomotiveManager locomotiveManager;

    public PersistenceContext(AdHocRailwayApplication adHocRailwayApplication) {
        this.adHocRailwayApplication = adHocRailwayApplication;
    }

    public TurnoutManager getTurnoutManager() {
        return turnoutManager;
    }

    public void setTurnoutManager(TurnoutManager turnoutManager) {
        this.turnoutManager = turnoutManager;
    }

    public RouteManager getRouteManager() {
        return routeManager;
    }

    public void setRouteManager(RouteManager routeManager) {
        this.routeManager = routeManager;
    }

    public LocomotiveManager getLocomotiveManager() {
        return locomotiveManager;
    }

    public void setLocomotiveManager(LocomotiveManager locomotiveManager) {
        this.locomotiveManager = locomotiveManager;
    }
}
