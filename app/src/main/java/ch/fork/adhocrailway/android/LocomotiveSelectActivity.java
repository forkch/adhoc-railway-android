package ch.fork.adhocrailway.android;

import android.app.Activity;
import android.app.ListActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import ch.fork.AdHocRailway.model.locomotives.Locomotive;
import ch.fork.AdHocRailway.model.locomotives.LocomotiveGroup;


public class LocomotiveSelectActivity extends ListActivity {

    private List<Locomotive> locomotives;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locomotive_select);

        ListView listView = getListView();

        final AdHocRailwayApplication adHocRailwayApplication = (AdHocRailwayApplication) getApplication();
        locomotives = new ArrayList<Locomotive>();

        for (LocomotiveGroup locomotiveGroup : adHocRailwayApplication.getLocomotiveGroups()) {
                locomotives.addAll(locomotiveGroup.getLocomotives());
        }

        LocomotiveListAdapter locomotiveListAdapter = new LocomotiveListAdapter(this, locomotives);

        setListAdapter(locomotiveListAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                adHocRailwayApplication.setSelectedLocomotive(locomotives.get(position));
                onBackPressed();
            }
        });

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
