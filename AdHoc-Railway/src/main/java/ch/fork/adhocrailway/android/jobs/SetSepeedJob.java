package ch.fork.adhocrailway.android.jobs;

import ch.fork.AdHocRailway.model.locomotives.Locomotive;
import ch.fork.adhocrailway.android.AdHocRailwayApplication;

/**
 * Created by fork on 27.05.14.
 */
public class SetSepeedJob extends NetworkJob {
    private final AdHocRailwayApplication adHocRailwayApplication;
    private final Locomotive selectedLocomotive;
    private final int progress;

    public SetSepeedJob(AdHocRailwayApplication adHocRailwayApplication, Locomotive selectedLocomotive, int progress) {
        super();
        this.adHocRailwayApplication = adHocRailwayApplication;
        this.selectedLocomotive = selectedLocomotive;
        this.progress = progress;
    }
    @Override
    public void onRun() throws Throwable {
        adHocRailwayApplication.getLocomotiveController().setSpeed(selectedLocomotive, progress, selectedLocomotive.getCurrentFunctions());
    }


}
