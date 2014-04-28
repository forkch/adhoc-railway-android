package ch.fork.adhocrailway.android.events;

/**
 * Created by fork on 28.04.14.
 */
public class InfoEvent {
    private String message;

    public InfoEvent(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
