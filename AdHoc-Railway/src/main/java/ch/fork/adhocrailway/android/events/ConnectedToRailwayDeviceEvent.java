package ch.fork.adhocrailway.android.events;

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
