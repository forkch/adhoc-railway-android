package ch.fork.adhocrailway.android.fragments;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;

import ch.fork.AdHocRailway.controllers.RouteController;
import ch.fork.AdHocRailway.controllers.TurnoutController;
import ch.fork.AdHocRailway.model.turnouts.Route;
import ch.fork.AdHocRailway.model.turnouts.Turnout;
import ch.fork.adhocrailway.android.AdHocRailwayApplication;
import ch.fork.adhocrailway.android.R;
import ch.fork.adhocrailway.android.activities.ControllerActivity;


/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link NumberControlFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link NumberControlFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NumberControlFragment extends Fragment {

    private StringBuffer enteredNumberKeys = new StringBuffer();
    private TextView currentNumber;

    private OnFragmentInteractionListener mListener;
    private AdHocRailwayApplication adHocRailwayApplication;
    private View fragmentView;
    private NumberControlState numberControlState = NumberControlState.TURNOUT;

    public NumberControlFragment() {
    }

    public static NumberControlFragment newInstance() {
        NumberControlFragment fragment = new NumberControlFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fragmentView = inflater.inflate(R.layout.fragment_number_control, container, false);

        currentNumber = (TextView) fragmentView.findViewById(R.id.currentNumber);
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
        adHocRailwayApplication = (AdHocRailwayApplication) activity.getApplication();

    }

    private void initEventHandling() {

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

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    private void resetNumbers() {
        currentNumber.post(new Runnable() {
            @Override
            public void run() {
                enteredNumberKeys = new StringBuffer();
                currentNumber.setText("---");
            }
        });
    }

    private int getEnteredNumber() {
        if (StringUtils.isBlank(enteredNumberKeys.toString())) {
            return -1;
        }
        return Integer.parseInt(enteredNumberKeys.toString());
    }

    private enum NumberControlState {
        TURNOUT, ROUTE, LOCOMOTIVE_FUNCTION;
    }

    public interface OnFragmentInteractionListener {
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

    private class DefaultStateHandler extends NumberControlActionHandler {

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
                    }
                    break;
                case ROUTE:
                    if (!StringUtils.isBlank(enteredNumberKeys)) {
                        numberControlState = NumberControlState.LOCOMOTIVE_FUNCTION;
                    }
                    break;
                case LOCOMOTIVE_FUNCTION:
                    numberControlState = NumberControlState.TURNOUT;
                    break;
            }
        }
    }
}
