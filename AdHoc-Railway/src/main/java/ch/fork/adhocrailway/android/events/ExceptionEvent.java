package ch.fork.adhocrailway.android.events;

/**
 * Created by fork on 28.04.14.
 */
public class ExceptionEvent {
    private String message;
    private Throwable exception;

    public ExceptionEvent(String message, Throwable e) {
        this.message = message;
        this.exception = e;
    }

    public String getMessage() {
        return message;
    }

    public Throwable getException() {
        return exception;
    }
}
