package ch.fork.adhocrailway.android;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import ch.fork.AdHocRailway.model.locomotives.Locomotive;


/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link LocomotiveControlFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link LocomotiveControlFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LocomotiveControlFragment extends Fragment {
    private OnFragmentInteractionListener mListener;
    private View fragmentView;
    private AdHocRailwayApplication adHocRailwayApplication;

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
    public static LocomotiveControlFragment newInstance() {
        LocomotiveControlFragment fragment = new LocomotiveControlFragment();
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
        fragmentView = inflater.inflate(R.layout.fragment_locomotive_control, container, false);
        initEventHandling();

        return fragmentView;

    }

    private void initEventHandling() {
        SeekBar locomotive1Seekbar = (SeekBar) fragmentView.findViewById(R.id.locomotive1Speed);
        locomotive1Seekbar.setOnSeekBarChangeListener(new Locomotive1SpeedListener());

        Button directionButton = (Button) fragmentView.findViewById(R.id.locomotive1Direction);
        directionButton.setOnClickListener(new Locomotive1DirectionListener());

        Button stopButton = (Button) fragmentView.findViewById(R.id.locomotive1Stop);
        stopButton.setOnClickListener(new Locomotive1StopListener());

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

        FrameLayout viewById = (FrameLayout) fragmentView.findViewById(R.id.selectedLocomotive);
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        LinearLayout locomotiveRowView = (LinearLayout) inflater.inflate(R.layout.locomotive_row, null);
        TextView label = (TextView) locomotiveRowView.findViewById(R.id.label);
        ImageView imageView = (ImageView) locomotiveRowView.findViewById(R.id.icon);

        viewById.removeAllViews();
        viewById.addView(locomotiveRowView);
        Locomotive selectedLocomotive = adHocRailwayApplication.getSelectedLocomotive();
        if (selectedLocomotive != null) {
            label.setText(selectedLocomotive.getName());
            ImageHelper.fillImageViewFromBase64ImageString(imageView, selectedLocomotive.getImageBase64());
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

    private class Locomotive1SpeedListener implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onProgressChanged(SeekBar seekBar, final int progress, boolean fromUser) {
            final Locomotive selectedLocomotive = adHocRailwayApplication.getSelectedLocomotive();
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
            final Locomotive selectedLocomotive = adHocRailwayApplication.getSelectedLocomotive();
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
            final Locomotive selectedLocomotive = adHocRailwayApplication.getSelectedLocomotive();
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

}
