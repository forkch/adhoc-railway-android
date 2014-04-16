package ch.fork.adhocrailway.android;

import android.app.Activity;
import android.app.ListActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.List;

import ch.fork.AdHocRailway.model.locomotives.LocomotiveGroup;


public class LocomotiveSelectActivity extends ListActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locomotive_select);

        AdHocRailwayApplication adHocRailwayApplication = (AdHocRailwayApplication) getApplication();
        List<String> values = new ArrayList<String>();

        for (LocomotiveGroup locomotiveGroup : adHocRailwayApplication.getLocomotiveGroups()) {
            values.add(locomotiveGroup.getName());
        }
        ArrayAdapter<String> locomotivesArray = new ArrayAdapter<String>(this, R.layout.locomotive_row, R.id.label
                ,values);

        setListAdapter(locomotivesArray);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.locomotive_select, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
