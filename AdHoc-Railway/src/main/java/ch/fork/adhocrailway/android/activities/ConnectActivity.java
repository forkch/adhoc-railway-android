package ch.fork.adhocrailway.android.activities;

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
import android.widget.Toast;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import ch.fork.AdHocRailway.manager.LocomotiveManager;
import ch.fork.AdHocRailway.manager.RouteManager;
import ch.fork.AdHocRailway.manager.TurnoutManager;
import ch.fork.AdHocRailway.manager.impl.events.LocomotivesUpdatedEvent;
import ch.fork.AdHocRailway.manager.impl.events.RoutesUpdatedEvent;
import ch.fork.AdHocRailway.manager.impl.events.TurnoutsUpdatedEvent;
import ch.fork.adhocrailway.android.AdHocRailwayApplication;
import ch.fork.adhocrailway.android.R;
import ch.fork.adhocrailway.android.events.ConnectedToRailwayDeviceEvent;
import ch.fork.adhocrailway.android.events.ExceptionEvent;
import ch.fork.adhocrailway.android.events.InfoEvent;
import ch.fork.adhocrailway.android.jobs.ConnectToPersistenceJob;
import ch.fork.adhocrailway.android.jobs.ConnectToRailwayDeviceJob;

public class ConnectActivity extends BaseActivity {

    private static final String TAG = ConnectActivity.class.getSimpleName();
    @InjectView(R.id.connectButton)
    Button connectButton;

    @InjectView(R.id.connectingProgress)
     ProgressBar connectingProgress;
    @InjectView(R.id.adhocServerHostTextView)
    TextView adHocServerHostTextView;
    @InjectView(R.id.srcpServerHostTextView)
    TextView srcpServerHostTextView;

    @InjectView(R.id.serversTextView)
    TextView serversTextView;


    @Inject
    TurnoutManager turnoutManager;
    @Inject
    RouteManager routeManager;
    @Inject
    LocomotiveManager locomotiveManager;
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
        boolean dummyServers = sharedPref.getBoolean(SettingsActivity.KEY_USE_DUMMY_SERVICES, false);
        if(dummyServers) {
            serversTextView.setText("Servers: DUMMY!!!");
        } else {
            serversTextView.setText("Servers:");
        }
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
                connectToPersistence();
                connectToRailwayDevice();
            }
        });
    }

    public void connectToRailwayDevice() {

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean useDummyServices = sharedPref.getBoolean(SettingsActivity.KEY_USE_DUMMY_SERVICES, false);

        ConnectToRailwayDeviceJob connectToRailwayDeviceJob = new ConnectToRailwayDeviceJob(adHocRailwayApplication, useDummyServices, turnoutManager);
        adHocRailwayApplication.getJobManager().addJobInBackground(connectToRailwayDeviceJob);
    }

    public void connectToPersistence() {
        turnoutManager.addTurnoutManagerListener(adHocRailwayApplication);
        routeManager.addRouteManagerListener(adHocRailwayApplication);
        locomotiveManager.addLocomotiveManagerListener(adHocRailwayApplication);


        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean useDummyServices = sharedPref.getBoolean(SettingsActivity.KEY_USE_DUMMY_SERVICES, false);

        ConnectToPersistenceJob connectToPersistenceJob = new ConnectToPersistenceJob(adHocRailwayApplication, useDummyServices, turnoutManager, routeManager, locomotiveManager);

        adHocRailwayApplication.getJobManager().addJobInBackground(connectToPersistenceJob);
    }

    @Subscribe
    public void onExceptionEvent(ExceptionEvent event) {
        Toast.makeText(this, event.getMessage(), Toast.LENGTH_SHORT).show();
    }

    @Subscribe
    public void onInfonEvent(InfoEvent event) {
        Toast.makeText(this, event.getMessage(), Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStart() {
        super.onStart();
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
