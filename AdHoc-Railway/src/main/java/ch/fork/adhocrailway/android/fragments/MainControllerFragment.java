package ch.fork.adhocrailway.android.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import com.squareup.otto.Bus;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayDeque;
import java.util.Deque;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
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
import ch.fork.adhocrailway.android.utils.ImageHelper;


public class MainControllerFragment extends BaseFragment {

    private static final String TAG = MainControllerFragment.class.getSimpleName();
    LinearLayout functionContainer;
    @InjectView(R.id.locomotive1Speed)
    SeekBar locomotive1Seekbar;
    @InjectView(R.id.locomotive1Direction)
    Button directionButton;
    @InjectView(R.id.locomotive1Stop)
    Button stopButton;
    @InjectView(R.id.locomotiveEmergencyStop)
    Button emergencyStopButton;
    @InjectView(R.id.selectedLocomotive)
    LinearLayout selectedLocomotiveView;

    @Inject
    TurnoutManager turnoutManager;
    @Inject
    RouteManager routeManager;


    private OnFragmentInteractionListener mListener;
    private View fragmentView;
    private int number;
    private AdHocRailwayApplication adHocRailwayApplication;
    private Locomotive selectedLocomotive;
    private StringBuffer enteredNumberKeys = new StringBuffer();
    private TextView currentNumber;
    private NumberControlState numberControlState = NumberControlState.TURNOUT;
    private TextView routeIndicator;
    private Deque<Object> previousChangedObjects = new ArrayDeque<Object>();

    public MainControllerFragment() {
    }

    public static MainControllerFragment newInstance(int number) {
        MainControllerFragment fragment = new MainControllerFragment();
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
            selectedLocomotive = (Locomotive) data.getSerializableExtra("selectedLocomotive");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fragmentView = inflater.inflate(R.layout.fragment_main_controller, container, false);
        ButterKnife.inject(this, fragmentView);
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
        adHocRailwayApplication = (AdHocRailwayApplication) getActivity().getApplication();
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
        adHocRailwayApplication = (AdHocRailwayApplication) getActivity().getApplication();
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

        functionContainer = (LinearLayout) fragmentView.findViewById(R.id.functionContainer);

        if (functionContainer != null) {
            for (int i = 0; i < 5; i++) {
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
            ImageHelper.fillImageViewFromBase64ImageString(imageView, selectedLocomotive.getImageBase64());
            adHocRailwayApplication.getLocomotiveController().activateLoco(selectedLocomotive);

        } else {
            label.setText("select locomotive");
            imageView.setImageBitmap(null);
        }

        mListener.onLocomotiveSelected();
    }

    private void initNumberControlEventHandling() {

        for (int i = 0; i < 10; i++) {
            Button turnoutButton = (Button) fragmentView.findViewById(getResources().getIdentifier("turnoutButton" + i, "id", getActivity().getPackageName()));
            turnoutButton.setOnClickListener(new NumberButtonClickListener(i));
        }

        Button defaultStateButton = (Button) fragmentView.findViewById(R.id.turnoutButtonDefault);
        defaultStateButton.setOnClickListener(new DefaultStateHandler());

        Button nonDefaultStateButton = (Button) fragmentView.findViewById(R.id.turnoutButtonNonDefault);
        nonDefaultStateButton.setOnClickListener(new NonDefaultStateHandler());

        Button leftStateButton = (Button) fragmentView.findViewById(R.id.turnoutButtonLeft);
        leftStateButton.setOnClickListener(new LeftStateHandler());

        Button straightStateButton = (Button) fragmentView.findViewById(R.id.turnoutButtonStraight);
        straightStateButton.setOnClickListener(new StraightStateHandler());

        Button rightStateButton = (Button) fragmentView.findViewById(R.id.turnoutButtonRight);
        rightStateButton.setOnClickListener(new RightStateHandler());

        Button periodButton = (Button) fragmentView.findViewById(R.id.turnoutButtonPeriod);
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

    private enum NumberControlState {
        TURNOUT, ROUTE, LOCOMOTIVE_FUNCTION;
    }

    public interface OnFragmentInteractionListener {
        void onLocomotiveSelected();
    }

    private class EmergencyStopListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (selectedLocomotive == null) {
                return;
            }

            enqueueJob(new NetworkJob() {
                @Override
                public void onRun() throws Throwable {
                    adHocRailwayApplication.getLocomotiveController().emergencyStop(selectedLocomotive);
                    locomotive1Seekbar.setProgress(0);
                }
            });

        }
    }

