package ch.fork.adhocrailway.android.activities;

import android.app.Activity;
import android.app.ListActivity;
import android.os.Bundle;

import ch.fork.adhocrailway.android.AdHocRailwayApplication;

/**
 * Created by fork on 30.05.14.
 */
public abstract class BaseListActivity extends ListActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AdHocRailwayApplication adHocRailwayApplication = (AdHocRailwayApplication) getApplication();
        adHocRailwayApplication.getObjectGraph().inject(this);
    }

}
