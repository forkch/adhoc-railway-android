package ch.fork.adhocrailway.android.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.path.android.jobqueue.Job;
import com.squareup.otto.Subscribe;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayDeque;
import java.util.Deque;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import ch.fork.AdHocRailway.controllers.LocomotiveChangeListener;
import ch.fork.AdHocRailway.controllers.LocomotiveController;
import ch.fork.AdHocRailway.controllers.RouteController;
import ch.fork.AdHocRailway.controllers.TurnoutController;
import ch.fork.AdHocRailway.manager.RouteManager;
import ch.fork.AdHocRailway.manager.TurnoutManager;
import ch.fork.AdHocRailway.model.locomotives.Locomotive;
import ch.fork.AdHocRailway.model.turnouts.Route;
import ch.fork.AdHocRailway.model.turnouts.Turnout;
import ch.fork.adhocrailway.android.AdHocRailwayApplication;
import ch.fork.adhocrailway.android.R;
import ch.fork.adhocrailway.android.activities.ControllerActivity;
import ch.fork.adhocrailway.android.activities.LocomotiveSelectActivity;
import ch.fork.adhocrailway.android.jobs.NetworkJob;
import ch.fork.adhocrailway.android.presenters.ControllerPresenter;
import ch.fork.adhocrailway.android.utils.ImageHelper;


public class ControllerFragment extends BaseFragment implements LocomotiveChangeListener {

    private static final String TAG = ControllerFragment.class.getSimpleName();
    ViewGroup functionContainer;
    @BindView(R.id.locomotive1Speed)
    SeekBar locomotive1Seekbar;
    @BindView(R.id.locomotive1Direction)
    Button directionButton;
    @BindView(R.id.locomotive1Stop)
    Button stopButton;
    @BindView(R.id.locomotiveEmergencyStop)
    Button emergencyStopButton;
    @BindView(R.id.selectedLocomotive)
    LinearLayout selectedLocomotiveView;

    @BindView(R.id.turnoutButtonDefault)
    Button defaultStateButton;
    @BindView(R.id.turnoutButtonNonDefault)
    Button nonDefaultStateButton;
    @BindView(R.id.turnoutButtonLeft)
    Button leftStateButton;

    @BindView(R.id.turnoutButtonRight)
    Button rightStateButton;
    @BindView(R.id.turnoutButtonStraight)
    Button straightStateButton;
    @BindView(R.id.turnoutButtonPeriod)
    Button periodButton;

    @Inject
    TurnoutManager turnoutManager;
    @Inject
    RouteManager routeManager;
    @Inject
    LocomotiveController locomotiveController;
    @Inject
    TurnoutController turnoutController;
    @Inject
    RouteController routeController;
    @Inject
    AdHocRailwayApplication adHocRailwayApplication;
    @Inject
    ControllerPresenter controllerPresenter;

    private boolean disableProgressBarListener = false;

    private OnFragmentInteractionListener mListener;
    private View fragmentView;
    private int number;
    private Locomotive selectedLocomotive;
    private StringBuffer enteredNumberKeys = new StringBuffer();
    private TextView currentNumber;
    private NumberControlState numberControlState = NumberControlState.TURNOUT;
    private TextView routeIndicator;
    private Deque<Object> previousChangedObjects = new ArrayDeque<Object>();
    private Vibrator vibrator;

    public ControllerFragment() {
    }

