package edu.orangecoastcollege.cs273.vnguyen629.flagquiz;

import android.os.Bundle;
import android.support.v4.app.Fragment;

/**
 * SettingsActivityFragment is a subclass of PreferenceFragment for managing
 * the app settings, such as number of guesses and regions to select flags from.
 */
public class SettingsActivityFragment extends Fragment {
    // creates preferences GUI from preferences.xml file in res/xml
    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        addPreferencesFromResource(R.xml.preferences); // load from XML
    }

    /*
    public SettingsActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }
    */
}
