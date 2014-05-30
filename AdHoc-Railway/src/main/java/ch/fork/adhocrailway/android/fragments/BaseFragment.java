package ch.fork.adhocrailway.android.fragments;

import android.app.Activity;
import android.app.Application;
import android.support.v4.app.Fragment;

import java.lang.reflect.AnnotatedElement;

import ch.fork.adhocrailway.android.AdHocRailwayApplication;

/**
 * Created by fork on 30.05.14.
 */
public class BaseFragment extends Fragment {


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        AdHocRailwayApplication adHocRailwayApplication = (AdHocRailwayApplication) activity.getApplication();
        adHocRailwayApplication.getObjectGraph().inject(this);
    }
}
