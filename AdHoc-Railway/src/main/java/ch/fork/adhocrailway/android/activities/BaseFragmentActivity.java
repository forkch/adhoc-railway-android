package ch.fork.adhocrailway.android.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import ch.fork.adhocrailway.android.AdHocRailwayApplication;

/**
 * Created by fork on 30.05.14.
 */
public abstract class BaseFragmentActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AdHocRailwayApplication adHocRailwayApplication = (AdHocRailwayApplication) getApplication();
        adHocRailwayApplication.getObjectGraph().inject(this);
    }
}
