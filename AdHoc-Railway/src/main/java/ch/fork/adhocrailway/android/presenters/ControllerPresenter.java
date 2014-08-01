package ch.fork.adhocrailway.android.presenters;

import ch.fork.AdHocRailway.model.locomotives.Locomotive;
import ch.fork.adhocrailway.android.fragments.ControllerFragment;

/**
 * Created by fork on 08.06.14.
 */
public interface ControllerPresenter {


    void emergencyStop(Locomotive selectedLocomotive);

    void setSpeed(Locomotive selectedLocomotive, int progress);

    void toggleDirection(Locomotive selectedLocomotive);

    void stopLocomotive(Locomotive selectedLocomotive);

    void toggleLocomotiveFunction(Locomotive selectedLocomotive, int functionNumber);
}
