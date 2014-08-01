package ch.fork.adhocrailway.android.activities;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.TextView;

import ch.fork.adhocrailway.android.R;


/**
 * Created by fork on 29.05.14.
 */
public class ConnectActivityTest extends ActivityInstrumentationTestCase2<ConnectActivity> {

    ConnectActivity activity;

    public ConnectActivityTest() {
        super(ConnectActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getInstrumentation().getTargetContext());
        settings.edit().clear().commit();
        activity = getActivity();
    }

    public void testServerTextfields() {
        TextView adhocServerHost = (TextView) activity.findViewById(R.id.adhocServerHostTextView);

    }
}
