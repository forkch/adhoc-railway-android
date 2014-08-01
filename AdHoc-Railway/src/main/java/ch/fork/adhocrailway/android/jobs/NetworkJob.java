package ch.fork.adhocrailway.android.jobs;

import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

/**
 * Created by fork on 27.05.14.
 */
public abstract class NetworkJob extends Job {
    protected NetworkJob() {
        super(new Params(1).requireNetwork());
    }

    @Override
    public void onAdded() {

    }

    @Override
    protected void onCancel() {

    }

    @Override
    protected boolean shouldReRunOnThrowable(Throwable throwable) {
        return false;
    }
}
