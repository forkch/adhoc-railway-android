package ch.fork.adhocrailway.android;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import ch.fork.AdHocRailway.model.locomotives.Locomotive;
import ch.fork.AdHocRailway.model.locomotives.LocomotiveGroup;


public class LocomotiveSelectActivity extends ListActivity {

    private List<Locomotive> locomotives;
    private SortedSet<Locomotive> sortedLocomotives;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locomotive_select);

        ListView listView = getListView();

        final AdHocRailwayApplication adHocRailwayApplication = (AdHocRailwayApplication) getApplication();
        sortedLocomotives = new TreeSet<Locomotive>();

        SortedSet<LocomotiveGroup> locomotiveGroups = adHocRailwayApplication.getLocomotiveGroups();
        if (locomotiveGroups != null) {
            for (LocomotiveGroup locomotiveGroup : locomotiveGroups) {
                sortedLocomotives.addAll(locomotiveGroup.getLocomotives());
            }
        }
        locomotives = new ArrayList<Locomotive>(sortedLocomotives);

        LocomotiveListAdapter locomotiveListAdapter = new LocomotiveListAdapter(this, locomotives);

        setListAdapter(locomotiveListAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                //adHocRailwayApplication.setSelectedLocomotive(locomotives.get(position));
                Intent intent = getIntent();
                intent.putExtra("selectedLocomotive", locomotives.get(position));
                if (getParent() == null) {
                    setResult(RESULT_OK, intent);
                } else {
                    getParent().setResult(RESULT_OK, intent);
                }
                finish();
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
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
