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
    private boolean connected;

    public ConnectedToRailwayDeviceEvent(boolean connected) {
        this.connected = connected;
    }

    public boolean isConnected() {
        return connected;
    }

}
