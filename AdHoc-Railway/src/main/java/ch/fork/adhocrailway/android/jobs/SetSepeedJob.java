package ch.fork.adhocrailway.android.jobs;

import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

import ch.fork.AdHocRailway.model.locomotives.Locomotive;
import ch.fork.adhocrailway.android.AdHocRailwayApplication;

/**
 * Created by fork on 27.05.14.
 */
public class SetSepeedJob extends Job {
    private final AdHocRailwayApplication adHocRailwayApplication;
    private final Locomotive selectedLocomotive;
    private final int progress;

    public SetSepeedJob(AdHocRailwayApplication adHocRailwayApplication, Locomotive selectedLocomotive, int progress) {
        super(new Params(1).requireNetwork());
        this.adHocRailwayApplication = adHocRailwayApplication;
        this.selectedLocomotive = selectedLocomotive;
        this.progress = progress;
    }

    @Override
    public void onAdded() {

    }

    @Override
    public void onRun() throws Throwable {
        adHocRailwayApplication.getLocomotiveController().setSpeed(selectedLocomotive, progress, selectedLocomotive.getCurrentFunctions());
    }

    @Override
    protected void onCancel() {

    }

    @Override
    protected boolean shouldReRunOnThrowable(Throwable throwable) {
        return false;
    }
}
