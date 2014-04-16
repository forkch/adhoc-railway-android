package ch.fork.adhocrailway.android;

import android.app.Application;

import java.util.SortedSet;

import ch.fork.AdHocRailway.model.locomotives.Locomotive;
import ch.fork.AdHocRailway.model.locomotives.LocomotiveGroup;

/**
 * Created by fork on 4/16/14.
 */
public class AdHocRailwayApplication extends Application {


    private SortedSet<LocomotiveGroup> locomotiveGroups;
    private Locomotive selectedLocomotive;

    public void setLocomotiveGroups(SortedSet<LocomotiveGroup> locomotiveGroups) {
        this.locomotiveGroups = locomotiveGroups;
    }

    public SortedSet<LocomotiveGroup> getLocomotiveGroups() {
        return locomotiveGroups;
    }

    public void setSelectedLocomotive(Locomotive selectedLocomotive) {
        this.selectedLocomotive = selectedLocomotive;
    }

    public Locomotive getSelectedLocomotive() {
        return selectedLocomotive;
    }
}
