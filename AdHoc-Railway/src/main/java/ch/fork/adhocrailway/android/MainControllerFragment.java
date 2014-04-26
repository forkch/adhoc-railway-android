package ch.fork.adhocrailway.android;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerTitleStrip;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedList;

import ch.fork.AdHocRailway.controllers.RouteController;
import ch.fork.AdHocRailway.controllers.TurnoutController;
import ch.fork.AdHocRailway.model.locomotives.Locomotive;
import ch.fork.AdHocRailway.model.turnouts.Route;
import ch.fork.AdHocRailway.model.turnouts.Turnout;


public class MainControllerFragment extends Fragment implements NumberControlFragment.OnFragmentInteractionListener, LocomotiveControlFragment.OnFragmentInteractionListener {

    private OnFragmentInteractionListener mListener;
    private View fragmentView;
    private int number;
    private AdHocRailwayApplication adHocRailwayApplication;
    private Locomotive selectedLocomotive;
    private LinearLayout selectedLocomotiveView;
    private StringBuffer enteredNumberKeys = new StringBuffer();
    private TextView currentNumber;
    private NumberControlState numberControlState = NumberControlState.TURNOUT;
    private TextView routeIndicator;
    private Deque<Turnout> previousTurnouts = new ArrayDeque<Turnout>();

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
        currentNumber = (TextView) fragmentView.findViewById(R.id.currentNumber);
        routeIndicator = (TextView) fragmentView.findViewById(R.id.routeIndicator);

        selectedLocomotiveView = (LinearLayout) fragmentView.findViewById(R.id.selectedLocomotive);
        initNumberControlEventHandling();
        initEventHandling();

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

    private void initEventHandling() {

        selectedLocomotiveView.setOnClickListener(new SelectLocomotiveListener());

        SeekBar locomotive1Seekbar = (SeekBar) fragmentView.findViewById(R.id.locomotive1Speed);
        locomotive1Seekbar.setOnSeekBarChangeListener(new Locomotive1SpeedListener());

        Button directionButton = (Button) fragmentView.findViewById(R.id.locomotive1Direction);
        directionButton.setOnClickListener(new Locomotive1DirectionListener());

        Button stopButton = (Button) fragmentView.findViewById(R.id.locomotive1Stop);
        stopButton.setOnClickListener(new Locomotive1StopListener());

        Button emergencyStopButton = (Button) fragmentView.findViewById(R.id.locomotiveEmergencyStop);
        emergencyStopButton.setOnClickListener(new EmergencyStopListener());
    }

    private void updateSelectedLocomotive() {
        TextView label = (TextView) selectedLocomotiveView.findViewById(R.id.label);
        ImageView imageView = (ImageView) selectedLocomotiveView.findViewById(R.id.icon);

        if (selectedLocomotive != null) {
            label.setText(selectedLocomotive.getName());
            ImageHelper.fillImageViewFromBase64ImageString(imageView, selectedLocomotive.getImageBase64());
            mListener.onLocomotiveSelected();
            adHocRailwayApplication.getLocomotiveController().activateLoco(selectedLocomotive);

        } else {
            label.setText("select locomotive");
            imageView.setImageBitmap(null);
        }
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

            AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {

                @Override
                protected Void doInBackground(Void... params) {
                    adHocRailwayApplication.getLocomotiveController().emergencyStop(selectedLocomotive);
                    return null;
                }
            };
            asyncTask.execute();
        }
    }

    private class Locomotive1SpeedListener implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onProgressChanged(SeekBar seekBar, final int progress, boolean fromUser) {
            if (selectedLocomotive == null) {
                return;
            }

            AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {

                @Override
                protected Void doInBackground(Void... params) {
                    adHocRailwayApplication.getLocomotiveController().setSpeed(selectedLocomotive, progress, selectedLocomotive.getCurrentFunctions());
                    return null;
                }
            };
            asyncTask.execute();
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

            AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {

                @Override
                protected Void doInBackground(Void... params) {
                    adHocRailwayApplication.getLocomotiveController().toggleDirection(selectedLocomotive);
                    return null;
                }
            };
            asyncTask.execute();
        }
    }

    private class Locomotive1StopListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (selectedLocomotive == null) {
                return;
            }
            AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {

                @Override
                protected Void doInBackground(Void... params) {
                    adHocRailwayApplication.getLocomotiveController().setSpeed(selectedLocomotive, 0, selectedLocomotive.getCurrentFunctions());
                    return null;
                }
            };
            asyncTask.execute();
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

        @Override
        public void onClick(View v) {
            AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {

                @Override
                protected Void doInBackground(Void... params) {
                    int enteredNumber = getEnteredNumber();
                    if (enteredNumber == -1) {
                        return null;
                    }
                    if (numberControlState == NumberControlState.TURNOUT) {
                        Turnout turnoutByNumber = adHocRailwayApplication.getTurnoutManager().getTurnoutByNumber(enteredNumber);
                        if (turnoutByNumber == null) {
                            resetNumbers();
                            return null;
                        }
                        TurnoutController turnoutController = adHocRailwayApplication.getTurnoutController();
                        doPerformStateAction(turnoutController, turnoutByNumber);
                        previousTurnouts.addFirst(turnoutByNumber);
                        updatePreviousTurnouts();
                    } else {
                        Route routeByNumber = adHocRailwayApplication.getRouteManager().getRouteByNumber(enteredNumber);
                        if (routeByNumber == null) {
                            resetNumbers();
                            return null;
                        }
                        RouteController routeController = adHocRailwayApplication.getRouteController();
                        doPerformStateAction(routeController, routeByNumber);
                    }
                    resetNumbers();
                    return null;
                }
            };
            asyncTask.execute();
        }

    }

    private void updatePreviousTurnouts() {
        fragmentView.post(new Runnable() {
            @Override
            public void run() {
                LinearLayout lastTurnouts = (LinearLayout) fragmentView.findViewById(R.id.lastTurnouts);
                lastTurnouts.removeAllViews();
                for (Turnout t : previousTurnouts) {
                    TextView v = new TextView(getActivity());
                    v.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 40);
                    v.setText(Integer.toString(t.getNumber()));
                    lastTurnouts.addView(v);
                }
            }
        });

    }

    private class DefaultStateHandler extends NumberControlActionHandler {
        @Override
        public void onClick(View v) {
            if (StringUtils.isBlank(enteredNumberKeys.toString())) {
                if (previousTurnouts.isEmpty()) {
                    return;
                }
                final Turnout turnout = previousTurnouts.removeFirst();
                if (turnout == null) {
                    return;
                }
                AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {

                    @Override
                    protected Void doInBackground(Void... params) {
                        doPerformStateAction(adHocRailwayApplication.getTurnoutController(), turnout);
                        updatePreviousTurnouts();
                        return null;
                    }
                };
                asyncTask.execute();
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
        }

        @Override
        protected void doPerformStateAction(RouteController routeController, Route routeByNumber) {
            routeController.enableRoute(routeByNumber);
        }
    }

    private class LeftStateHandler extends NumberControlActionHandler {

        @Override
        protected void doPerformStateAction(TurnoutController turnoutController, Turnout turnout) {

            turnoutController.setCurvedLeft(turnout);
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
