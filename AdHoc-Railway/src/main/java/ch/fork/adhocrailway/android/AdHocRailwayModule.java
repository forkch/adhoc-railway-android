package ch.fork.adhocrailway.android;

import android.content.Context;

import com.squareup.otto.Bus;

import javax.inject.Singleton;

import ch.fork.AdHocRailway.controllers.LocomotiveController;
import ch.fork.AdHocRailway.controllers.PowerController;
import ch.fork.AdHocRailway.controllers.RouteController;
import ch.fork.AdHocRailway.controllers.TurnoutController;
import ch.fork.AdHocRailway.manager.LocomotiveManager;
import ch.fork.AdHocRailway.manager.RouteManager;
import ch.fork.AdHocRailway.manager.TurnoutManager;
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
                PowerFragment.class, ControllerPresenterImpl.class}, library = true
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
    public RailwayDeviceContext providesRailwayDeviceContext() {
        return new RailwayDeviceContext();
    }

    @Provides
    @Singleton
    public PersistenceContext providesPersistenceContext() {
        return new PersistenceContext(adHocRailwayApplication);
    }

    @Provides
    public TurnoutManager providesTurnoutManager(PersistenceContext persistenceContext) {
        return persistenceContext.getTurnoutManager();
    }

    @Provides
    public RouteManager providesRouteManager(PersistenceContext persistenceContext) {
        return persistenceContext.getRouteManager();
    }

    @Provides
    public LocomotiveManager providesLocomotiveManager(PersistenceContext persistenceContext) {
        return persistenceContext.getLocomotiveManager();
    }

    @Provides
    @Singleton
    public TurnoutController providesTurnoutController(RailwayDeviceContext railwayDeviceContext) {
        return railwayDeviceContext.getTurnoutController();
    }

    @Provides
    @Singleton
    public RouteController providesRouteController(RailwayDeviceContext railwayDeviceContext) {
        return railwayDeviceContext.getRouteController();
    }

    @Provides
    @Singleton
    public LocomotiveController providesLocomotiveController(RailwayDeviceContext railwayDeviceContext) {
        return railwayDeviceContext.getLocomotiveController();
    }

    @Provides
    @Singleton
    public PowerController providesPowerController(RailwayDeviceContext railwayDeviceContext) {
        return railwayDeviceContext.getPowerController();
    }

    @Provides
    public ControllerPresenter providesControllerPresenter(AdHocRailwayApplication adHocRailwayApplication) {
        return new ControllerPresenterImpl(adHocRailwayApplication);
    }

    @Provides
    @Singleton
    public Bus providesEventBus() {
        return MainThreadBus.getMainThreadBus();
    }
}
