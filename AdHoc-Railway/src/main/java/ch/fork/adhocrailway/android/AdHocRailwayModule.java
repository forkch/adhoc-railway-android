package ch.fork.adhocrailway.android;

import android.content.Context;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

import javax.inject.Singleton;

import ch.fork.AdHocRailway.controllers.LocomotiveController;
import ch.fork.AdHocRailway.controllers.PowerController;
import ch.fork.AdHocRailway.controllers.RouteController;
import ch.fork.AdHocRailway.controllers.TurnoutController;
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
import ch.fork.adhocrailway.android.fragments.ControllerFragment;
import ch.fork.adhocrailway.android.fragments.PowerFragment;
import ch.fork.adhocrailway.android.presenters.ControllerPresenter;
import ch.fork.adhocrailway.android.presenters.ControllerPresenterImpl;
import dagger.Module;
import dagger.Provides;

/**
 * Created by fork on 30.05.14.
 */

@Module(
        injects = {AdHocRailwayApplication.class,
                ConnectActivity.class,
                ControllerActivity.class,
                LocomotiveSelectActivity.class,
                SettingsActivity.class,
                ControllerFragment.class,
                PowerFragment.class, ControllerPresenterImpl.class}
)
public class AdHocRailwayModule {

    private final Context context;
    private AdHocRailwayApplication adHocRailwayApplication;

    AdHocRailwayModule(AdHocRailwayApplication adHocRailwayApplication) {
        this.adHocRailwayApplication = adHocRailwayApplication;
        this.context = adHocRailwayApplication.getApplicationContext();
    }

    @Provides
    public AdHocRailwayApplication providesAdHocRailwayApplication() {
        return this.adHocRailwayApplication;
    }

    @Provides
    @Singleton
    public TurnoutManager providesTurnoutManager() {
        return new TurnoutManagerImpl();
    }

    @Provides
    @Singleton
    public RouteManager providesRouteManager(TurnoutManager turnoutManager) {
        return new RouteManagerImpl(turnoutManager);
    }

    @Provides
    @Singleton
    public LocomotiveManager providesLocomotiveManager() {
        return new LocomotiveManagerImpl();
    }

    @Provides
    @Singleton
    public TurnoutController providesTurnoutController() {
        return adHocRailwayApplication.getTurnoutController();
    }

    @Provides
    @Singleton
    public RouteController providesRouteController() {
        return adHocRailwayApplication.getRouteController();
    }

    @Provides
    @Singleton
    public LocomotiveController providesLocomotiveController() {
        return adHocRailwayApplication.getLocomotiveController();
    }

    @Provides
    @Singleton
    public PowerController providesPowerController() {
        return adHocRailwayApplication.getPowerController();
    }

    @Provides
    public ControllerPresenter providesControllerPresenter(AdHocRailwayApplication adHocRailwayApplication) {
        return new ControllerPresenterImpl(adHocRailwayApplication);
    }

    @Provides
    @Singleton
    public Bus providesEventBus() {
        return new Bus(ThreadEnforcer.ANY);
    }
}
