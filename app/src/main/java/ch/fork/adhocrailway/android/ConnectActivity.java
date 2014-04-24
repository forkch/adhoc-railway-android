package ch.fork.adhocrailway.android;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import ch.fork.AdHocRailway.manager.impl.events.LocomotivesUpdatedEvent;
import ch.fork.AdHocRailway.manager.impl.events.RoutesUpdatedEvent;
import ch.fork.AdHocRailway.manager.impl.events.TurnoutsUpdatedEvent;

public class ConnectActivity extends Activity {

    private Button connectButton;
    private AdHocRailwayApplication adHocRailwayApplication;
    private boolean locomotivesLoaded;
    private boolean routesLoaded;
    private boolean turnoutsLoaded;
    private boolean connectedToRailwayDevice;

    @Override
    protected void onResume() {
        super.onResume();
        initValues();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);
        adHocRailwayApplication = (AdHocRailwayApplication) getApplication();
        connectButton = (Button) findViewById(R.id.connectButton);
        initEventHandling();
    }

    private void initValues() {

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String adhocServerHost = sharedPref.getString(SettingsActivity.KEY_ADHOC_SERVER_HOST, AdHocRailwayApplication.SERVER_HOST);
        String srcpServerHost = sharedPref.getString(SettingsActivity.KEY_SRCP_SERVER_HOST, AdHocRailwayApplication.SERVER_HOST);
        TextView adHocServerHostTextView = (TextView) findViewById(R.id.adhocServerHostTextView);
        TextView srcpServerHostTextView = (TextView) findViewById(R.id.srcpServerHostTextView);
        adHocServerHostTextView.setText(getString(R.string.adhocServerHostLabel) + adhocServerHost);
        srcpServerHostTextView.setText(getString(R.string.srcpServerHostLabel) + srcpServerHost);
    }

    private void initEventHandling() {
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
        turnoutsLoaded = true;
        startMainActivityIfEverythingIsLoaded();
    }

    @Subscribe
    public void routesUpdated(RoutesUpdatedEvent event) {
        routesLoaded = true;
        startMainActivityIfEverythingIsLoaded();
    }

}
