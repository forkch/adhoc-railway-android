package ch.fork.adhocrailway.android;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.InjectView;
import ch.fork.AdHocRailway.manager.impl.events.LocomotivesUpdatedEvent;
import ch.fork.AdHocRailway.manager.impl.events.RoutesUpdatedEvent;
import ch.fork.AdHocRailway.manager.impl.events.TurnoutsUpdatedEvent;

public class ConnectActivity extends Activity {

    private static final String TAG = ConnectActivity.class.getSimpleName();
    @InjectView(R.id.connectButton)
    Button connectButton;

    @InjectView(R.id.connectingProgress)
     ProgressBar connectingProgress;
    @InjectView(R.id.adhocServerHostTextView)
    TextView adHocServerHostTextView;
    @InjectView(R.id.srcpServerHostTextView)
    TextView srcpServerHostTextView;

    private AdHocRailwayApplication adHocRailwayApplication;
    private boolean locomotivesLoaded;
    private boolean routesLoaded;
    private boolean turnoutsLoaded;
    private boolean connectedToRailwayDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);
        adHocRailwayApplication = (AdHocRailwayApplication) getApplication();
        ButterKnife.inject(this);
        initEventHandling();
    }

    private void initValues() {

        connectButton.setEnabled(true);
        connectingProgress.setVisibility(View.INVISIBLE);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String adhocServerHost = sharedPref.getString(SettingsActivity.KEY_ADHOC_SERVER_HOST, AdHocRailwayApplication.SERVER_HOST);
        String srcpServerHost = sharedPref.getString(SettingsActivity.KEY_SRCP_SERVER_HOST, AdHocRailwayApplication.SERVER_HOST);
        adHocServerHostTextView.setText(getString(R.string.adhocServerHostLabel) + " " + adhocServerHost);
        srcpServerHostTextView.setText(getString(R.string.srcpServerHostLabel) + " " + srcpServerHost);
    }

    private void initEventHandling() {
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectingProgress.setVisibility(View.VISIBLE);
                connectButton.setEnabled(false);
                adHocRailwayApplication.clearServers();
                adHocRailwayApplication.connectToPersistence();
                adHocRailwayApplication.connectToRailwayDevice();
            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();
        adHocRailwayApplication.getBus().register(this);
    }
    @Override
    protected void onResume() {
        super.onResume();
        initValues();
        locomotivesLoaded = false;
        turnoutsLoaded = false;
        routesLoaded = false;
        connectedToRailwayDevice = false;
    }


    @Override
    protected void onStop() {
        super.onStop();
        adHocRailwayApplication.getBus().unregister(this);
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
    public void connectedToRailwayDevice(ConnectedToRailwayDeviceEvent event) {
        connectedToRailwayDevice = event.isConnected();
        startMainActivityIfEverythingIsLoaded();
    }

    @Subscribe
    public void locomotivesUpdated(LocomotivesUpdatedEvent event) {
        Log.i(TAG, "received LocomotivesUpdatedEvent");
        locomotivesLoaded = true;
        startMainActivityIfEverythingIsLoaded();
    }

    private void startMainActivityIfEverythingIsLoaded() {
        if (connectedToRailwayDevice && locomotivesLoaded && turnoutsLoaded && routesLoaded) {
            startActivity(new Intent(this, ControllerActivity.class));
        }
    }

    @Subscribe
    public void turnoutsUpdates(TurnoutsUpdatedEvent event) {
        Log.i(TAG, "received TurnoutsUpdatedEvent");
        turnoutsLoaded = true;
        startMainActivityIfEverythingIsLoaded();
    }

    @Subscribe
    public void routesUpdated(RoutesUpdatedEvent event) {
        Log.i(TAG, "received RoutesUpdatedEvent");
        routesLoaded = true;
        startMainActivityIfEverythingIsLoaded();
    }

}
