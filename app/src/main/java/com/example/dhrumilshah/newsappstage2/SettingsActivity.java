package com.example.dhrumilshah.newsappstage2;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity{

        private static Preference numberOfEnteries, filterBy;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
    }
    public static class NewsPortalProferenceFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener{
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings_main);

            numberOfEnteries = findPreference(getString(R.string.settings_display_entries_key));
            bindPreferenceSummaryToValue(numberOfEnteries);

            filterBy = findPreference(getString(R.string.settings_filter_key));
            bindPreferenceSummaryToValue(filterBy);
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();
            if(preference == numberOfEnteries) {
                int enteriesCheck;
                try {
                    enteriesCheck = Integer.parseInt(stringValue);
                    if (enteriesCheck < 5 || enteriesCheck > 50) {
                        stringValue = getString(R.string.settings_display_entries_default);
                    }
                } catch (NumberFormatException e) {
                    stringValue = getString(R.string.settings_display_entries_default);
                }
                preference.setSummary(stringValue);
            }
            if(preference == filterBy) {
                if (preference instanceof ListPreference) {
                    ListPreference listPreference = (ListPreference) preference;
                    int prefIndex = listPreference.findIndexOfValue(stringValue);
                    if (prefIndex >= 0) {
                        CharSequence[] labels = listPreference.getEntries();
                        preference.setSummary(labels[prefIndex]);
                    }
                } else {
                    preference.setSummary(stringValue);
                }
            }
            return true;
        }

        private void bindPreferenceSummaryToValue(Preference preference) {
            preference.setOnPreferenceChangeListener(this);
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(preference.getContext());
            String preferenceString = preferences.getString(preference.getKey(), "");
            onPreferenceChange(preference, preferenceString);
        }
    }
}