    public static ControllerFragment newInstance(int number) {
        ControllerFragment fragment = new ControllerFragment();
        Bundle args = new Bundle();
        args.putInt("number", number);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            number = getArguments().getInt("number");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && resultCode == Activity.RESULT_OK) {

            final Locomotive oldLoco = selectedLocomotive;
            if (selectedLocomotive != null) {

                locomotiveController.removeLocomotiveChangeListener(oldLoco, this);
                enqueueJob(new NetworkJob() {
                    @Override
                    public void onRun() throws Throwable {
                        locomotiveController.terminateLocomotive(oldLoco);
                    }
                });
            }

            selectedLocomotive = (Locomotive) data.getSerializableExtra("selectedLocomotive");
            locomotiveController.addLocomotiveChangeListener(selectedLocomotive, this);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fragmentView = inflater.inflate(R.layout.fragment_main_controller, container, false);
        Unbinder unbinder = ButterKnife.bind(this, fragmentView);
        currentNumber = (TextView) fragmentView.findViewById(R.id.currentNumber);
        routeIndicator = (TextView) fragmentView.findViewById(R.id.routeIndicator);

        initNumberControlEventHandling();
        initLocomotiveEventHandling();

        return fragmentView;

    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        updateSelectedLocomotive();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("selectedLocomotive", selectedLocomotive);
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null && savedInstanceState.containsKey("selectedLocomotive")) {
            selectedLocomotive = (Locomotive) savedInstanceState.getSerializable("selectedLocomotive");
            if (selectedLocomotive != null) {
                updateSelectedLocomotive();
            }
        }
    }

    private void initLocomotiveEventHandling() {

        selectedLocomotiveView.setOnClickListener(new SelectLocomotiveListener());

        locomotive1Seekbar.setOnSeekBarChangeListener(new Locomotive1SpeedListener());

        directionButton.setOnClickListener(new Locomotive1DirectionListener());

        stopButton.setOnClickListener(new Locomotive1StopListener());

        emergencyStopButton.setOnClickListener(new EmergencyStopListener());

        functionContainer = (ViewGroup) fragmentView.findViewById(R.id.functionContainer);

        if (functionContainer != null) {
            for (int i = 0; i <= 8; i++) {
                Button functionButton = (Button) fragmentView.findViewById(getResources().getIdentifier("locomotive1F" + i, "id", getActivity().getPackageName()));
                if (functionButton != null) {
                    //some layout have no function buttons
                    functionButton.setOnClickListener(new FunctionButtonClickListener(i));
                }
            }
        }
    }

    private void updateSelectedLocomotive() {
        TextView label = (TextView) selectedLocomotiveView.findViewById(R.id.label);
        ImageView imageView = (ImageView) selectedLocomotiveView.findViewById(R.id.icon);

        if (selectedLocomotive != null) {
            label.setText(selectedLocomotive.getName());
            if (StringUtils.isNotBlank(selectedLocomotive.getImageBase64())) {
                ImageHelper.fillImageViewFromBase64ImageString(imageView, selectedLocomotive.getImageBase64());
            } else {
                imageView.setImageBitmap(null);
            }
            locomotiveController.activateLoco(selectedLocomotive);

        } else {
            label.setText("select locomotive");
            imageView.setImageBitmap(null);
        }

        mListener.onLocomotiveSelected();
    }

    @Subscribe
    public void onLocomotiveUpdated(final Locomotive locomotive) {
        locomotive1Seekbar.post(new Runnable() {
            @Override
            public void run() {


                if (functionContainer != null) {
                    boolean[] currentFunctions = locomotive.getCurrentFunctions();
                    for (int i = 0; i <= 8; i++) {
                        Button functionButton = (Button) fragmentView.findViewById(getResources().getIdentifier("locomotive1F" + i, "id", getActivity().getPackageName()));
                        if (functionButton != null && currentFunctions.length > i) {
                            //some layout have no fun
                            if (currentFunctions[i])
                                functionButton.setBackground(getResources().getDrawable(R.drawable.bring_button_primary_red));
                            else
                                functionButton.setBackground(getResources().getDrawable(R.drawable.bring_button_primary));

                        }
                    }
                }


            }
        });

    }

    private void initNumberControlEventHandling() {

        for (int i = 0; i < 10; i++) {
            Button turnoutButton = (Button) fragmentView.findViewById(getResources().getIdentifier("turnoutButton" + i, "id", getActivity().getPackageName()));
            turnoutButton.setOnClickListener(new NumberButtonClickListener(i));
        }

        defaultStateButton.setOnClickListener(new DefaultStateHandler());

        nonDefaultStateButton.setOnClickListener(new NonDefaultStateHandler());

        leftStateButton.setOnClickListener(new LeftStateHandler());

        straightStateButton.setOnClickListener(new StraightStateHandler());

        rightStateButton.setOnClickListener(new RightStateHandler());

        periodButton.setOnClickListener(new PeriodButtonHandler());
    }

    private void resetNumbers() {
        currentNumber.post(new Runnable() {
            @Override
            public void run() {
                enteredNumberKeys = new StringBuffer();
                currentNumber.setText("---");
                numberControlState = NumberControlState.TURNOUT;
                routeIndicator.setVisibility(View.INVISIBLE);
            }
        });
    }

    private int getEnteredNumber() {
        if (StringUtils.isBlank(enteredNumberKeys.toString())) {
            return -1;
        }
        return Integer.parseInt(enteredNumberKeys.toString());
    }

    public CharSequence getTitle() {
        if (selectedLocomotive != null) {
            return selectedLocomotive.getName();
        } else {
            return "n/a";
        }
    }

    private void updatePreviousChangedObject() {
        fragmentView.post(new Runnable() {
            @Override
            public void run() {
                LinearLayout lastTurnouts = (LinearLayout) fragmentView.findViewById(R.id.lastTurnouts);
                lastTurnouts.removeAllViews();
                for (Object obj : previousChangedObjects) {
                    TextView v = new TextView(getActivity());
                    v.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 30);
                    if (obj instanceof Turnout) {
                        v.setText("T" + Integer.toString(((Turnout) obj).getNumber()));
                    } else {
                        v.setText("R" + Integer.toString(((Route) obj).getNumber()));
                    }
                    v.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    v.setGravity(Gravity.CENTER);
                    lastTurnouts.addView(v);
                }
            }
        });

    }

    private void enqueueJob(Job job) {
        adHocRailwayApplication.getJobManager().addJobInBackground(job);
    }

    private void vibrate() {
        vibrator = (Vibrator) adHocRailwayApplication.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(50);
    }

    @Override
    public void locomotiveChanged(Locomotive changedLocomotive) {
        onLocomotiveUpdated(changedLocomotive);
    }

    private enum NumberControlState {
        TURNOUT, ROUTE, LOCOMOTIVE_FUNCTION;
    }

    public interface OnFragmentInteractionListener {
        void onLocomotiveSelected();
    }

    private class EmergencyStopListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            controllerPresenter.emergencyStop(selectedLocomotive);
            vibrate();
        }
    }

    private class Locomotive1SpeedListener implements SeekBar.OnSeekBarChangeListener {
        private int previousSpeed = 0;

        @Override
        public void onProgressChanged(SeekBar seekBar, final int progress, boolean fromUser) {
            if (Math.abs(previousSpeed - progress) > 5 || progress == 0 || progress == 127) {
                previousSpeed = progress;
                if (!disableProgressBarListener)
                    controllerPresenter.setSpeed(selectedLocomotive, progress);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    }

    private class Locomotive1DirectionListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            controllerPresenter.toggleDirection(selectedLocomotive);
            vibrate();
        }
    }

    private class Locomotive1StopListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            controllerPresenter.stopLocomotive(selectedLocomotive);

            disableProgressBarListener = true;
            locomotive1Seekbar.setProgress(0);
            disableProgressBarListener = false;
            vibrate();

        }
    }

    private class FunctionButtonClickListener implements View.OnClickListener {
        private int functionNumber;

        public FunctionButtonClickListener(int functionNumber) {
            this.functionNumber = functionNumber;
        }

        @Override
        public void onClick(View v) {
            controllerPresenter.toggleLocomotiveFunction(selectedLocomotive, functionNumber);

            vibrate();
        }
    }

    private class SelectLocomotiveListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (selectedLocomotive == null || selectedLocomotive.getCurrentSpeed() == 0) {
                startActivityForResult(new Intent(getActivity(), LocomotiveSelectActivity.class), 0);
            } else {
                Toast.makeText(getActivity(), "Please stop locomotive first", Toast.LENGTH_SHORT).show();
            }
        }

    }

    private class NumberButtonClickListener implements View.OnClickListener {

        private int number;

        public NumberButtonClickListener(int number) {
            this.number = number;
        }

        @Override
        public void onClick(View v) {
            enteredNumberKeys.append(number);
            final int currentEnteredNumber = getEnteredNumber();
            Log.i(ControllerActivity.class.getSimpleName(), "current entered number: " + currentEnteredNumber);
            if (currentEnteredNumber > 999) {
                resetNumbers();
                return;
            }
            currentNumber.setText("" + currentEnteredNumber);
        }
    }

    private abstract class NumberControlActionHandler implements View.OnClickListener {

        protected abstract void doPerformStateAction(TurnoutController srcpTurnoutControlAdapter, Turnout turnout);

        protected abstract void doPerformStateAction(RouteController routeController, Route routeByNumber);

        protected void storePreviousChangedObject(Object obj) {
            previousChangedObjects.addLast(obj);
            updatePreviousChangedObject();
        }

        @Override
        public void onClick(View v) {
            enqueueJob(new NetworkJob() {
                @Override
                public void onRun() throws Throwable {
                    int enteredNumber = getEnteredNumber();
                    if (enteredNumber == -1) {
                        return;
                    }
                    if (numberControlState == NumberControlState.TURNOUT) {
                        handleTurnoutChange(enteredNumber);
                    } else {
                        handleRouteChange(enteredNumber);
                    }
                    resetNumbers();
                }

                private void handleRouteChange(int enteredNumber) {
                    Route routeByNumber = routeManager.getRouteByNumber(enteredNumber);
                    if (routeByNumber == null) {
                        resetNumbers();
                        return;
                    }
                    doPerformStateAction(routeController, routeByNumber);
                    vibrate();
                }

                private void handleTurnoutChange(int enteredNumber) {
                    Turnout turnoutByNumber = turnoutManager.getTurnoutByNumber(enteredNumber);
                    if (turnoutByNumber == null) {
                        resetNumbers();
                        return;
                    }
                    doPerformStateAction(turnoutController, turnoutByNumber);
                    vibrate();
                }
            });
        }

    }

    private class DefaultStateHandler extends NumberControlActionHandler {

        @Override
        public void onClick(View v) {
            if (StringUtils.isBlank(enteredNumberKeys.toString())) {
                if (previousChangedObjects.isEmpty()) {
                    return;
                }
                final Object obj = previousChangedObjects.removeFirst();

                if (obj == null) {
                    return;
                }
                enqueueJob(new NetworkJob() {
                               @Override
                               public void onRun() throws Throwable {
                                   if (obj instanceof Turnout) {
                                       doPerformStateAction(turnoutController, (Turnout) obj);
                                   } else {
                                       doPerformStateAction(routeController, (Route) obj);
                                   }
                                   vibrate();
                                   updatePreviousChangedObject();
                               }
                           }
                );
            } else {
                super.onClick(v);
            }
        }

        @Override
        protected void doPerformStateAction(TurnoutController turnoutController, Turnout turnout) {
            if (turnout.isLinkedToRoute()) {
                routeController.disableRoute(routeManager.getRouteByNumber(turnout.getLinkedRouteNumber()));
            } else {
                turnoutController.setDefaultState(turnout);
            }
        }

        @Override
        protected void doPerformStateAction(RouteController routeController, Route routeByNumber) {
            routeController.disableRoute(routeByNumber);
        }

    }

    private class NonDefaultStateHandler extends NumberControlActionHandler {

        @Override
        protected void doPerformStateAction(TurnoutController turnoutController, Turnout turnout) {
            if (turnout.isLinkedToRoute()) {
                routeController.enableRoute(routeManager.getRouteByNumber(turnout.getLinkedRouteNumber()));
            } else {
                turnoutController.setNonDefaultState(turnout);
            }
            storePreviousChangedObject(turnout);
        }

        @Override
        protected void doPerformStateAction(RouteController routeController, Route routeByNumber) {
            routeController.enableRoute(routeByNumber);
            storePreviousChangedObject(routeByNumber);
        }

    }

    private class LeftStateHandler extends NumberControlActionHandler {

        @Override
        protected void doPerformStateAction(TurnoutController turnoutController, Turnout turnout) {
            if (turnout.isLinkedToRoute()) {
                routeController.enableRoute(routeManager.getRouteByNumber(turnout.getLinkedRouteNumber()));
            } else {
                turnoutController.setCurvedLeft(turnout);
            }
            storePreviousChangedObject(turnout);
        }

        @Override
        protected void doPerformStateAction(RouteController routeController, Route routeByNumber) {
        }

    }

    private class StraightStateHandler extends NumberControlActionHandler {

        @Override
        protected void doPerformStateAction(TurnoutController turnoutController, Turnout turnout) {
            if (turnout.isLinkedToRoute()) {
                routeController.disableRoute(routeManager.getRouteByNumber(turnout.getLinkedRouteNumber()));
            } else {
                turnoutController.setStraight(turnout);
            }
        }

        @Override
        protected void doPerformStateAction(RouteController routeController, Route routeByNumber) {
        }

    }

    private class RightStateHandler extends NumberControlActionHandler {

        @Override
        protected void doPerformStateAction(TurnoutController turnoutController, Turnout turnout) {
            if (turnout.isLinkedToRoute()) {
                routeController.enableRoute(routeManager.getRouteByNumber(turnout.getLinkedRouteNumber()));
            } else {
                turnoutController.setCurvedRight(turnout);
            }
            storePreviousChangedObject(turnout);
        }

        @Override
        protected void doPerformStateAction(RouteController routeController, Route routeByNumber) {
        }

    }

    private class PeriodButtonHandler implements View.OnClickListener {
        @Override
        public void onClick(View v) {


            switch (numberControlState) {

                case TURNOUT:
                    if (StringUtils.isBlank(enteredNumberKeys)) {
                        numberControlState = NumberControlState.ROUTE;
                        routeIndicator.setVisibility(View.VISIBLE);
                    }
                    break;
                case ROUTE:
                    if (!StringUtils.isBlank(enteredNumberKeys)) {
                        numberControlState = NumberControlState.LOCOMOTIVE_FUNCTION;
                        routeIndicator.setVisibility(View.INVISIBLE);
                    }
                    break;
                case LOCOMOTIVE_FUNCTION:
                    numberControlState = NumberControlState.TURNOUT;
                    routeIndicator.setVisibility(View.INVISIBLE);
                    break;
            }
        }

    }
}
