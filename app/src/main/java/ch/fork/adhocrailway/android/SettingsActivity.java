package ch.fork.adhocrailway.android;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Created by fork on 4/24/14.
 */
public class SettingsActivity extends PreferenceActivity {

    public static final String KEY_ADHOC_SERVER_HOST = "adhocServerHost";
    public static final String KEY_SRCP_SERVER_HOST = "srcpServerHost";
    public static final String KEY_USE_DUMMY_SERVICES = "useDummyServices";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }
}
