package ch.fork.adhocrailway.android;

import android.app.Application;
import android.os.AsyncTask;
import android.util.Log;

import com.google.common.eventbus.EventBus;

import java.util.SortedSet;
import java.util.UUID;

import ch.fork.AdHocRailway.manager.TurnoutManager;
import ch.fork.AdHocRailway.manager.TurnoutManagerListener;
import ch.fork.AdHocRailway.manager.impl.TurnoutManagerImpl;
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
import ch.fork.AdHocRailway.railway.srcp.SRCPLocomotiveControlAdapter;
import ch.fork.AdHocRailway.railway.srcp.SRCPTurnoutControlAdapter;
import ch.fork.AdHocRailway.services.AdHocServiceException;
import ch.fork.AdHocRailway.services.LocomotiveServiceListener;
import de.dermoba.srcp.client.SRCPSession;
import de.dermoba.srcp.common.exception.SRCPException;

/**
 * Created by fork on 4/16/14.
 */
public class AdHocRailwayApplication extends Application implements LocomotiveServiceListener, ServiceListener, TurnoutManagerListener {
    private SortedSet<LocomotiveGroup> locomotiveGroups;
    private Locomotive selectedLocomotive;

    private SRCPLocomotiveControlAdapter srcpLocomotiveControlAdapter;
    private SRCPTurnoutControlAdapter srcpTurnoutControlAdapter;

    private TurnoutManager turnoutManager;

    @Override
    public void onCreate() {
        super.onCreate();
        connectToAdHocServer();
        connectToSrcpd();
    }

    public SortedSet<LocomotiveGroup> getLocomotiveGroups() {
        return locomotiveGroups;
    }

    public void setLocomotiveGroups(SortedSet<LocomotiveGroup> locomotiveGroups) {
        this.locomotiveGroups = locomotiveGroups;
    }

    public Locomotive getSelectedLocomotive() {
        return selectedLocomotive;
    }

    public void setSelectedLocomotive(Locomotive selectedLocomotive) {
        this.selectedLocomotive = selectedLocomotive;
    }

    private void connectToSrcpd() {
        AsyncTask<Void, Void, Void> switchTurnout = new AsyncTask<Void, Void, Void>() {


            @Override
            protected Void doInBackground(Void... params) {
                try {
                    SRCPSession session = new SRCPSession("10.0.2.2", 4303);
                    session.connect();


                    srcpTurnoutControlAdapter = new SRCPTurnoutControlAdapter();
                    srcpLocomotiveControlAdapter = new SRCPLocomotiveControlAdapter();
                    srcpTurnoutControlAdapter.setSession(session);
                    srcpLocomotiveControlAdapter.setSession(session);
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

    private void connectToAdHocServer() {
        AsyncTask<Void, Void, Void> rest = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                turnoutManager = new TurnoutManagerImpl();
                turnoutManager.addTurnoutManagerListener(AdHocRailwayApplication.this);

                RestLocomotiveService restLocomotiveService = new RestLocomotiveService("http://10.0.2.2:3000", UUID.randomUUID().toString());
                RestTurnoutService restTurnoutService = new RestTurnoutService("http://10.0.2.2:3000", UUID.randomUUID().toString());
                turnoutManager.setTurnoutService(restTurnoutService);
                turnoutManager.initialize(new EventBus());
                SIOService.getInstance().connect("http://10.0.2.2:3000", AdHocRailwayApplication.this);
                return null;
            }
        };

        rest.execute();
    }

    public SRCPTurnoutControlAdapter getSrcpTurnoutControlAdapter() {
        return srcpTurnoutControlAdapter;
    }

    public SRCPLocomotiveControlAdapter getSrcpLocomotiveControlAdapter() {
        return srcpLocomotiveControlAdapter;
    }

    public TurnoutManager getTurnoutManager() {
        return turnoutManager;
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
        setLocomotiveGroups(locomotiveGroups);
    }

    @Override
    public void turnoutsUpdated(SortedSet<TurnoutGroup> turnoutGroups) {

    }

    @Override
    public void turnoutAdded(Turnout turnout) {

    }

    @Override
    public void turnoutUpdated(Turnout turnout) {

    }

    @Override
    public void turnoutRemoved(Turnout turnout) {

    }

    @Override
    public void turnoutGroupAdded(TurnoutGroup group) {

    }

    @Override
    public void turnoutGroupUpdated(TurnoutGroup group) {

    }

    @Override
    public void turnoutGroupRemoved(TurnoutGroup group) {

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
