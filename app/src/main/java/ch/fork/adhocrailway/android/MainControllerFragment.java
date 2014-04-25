package ch.fork.adhocrailway.android;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;


public class MainControllerFragment extends Fragment implements NumberControlFragment.OnFragmentInteractionListener, LocomotiveControlFragment.OnFragmentInteractionListener {

    private OnFragmentInteractionListener mListener;
    private View fragmentView;
    private int number;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fragmentView = inflater.inflate(R.layout.fragment_main_controller, container, false);

        initFragments();

        return fragmentView;
    }

    private void initFragments() {

        FrameLayout numberControlContainer = (FrameLayout) fragmentView.findViewById(R.id.numberControlContainer);
        numberControlContainer.removeAllViews();

        FrameLayout locomotiveControlContainer = (FrameLayout) fragmentView.findViewById(R.id.locomotiveControlContainer);
        locomotiveControlContainer.removeAllViews();

        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        NumberControlFragment numberControl = NumberControlFragment.newInstance();
        fragmentTransaction.add(R.id.numberControlContainer, numberControl);

        LocomotiveControlFragment locomotiveControlFragment = LocomotiveControlFragment.newInstance(number);
        fragmentTransaction.add(R.id.locomotiveControlContainer, locomotiveControlFragment);

        fragmentTransaction.commit();
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
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
    }

}
