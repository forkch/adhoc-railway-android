package ch.fork.adhocrailway.android;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.util.SortedSet;
import java.util.UUID;

import ch.fork.AdHocRailway.model.locomotives.Locomotive;
import ch.fork.AdHocRailway.model.locomotives.LocomotiveGroup;
import ch.fork.AdHocRailway.model.turnouts.Turnout;
import ch.fork.AdHocRailway.model.turnouts.TurnoutGroup;
import ch.fork.AdHocRailway.model.turnouts.TurnoutState;
import ch.fork.AdHocRailway.model.turnouts.TurnoutType;
import ch.fork.AdHocRailway.persistence.adhocserver.impl.rest.RestLocomotiveService;
import ch.fork.AdHocRailway.persistence.adhocserver.impl.rest.RestTurnoutService;
import ch.fork.AdHocRailway.persistence.adhocserver.impl.socketio.SIOService;
import ch.fork.AdHocRailway.persistence.adhocserver.impl.socketio.ServiceListener;
import ch.fork.AdHocRailway.railway.srcp.SRCPTurnoutControlAdapter;
import ch.fork.AdHocRailway.services.AdHocServiceException;
import ch.fork.AdHocRailway.services.LocomotiveServiceListener;
import ch.fork.AdHocRailway.services.TurnoutServiceListener;
import de.dermoba.srcp.client.SRCPSession;
import de.dermoba.srcp.common.exception.SRCPException;

public class MainActivity extends Activity implements LocomotiveServiceListener, ServiceListener {

    private AdHocRailwayApplication adHocRailwayApplication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        adHocRailwayApplication = (AdHocRailwayApplication) getApplication();
        AsyncTask<Void, Void, Void> rest = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                RestLocomotiveService restLocomotiveService = new RestLocomotiveService("http://10.0.2.2:3000", UUID.randomUUID().toString());
                restLocomotiveService.init(MainActivity.this);
                SIOService.getInstance().connect("http://10.0.2.2:3000", MainActivity.this);
                return null;
            }
        };

        rest.execute();

        /*SortedSet<TurnoutGroup> allTurnoutGroups = restTurnoutService.getAllTurnoutGroups();
        for (TurnoutGroup allTurnoutGroup : allTurnoutGroups) {
            Log.d("", String.valueOf(allTurnoutGroup));
        }*/


        AsyncTask<Void, Void, Void> switchTurnout = new AsyncTask<Void, Void, Void>() {


            @Override
            protected Void doInBackground(Void... params) {
                try {
                    SRCPSession session = new SRCPSession("10.0.2.2", 4303);
                    session.connect();


                    SRCPTurnoutControlAdapter srcpTurnoutControlAdapter = new SRCPTurnoutControlAdapter();
                    srcpTurnoutControlAdapter.setSession(session);
                    Turnout turnout = new Turnout();
                    turnout.setBus1(1);
                    turnout.setAddress1(1);
                    turnout.setDefaultState(TurnoutState.STRAIGHT);
                    turnout.setType(TurnoutType.DEFAULT_LEFT);

                    srcpTurnoutControlAdapter.setCurvedLeft(turnout);
                } catch (SRCPException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };

        switchTurnout.execute();

    }

    public void onSelectLocomotiveClick(View view) {
        Intent selectLocomotiveIntent = new Intent(this, LocomotiveSelectActivity.class);
        startActivity(selectLocomotiveIntent);
    }

    @Override
    public void locomotiveAdded(Locomotive locomotive) {

        Log.d("", String.valueOf(locomotive));
    }

    @Override
    public void locomotiveUpdated(Locomotive locomotive) {

        Log.d("", String.valueOf(locomotive));
    }

    @Override
    public void locomotiveRemoved(Locomotive locomotive) {

        Log.d("", String.valueOf(locomotive));
    }

    @Override
    public void locomotiveGroupAdded(LocomotiveGroup group) {

        Log.d("", String.valueOf(group));
    }

    @Override
    public void locomotiveGroupUpdated(LocomotiveGroup group) {

        Log.d("", String.valueOf(group));
    }

    @Override
    public void locomotiveGroupRemoved(LocomotiveGroup group) {

        Log.d("", String.valueOf(group));
    }

    @Override
    public void locomotivesUpdated(SortedSet<LocomotiveGroup> locomotiveGroups) {
        Log.d("", String.valueOf(locomotiveGroups));
        adHocRailwayApplication.setLocomotiveGroups(locomotiveGroups);
    }

    @Override
    public void failure(AdHocServiceException arg0) {
        Log.e("", "error");
    }

    @Override
    public void connected() {
        Log.d("", "connected");
    }

    @Override
    public void connectionError(AdHocServiceException ex) {

        Log.e("", "error");
    }

    @Override
    public void disconnected() {

        Log.d("", "disconnected");
    }
}
