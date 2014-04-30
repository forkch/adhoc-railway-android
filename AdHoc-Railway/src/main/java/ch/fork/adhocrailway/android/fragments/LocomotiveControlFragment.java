package ch.fork.adhocrailway.android.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.ButterKnife;
import butterknife.InjectView;
import ch.fork.AdHocRailway.model.locomotives.Locomotive;
import ch.fork.adhocrailway.android.AdHocRailwayApplication;
import ch.fork.adhocrailway.android.R;
import ch.fork.adhocrailway.android.activities.LocomotiveSelectActivity;
import ch.fork.adhocrailway.android.utils.ImageHelper;


public class LocomotiveControlFragment extends Fragment {
    @InjectView(R.id.functionContainer)
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
    private OnFragmentInteractionListener mListener;
    private View fragmentView;
    private AdHocRailwayApplication adHocRailwayApplication;
    private int number;
    private Locomotive selectedLocomotive;


    public LocomotiveControlFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment LocomotiveControlFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LocomotiveControlFragment newInstance(int number) {
        LocomotiveControlFragment fragment = new LocomotiveControlFragment();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fragmentView = inflater.inflate(R.layout.fragment_locomotive_control, container, false);
        ButterKnife.inject(this, fragmentView);

        initEventHandling();

        return fragmentView;

    }

    private void initEventHandling() {

        selectedLocomotiveView.setOnClickListener(new SelectLocomotiveListener());

        locomotive1Seekbar.setOnSeekBarChangeListener(new Locomotive1SpeedListener());

        directionButton.setOnClickListener(new Locomotive1DirectionListener());

        stopButton.setOnClickListener(new Locomotive1StopListener());

        emergencyStopButton.setOnClickListener(new EmergencyStopListener());

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

    private void updateSelectedLocomotive() {
        TextView label = (TextView) selectedLocomotiveView.findViewById(R.id.label);
        ImageView imageView = (ImageView) selectedLocomotiveView.findViewById(R.id.icon);

        if (selectedLocomotive != null) {
            label.setText(selectedLocomotive.getName());
            ImageHelper.fillImageViewFromBase64ImageString(imageView, selectedLocomotive.getImageBase64());

            adHocRailwayApplication.getLocomotiveController().activateLoco(selectedLocomotive);

        } else {
            label.setText("no locomotive selected");
            imageView.setImageBitmap(null);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void setSelectedLocomotive(Locomotive selectedLocomotive) {
        this.selectedLocomotive = selectedLocomotive;
        updateSelectedLocomotive();
    }

    public interface OnFragmentInteractionListener {
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
}
