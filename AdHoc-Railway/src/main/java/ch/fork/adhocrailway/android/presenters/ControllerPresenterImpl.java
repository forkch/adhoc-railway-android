package ch.fork.adhocrailway.android.presenters;

import android.content.Context;

import com.path.android.jobqueue.Job;
import com.squareup.otto.Bus;

import javax.inject.Inject;

import ch.fork.AdHocRailway.controllers.LocomotiveController;
import ch.fork.AdHocRailway.model.locomotives.Locomotive;
import ch.fork.adhocrailway.android.AdHocRailwayApplication;
import ch.fork.adhocrailway.android.fragments.ControllerFragment;
import ch.fork.adhocrailway.android.jobs.NetworkJob;

/**
 * Created by fork on 08.06.14.
 */
public class ControllerPresenterImpl implements ControllerPresenter {
    @Inject
    LocomotiveController locomotiveController;
    @Inject
    AdHocRailwayApplication adHocRailwayApplication;
    @Inject
    Bus bus;
    private ControllerFragment fragment;

    public ControllerPresenterImpl(Context context) {
        ((AdHocRailwayApplication) context.getApplicationContext()).inject(this);
        bus.register(this);
    }

    @Override
    public void emergencyStop(final Locomotive selectedLocomotive) {
        if (selectedLocomotive == null) {
            return;
        }

        enqueueJob(new NetworkJob() {
            @Override
            public void onRun() throws Throwable {
                locomotiveController.emergencyStop(selectedLocomotive);
                bus.post(selectedLocomotive);
            }
        });

    }

    @Override
    public void setSpeed(final Locomotive selectedLocomotive, final int progress) {

        if (selectedLocomotive == null) {
            return;
        }
        enqueueJob(new NetworkJob() {
            @Override
            public void onRun() throws Throwable {
                locomotiveController.setSpeed(selectedLocomotive, progress, selectedLocomotive.getCurrentFunctions());
            }
        });
    }

    @Override
    public void toggleDirection(final Locomotive selectedLocomotive) {
        if (selectedLocomotive == null) {
            return;
        }

        enqueueJob(new NetworkJob() {
            @Override
            public void onRun() throws Throwable {
                locomotiveController.toggleDirection(selectedLocomotive);
            }
        });
    }

    @Override
    public void stopLocomotive(final Locomotive selectedLocomotive) {
        if (selectedLocomotive == null) {
            return;
        }
        enqueueJob(new NetworkJob() {
            @Override
            public void onRun() throws Throwable {
                locomotiveController.setSpeed(selectedLocomotive, 0, selectedLocomotive.getCurrentFunctions());
                bus.post(selectedLocomotive);
            }
        });
    }

    @Override
    public void toggleLocomotiveFunction(final Locomotive selectedLocomotive, final int functionNumber) {
        if (selectedLocomotive == null) {
            return;
        }

        enqueueJob(new NetworkJob() {
            @Override
            public void onRun() throws Throwable {
                boolean currentFunctionValue = selectedLocomotive.getCurrentFunctions()[functionNumber];
                locomotiveController.setFunction(selectedLocomotive, functionNumber, !currentFunctionValue, 0);
            }
        });
    }

    private void enqueueJob(Job job) {
        adHocRailwayApplication.getJobManager().addJobInBackground(job);
    }

}
