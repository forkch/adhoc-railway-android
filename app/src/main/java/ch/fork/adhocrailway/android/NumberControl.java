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
 * {@link NumberControl.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link NumberControl#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NumberControl extends Fragment {

    private StringBuffer enteredNumberKeys = new StringBuffer();
    private TextView currentNumber;

    private OnFragmentInteractionListener mListener;
    private AdHocRailwayApplication adHocRailwayApplication;
    private View fragmentView;

    public NumberControl() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment NumberControl.
     */
    // TODO: Rename and change types and number of parameters
    public static NumberControl newInstance() {
        NumberControl fragment = new NumberControl();
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
        defaultStateButton.setOnClickListener(new View.OnClickListener() {
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
                        TurnoutController srcpTurnoutControlAdapter = adHocRailwayApplication.getTurnoutController();
                        srcpTurnoutControlAdapter.setDefaultState(turnoutByNumber);
                        resetNumbers();
                        return null;
                    }
                };
                asyncTask.execute();
            }
        });
        Button nonDefaultStateButton = (Button) fragmentView.findViewById(R.id.turnoutButtonNonDefault);
        nonDefaultStateButton.setOnClickListener(new View.OnClickListener() {
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
                        TurnoutController srcpTurnoutControlAdapter = adHocRailwayApplication.getTurnoutController();
                        srcpTurnoutControlAdapter.setNonDefaultState(turnoutByNumber);
                        resetNumbers();
                        return null;
                    }
                };
                asyncTask.execute();
            }
        });

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

}
