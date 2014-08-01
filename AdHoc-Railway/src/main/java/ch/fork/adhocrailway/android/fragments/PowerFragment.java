package ch.fork.adhocrailway.android.fragments;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.common.collect.Lists;

import java.util.List;

import javax.inject.Inject;

import ch.fork.AdHocRailway.controllers.PowerChangeListener;
import ch.fork.AdHocRailway.controllers.PowerController;
import ch.fork.AdHocRailway.model.power.Booster;
import ch.fork.AdHocRailway.model.power.BoosterState;
import ch.fork.AdHocRailway.model.power.PowerSupply;
import ch.fork.adhocrailway.android.AdHocRailwayApplication;
import ch.fork.adhocrailway.android.R;

public class PowerFragment extends Fragment implements PowerChangeListener {
    @Inject
    PowerController powerController;
    @Inject
    AdHocRailwayApplication adHocRailwayApplication;
    private OnPowerFragmentInteractionListener mListener;
    private View fragmentView;
    private List<Button> boosterButtons = Lists.newArrayList();


    public PowerFragment() {
        // Required empty public constructor
    }

    public static PowerFragment newInstance() {
        PowerFragment fragment = new PowerFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fragmentView = inflater.inflate(R.layout.fragment_power, container, false);
        initEventHandling();
        return fragmentView;
    }

    private void initEventHandling() {
        for (int i = 0; i < 8; i++) {
            Button boosterButton = (Button) fragmentView.findViewById(getResources().getIdentifier("booster" + i, "id", getActivity().getPackageName()));
            boosterButton.setOnClickListener(new BoosterButtonListener(i));
            boosterButtons.add(boosterButton);
        }

        Button boosterAllOnButton = (Button) fragmentView.findViewById(R.id.boosterAllOnButton);
        boosterAllOnButton.setOnClickListener(new BoosterAllOnListener());

        Button boosterAllOffButton = (Button) fragmentView.findViewById(R.id.boosterAllOffButton);
        boosterAllOffButton.setOnClickListener(new BoosterAllOffListener());

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnPowerFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        adHocRailwayApplication = (AdHocRailwayApplication) getActivity().getApplication();
        adHocRailwayApplication.getPowerController().addPowerChangeListener(this);
        powerChanged(adHocRailwayApplication.getPowerController().getPowerSupply(1));
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void powerChanged(final PowerSupply supply) {
        fragmentView.post(new Runnable() {
            @Override
            public void run() {

                if (supply == null) {
                    return;
                }
                for (Booster booster : supply.getBoosters()) {
                    Button button = boosterButtons.get(booster.getBoosterNumber());
                    if (booster.getState() == BoosterState.ACTIVE) {
                        button.setBackgroundResource(R.drawable.bring_button_primary);
                    } else {
                        button.setBackgroundResource(R.drawable.bring_button_alert);
                    }
                }
            }
        });

    }

    public interface OnPowerFragmentInteractionListener {
    }

    private class BoosterButtonListener implements View.OnClickListener {
        private int boosterNumber;

        public BoosterButtonListener(int boosterNumber) {
            this.boosterNumber = boosterNumber;
        }

        @Override
        public void onClick(View v) {
            AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    powerController.toggleBooster(adHocRailwayApplication.getPowerSupply().getBooster(boosterNumber));
                    return null;
                }

            };
            asyncTask.execute();
        }
    }

    private class BoosterAllOnListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    PowerController powerController = adHocRailwayApplication.getPowerController();
                    powerController.powerOn(adHocRailwayApplication.getPowerSupply());
                    return null;
                }

            };
            asyncTask.execute();
        }
    }

    private class BoosterAllOffListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    PowerController powerController = adHocRailwayApplication.getPowerController();
                    powerController.powerOff(adHocRailwayApplication.getPowerSupply());
                    return null;
                }

            };
            asyncTask.execute();
        }
    }
}