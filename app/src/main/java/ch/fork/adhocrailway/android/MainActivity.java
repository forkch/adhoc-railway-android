package ch.fork.adhocrailway.android;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import ch.fork.AdHocRailway.model.locomotives.Locomotive;

public class MainActivity extends Activity {

    private AdHocRailwayApplication adHocRailwayApplication;

    private StringBuffer enteredNumberKeys = new StringBuffer();
    private TextView currentNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        adHocRailwayApplication = (AdHocRailwayApplication) getApplication();

        currentNumber = (TextView) findViewById(R.id.currentNumber);

        initEventHandling();

    }

    private void initEventHandling() {
        SeekBar locomotive1Seekbar = (SeekBar) findViewById(R.id.locomotive1Speed);
        locomotive1Seekbar.setOnSeekBarChangeListener(new Locomotive1SpeedListener());

        Button directionButton = (Button) findViewById(R.id.locomotive1Direction);
        directionButton.setOnClickListener(new Locomotive1DirectionListener());

        Button stopButton = (Button) findViewById(R.id.locomotive1Stop);
        stopButton.setOnClickListener(new Locomotive1StopListener());

        for (int i = 0; i < 10; i++) {
            Button turnoutButton = (Button) findViewById(getResources().getIdentifier("turnoutButton" + i, "id", getPackageName()));
            turnoutButton.setOnClickListener(new NumberButtonClickListener(i));

        }

    }


    @Override
    protected void onResume() {
        super.onResume();
        adHocRailwayApplication = (AdHocRailwayApplication) getApplication();
        FrameLayout viewById = (FrameLayout) findViewById(R.id.selectedLocomotive);

        LinearLayout locomotiveRowView = (LinearLayout) getLayoutInflater().inflate(R.layout.locomotive_row, null);
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

    public void onSelectLocomotiveClick(View view) {
        Intent selectLocomotiveIntent = new Intent(this, LocomotiveSelectActivity.class);
        startActivity(selectLocomotiveIntent);
    }

    private void resetNumbers() {
        enteredNumberKeys = new StringBuffer();
        currentNumber.setText("---");
    }

    private class NumberButtonClickListener implements View.OnClickListener {
        private int number;

        public NumberButtonClickListener(int number) {
            this.number = number;
        }

        @Override
        public void onClick(View v) {
            enteredNumberKeys.append(number);
            final int currentEnteredNumber = Integer.parseInt(enteredNumberKeys.toString());
            Log.i(MainActivity.class.getSimpleName(), "current entered number: " + currentEnteredNumber);
            if (currentEnteredNumber > 999) {
                resetNumbers();
                return;
            }
            currentNumber.setText("" + currentEnteredNumber);
        }

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
                    adHocRailwayApplication.getSrcpLocomotiveControlAdapter().setSpeed(selectedLocomotive, progress, selectedLocomotive.getCurrentFunctions());
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
                    adHocRailwayApplication.getSrcpLocomotiveControlAdapter().toggleDirection(selectedLocomotive);
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
                    adHocRailwayApplication.getSrcpLocomotiveControlAdapter().setSpeed(selectedLocomotive, 0, selectedLocomotive.getCurrentFunctions());
                    return null;
                }
            };
            asyncTask.execute();
        }
    }
}
