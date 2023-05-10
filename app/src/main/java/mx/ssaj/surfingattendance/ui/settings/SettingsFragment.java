package mx.ssaj.surfingattendance.ui.settings;

import android.os.Bundle;
import androidx.preference.PreferenceFragmentCompat;
import mx.ssaj.surfingattendance.R;

public class SettingsFragment extends PreferenceFragmentCompat {

    // TODO: Add "Delay" as setting with a default of 120 secs
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);
    }
}