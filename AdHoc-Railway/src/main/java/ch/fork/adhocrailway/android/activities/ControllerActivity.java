package ch.fork.adhocrailway.android.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.common.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import ch.fork.adhocrailway.android.AdHocRailwayApplication;
import ch.fork.adhocrailway.android.R;
import ch.fork.adhocrailway.android.events.InfoEvent;
import ch.fork.adhocrailway.android.fragments.ControllerFragment;
import ch.fork.adhocrailway.android.fragments.PowerFragment;

public class ControllerActivity extends BaseFragmentActivity implements ControllerFragment.OnFragmentInteractionListener, PowerFragment.OnPowerFragmentInteractionListener {

    private static final int NUM_CONTROLLER_FRAGMENTS = 4;
    @Inject
    AdHocRailwayApplication adHocRailwayApplication;

    private ViewPager mPager;

    private PagerAdapter mPagerAdapter;

    public ControllerActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controller);

        // Instantiate a ViewPager and a PagerAdapter.
        mPager = (ViewPager) findViewById(R.id.pager);
        mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        mPager.setCurrentItem(1);
    }

    @Override
    protected void onResume() {
        super.onResume();
        adHocRailwayApplication = (AdHocRailwayApplication) getApplication();
        onLocomotiveSelected();

    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        if (mPager.getCurrentItem() == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed();
        } else {
            // Otherwise, select the previous step.
            mPager.setCurrentItem(mPager.getCurrentItem() - 1);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
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
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Subscribe
    public void onInfonEvent(InfoEvent event) {
        Toast.makeText(this, event.getMessage(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLocomotiveSelected() {
        mPagerAdapter.notifyDataSetChanged();
        mPager.requestLayout();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            findViewById(R.id.controller).setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            );
        }
    }


    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        private final List<ControllerFragment> fragments = new ArrayList<ControllerFragment>();
        private PowerFragment powerFragment;

        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);

            powerFragment = PowerFragment.newInstance();
            for (int i = 0; i < NUM_CONTROLLER_FRAGMENTS; i++) {
                ControllerFragment mainControllerFragment = ControllerFragment.newInstance(i);
                fragments.add(mainControllerFragment);
            }
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                return powerFragment;
            }
            return fragments.get(position - 1);
        }

        @Override
        public int getCount() {
            return 1 + fragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (position == 0) {
                return "Power";
            }
            return fragments.get(position - 1).getTitle();
        }
    }
}
