package ch.fork.adhocrailway.android;

import android.content.Context;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

import javax.inject.Singleton;

import ch.fork.AdHocRailway.manager.LocomotiveManager;
import ch.fork.AdHocRailway.manager.RouteManager;
import ch.fork.AdHocRailway.manager.TurnoutManager;
import ch.fork.AdHocRailway.manager.impl.LocomotiveManagerImpl;
import ch.fork.AdHocRailway.manager.impl.RouteManagerImpl;
import ch.fork.AdHocRailway.manager.impl.TurnoutManagerImpl;
import ch.fork.adhocrailway.android.activities.ConnectActivity;
import ch.fork.adhocrailway.android.activities.ControllerActivity;
import ch.fork.adhocrailway.android.activities.LocomotiveSelectActivity;
import ch.fork.adhocrailway.android.activities.SettingsActivity;
import ch.fork.adhocrailway.android.fragments.MainControllerFragment;
import dagger.Module;
import dagger.Provides;

/**
 * Created by fork on 30.05.14.
 */

@Module(
        injects = {AdHocRailwayApplication.class, ConnectActivity.class, ControllerActivity.class, LocomotiveSelectActivity.class, SettingsActivity.class, MainControllerFragment.class},
        library = true
)
public class AdHocRailwayModule {

    private final Context context;
    private AdHocRailwayApplication adHocRailwayApplication;

    AdHocRailwayModule(AdHocRailwayApplication adHocRailwayApplication) {
        this.adHocRailwayApplication = adHocRailwayApplication;
        this.context = adHocRailwayApplication.getApplicationContext();
    }

    @Provides
    public Context provideApplicationContext() {
        return this.context;
    }


    @Provides
    @Singleton
    public TurnoutManager provideTurnoutManager() {
        return new TurnoutManagerImpl();
    }

    @Provides
    @Singleton
    public RouteManager provideRouteManager(TurnoutManager turnoutManager) {
        return new RouteManagerImpl(turnoutManager);
    }

    @Provides
    @Singleton
    public LocomotiveManager providesLocomotiveManager() {
        return new LocomotiveManagerImpl();
    }

    @Provides
    @Singleton
    public Bus provideEventBus() {
        return new Bus(ThreadEnforcer.ANY);
    }
}
