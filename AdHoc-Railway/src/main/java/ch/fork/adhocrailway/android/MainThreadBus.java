package ch.fork.adhocrailway.android;

import android.os.Handler;
import android.os.Looper;

import com.squareup.otto.Bus;

/**
 * Created with love by fork on 27.09.14.
 */
public class MainThreadBus extends Bus {
    private static final MainThreadBus INSTANCE = new MainThreadBus();
    private final Handler mainThread = new Handler(Looper.getMainLooper());

    private MainThreadBus() {

    }

    public static MainThreadBus getMainThreadBus() {
        return INSTANCE;
    }


    @Override
    public void post(final Object event) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            super.post(event);
        } else {
            mainThread.post(new Runnable() {

                @Override
                public void run() {
                    post(event);
                }
            });
        }
    }
}