    private class Locomotive1SpeedListener implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onProgressChanged(SeekBar seekBar, final int progress, boolean fromUser) {
            if (selectedLocomotive == null) {
                return;
            }
            enqueueJob(new NetworkJob() {
                @Override
                public void onRun() throws Throwable {
                    adHocRailwayApplication.getLocomotiveController().setSpeed(selectedLocomotive, progress, selectedLocomotive.getCurrentFunctions());
                }
            });
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
            if (selectedLocomotive == null) {
                return;
            }

            enqueueJob(new NetworkJob() {
                @Override
                public void onRun() throws Throwable {
                    adHocRailwayApplication.getLocomotiveController().toggleDirection(selectedLocomotive);
                }
            });
        }
    }

    private class Locomotive1StopListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (selectedLocomotive == null) {
                return;
            }
            enqueueJob(new NetworkJob() {
                @Override
                public void onRun() throws Throwable {
                    adHocRailwayApplication.getLocomotiveController().setSpeed(selectedLocomotive, 0, selectedLocomotive.getCurrentFunctions());
                    locomotive1Seekbar.setProgress(0);
                }
            });
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
                    RouteController routeController = adHocRailwayApplication.getRouteController();
                    doPerformStateAction(routeController, routeByNumber);
                }

                private void handleTurnoutChange(int enteredNumber) {
                    Turnout turnoutByNumber = turnoutManager.getTurnoutByNumber(enteredNumber);
                    if (turnoutByNumber == null) {
                        resetNumbers();
                        return;
                    }
                    TurnoutController turnoutController = adHocRailwayApplication.getTurnoutController();
                    doPerformStateAction(turnoutController, turnoutByNumber);
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
                                       doPerformStateAction(adHocRailwayApplication.getTurnoutController(), (Turnout) obj);
                                   } else {
                                       doPerformStateAction(adHocRailwayApplication.getRouteController(), (Route) obj);
                                   }
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
            turnoutController.setDefaultState(turnout);
        }

        @Override
        protected void doPerformStateAction(RouteController routeController, Route routeByNumber) {
            routeController.disableRoute(routeByNumber);
        }
    }

    private class NonDefaultStateHandler extends NumberControlActionHandler {

        @Override
        protected void doPerformStateAction(TurnoutController turnoutController, Turnout turnout) {
            turnoutController.setNonDefaultState(turnout);
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
            turnoutController.setCurvedLeft(turnout);
            storePreviousChangedObject(turnout);
        }

        @Override
        protected void doPerformStateAction(RouteController routeController, Route routeByNumber) {
        }
    }

    private class StraightStateHandler extends NumberControlActionHandler {

        @Override
        protected void doPerformStateAction(TurnoutController turnoutController, Turnout turnout) {
            turnoutController.setStraight(turnout);
        }

        @Override
        protected void doPerformStateAction(RouteController routeController, Route routeByNumber) {
        }
    }

    private class RightStateHandler extends NumberControlActionHandler {

        @Override
        protected void doPerformStateAction(TurnoutController turnoutController, Turnout turnout) {
            turnoutController.setCurvedRight(turnout);
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

    private class FunctionButtonClickListener implements View.OnClickListener {
        private int functionNumber;

        public FunctionButtonClickListener(int functionNumber) {
            this.functionNumber = functionNumber;
        }

        @Override
        public void onClick(View v) {
            if (selectedLocomotive == null) {
                return;
            }

            enqueueJob(new NetworkJob() {
                @Override
                public void onRun() throws Throwable {
                    LocomotiveController locomotiveController = adHocRailwayApplication.getLocomotiveController();
                    boolean currentFunctionValue = selectedLocomotive.getCurrentFunctions()[functionNumber];
                    locomotiveController.setFunction(selectedLocomotive, functionNumber, !currentFunctionValue, 0);
                }
            });
        }
    }
}
