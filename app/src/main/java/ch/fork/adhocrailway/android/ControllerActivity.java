package ch.fork.adhocrailway.android;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;

public class ControllerActivity extends FragmentActivity implements NumberControlFragment.OnFragmentInteractionListener, LocomotiveControlFragment.OnFragmentInteractionListener {

    private AdHocRailwayApplication adHocRailwayApplication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controller);

        adHocRailwayApplication = (AdHocRailwayApplication) getApplication();

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        FrameLayout numberControlContainer = (FrameLayout) findViewById(R.id.numberControlContainer);
        numberControlContainer.removeAllViews();

        FrameLayout locomotiveControlContainer = (FrameLayout) findViewById(R.id.locomotiveControlContainer);
        locomotiveControlContainer.removeAllViews();

        NumberControlFragment numberControl = NumberControlFragment.newInstance();
        fragmentTransaction.add(R.id.numberControlContainer, numberControl);
        LocomotiveControlFragment locomotiveControlFragment = LocomotiveControlFragment.newInstance();
        fragmentTransaction.add(R.id.locomotiveControlContainer, locomotiveControlFragment);

        fragmentTransaction.commit();

    }

    @Override
    protected void onResume() {
        super.onResume();
        adHocRailwayApplication = (AdHocRailwayApplication) getApplication();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.connect, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
