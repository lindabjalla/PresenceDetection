package se.mogumogu.presencedetector.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.util.Log;

import se.mogumogu.presencedetector.NumberPickerPreference;
import se.mogumogu.presencedetector.R;

public final class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = SettingsFragment.class.getSimpleName();

    @Override
    public void onCreate(final Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onResume() {

        super.onResume();

        for (int i = 0; i < getPreferenceScreen().getPreferenceCount(); ++i) {

            final Preference preference = getPreferenceScreen().getPreference(i);

            if (preference instanceof PreferenceGroup) {

                final PreferenceGroup preferenceGroup = (PreferenceGroup) preference;

                for (int j = 0; j < preferenceGroup.getPreferenceCount(); ++j) {

                    final Preference singlePref = preferenceGroup.getPreference(j);
                    updatePreference(singlePref, singlePref.getKey());
                }

            } else {

                updatePreference(preference, preference.getKey());
            }
        }
    }

    @Override
    public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {

        updatePreference(findPreference(key), key);
    }

    private void updatePreference(final Preference preference, final String key) {

        final SharedPreferences sharedPreferences = getPreferenceManager().getSharedPreferences();

        if (preference != null) {

            if (preference instanceof ListPreference) {

                final ListPreference listPreference = (ListPreference) preference;
                listPreference.setSummary(listPreference.getEntry() + getResources().getString(R.string.after_out_of_range));

            } else if (preference instanceof NumberPickerPreference) {

                final String currentRssiThreshold = sharedPreferences.getString(key, getResources().getString(R.string.rssi_threshold_default));
                Log.d("currentRssiThreshold", currentRssiThreshold);
                preference.setSummary(currentRssiThreshold);
                sharedPreferences.edit().putString(key, currentRssiThreshold).apply();

            } else if (preference instanceof EditTextPreference) {

                preference.setSummary(sharedPreferences.getString(key, getResources().getString(R.string.default_server_url)));

            } else {

                Log.d(TAG, "preference did not match");
            }
        }
    }
}
