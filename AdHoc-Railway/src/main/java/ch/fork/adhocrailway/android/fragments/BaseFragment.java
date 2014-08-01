package ch.fork.adhocrailway.android.fragments;

import android.app.Activity;
import android.app.Application;
import android.support.v4.app.Fragment;

import com.squareup.otto.Bus;

import java.lang.reflect.AnnotatedElement;

import javax.inject.Inject;

import ch.fork.adhocrailway.android.AdHocRailwayApplication;

/**
 * Created by fork on 30.05.14.
 */
public class BaseFragment extends Fragment {


    @Inject
    Bus bus;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        AdHocRailwayApplication adHocRailwayApplication = (AdHocRailwayApplication) activity.getApplication();
        adHocRailwayApplication.inject(this);
        bus.register(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        bus.unregister(this);
    }
}
