package ch.fork.adhocrailway.android;

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

import ch.fork.AdHocRailway.controllers.TurnoutController;
import ch.fork.AdHocRailway.model.turnouts.Turnout;


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

    public NumberControlFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment NumberControlFragment.
     */
    // TODO: Rename and change types and number of parameters
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
        nonDefaultStateButton.setOnClickListener(new LefttStateHandler());

        Button straightStateButton = (Button) fragmentView.findViewById(R.id.turnoutButtonStraight);
        nonDefaultStateButton.setOnClickListener(new StraightStateHandler());

        Button rightStateButton = (Button) fragmentView.findViewById(R.id.turnoutButtonRight);
        nonDefaultStateButton.setOnClickListener(new RightStateHandler());

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
        return Integer.parseInt(enteredNumberKeys.toString());
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
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

    private abstract class TurnoutStateHandler implements View.OnClickListener {

        protected abstract void doPerformStateAction(TurnoutController srcpTurnoutControlAdapter, Turnout turnout);

        @Override
        public void onClick(View v) {
            AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {

                @Override
                protected Void doInBackground(Void... params) {
                    Turnout turnoutByNumber = adHocRailwayApplication.getTurnoutManager().getTurnoutByNumber(getEnteredNumber());
                    if (turnoutByNumber == null) {
                        resetNumbers();
                        return null;
                    }
                    TurnoutController turnoutController = adHocRailwayApplication.getTurnoutController();
                    doPerformStateAction(turnoutController, turnoutByNumber);
                    resetNumbers();
                    return null;
                }
            };
            asyncTask.execute();
        }
    }

    private class DefaultStateHandler extends TurnoutStateHandler {

        @Override
        protected void doPerformStateAction(TurnoutController turnoutController, Turnout turnout) {

            turnoutController.setDefaultState(turnout);
        }
    }

    private class NonDefaultStateHandler extends TurnoutStateHandler {

        @Override
        protected void doPerformStateAction(TurnoutController turnoutController, Turnout turnout) {

            turnoutController.setNonDefaultState(turnout);
        }
    }

    private class LefttStateHandler extends TurnoutStateHandler {

        @Override
        protected void doPerformStateAction(TurnoutController turnoutController, Turnout turnout) {

            turnoutController.setCurvedLeft(turnout);
        }
    }

    private class StraightStateHandler extends TurnoutStateHandler {

        @Override
        protected void doPerformStateAction(TurnoutController turnoutController, Turnout turnout) {

            turnoutController.setStraight(turnout);
        }
    }

    private class RightStateHandler extends TurnoutStateHandler {

        @Override
        protected void doPerformStateAction(TurnoutController turnoutController, Turnout turnout) {

            turnoutController.setCurvedRight(turnout);
        }
    }
}
