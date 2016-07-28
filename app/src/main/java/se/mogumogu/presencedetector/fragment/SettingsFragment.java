package se.mogumogu.presencedetector.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;

import se.mogumogu.presencedetector.NumberPickerPreference;
import se.mogumogu.presencedetector.R;

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        for (int i = 0; i < getPreferenceScreen().getPreferenceCount(); ++i) {
            Preference preference = getPreferenceScreen().getPreference(i);
            if (preference instanceof PreferenceGroup) {
                PreferenceGroup preferenceGroup = (PreferenceGroup) preference;
                for (int j = 0; j < preferenceGroup.getPreferenceCount(); ++j) {
                    Preference singlePref = preferenceGroup.getPreference(j);
                    updatePreference(singlePref, singlePref.getKey());
                }
            } else {
                updatePreference(preference, preference.getKey());
            }
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        updatePreference(findPreference(key), key);
    }

    private void updatePreference(Preference preference, String key) {

        SharedPreferences sharedPreferences = getPreferenceManager().getSharedPreferences();

        if (preference != null) {

            if (preference instanceof ListPreference) {

                ListPreference listPreference = (ListPreference) preference;
                listPreference.setSummary(listPreference.getEntry());

            } else if (preference instanceof NumberPickerPreference) {

                NumberPickerPreference numberPickerPreference = (NumberPickerPreference) preference;
                final String currentRssiThreshold = sharedPreferences.getString(key, getResources().getString(R.string.rssi_threshold_max));
                preference.setSummary(currentRssiThreshold);
                sharedPreferences.edit().putString("preference_rssi_threshold_key", currentRssiThreshold).apply();

            } else {

                if (key.contains("user_registration")) {

                    preference.setSummary(sharedPreferences.getString(
                            key, getResources().getString(R.string.default_user_registration_url)));

                } else if (key.contains("beacon_subscription")) {

                    preference.setSummary(sharedPreferences.getString(
                            key, getResources().getString(R.string.default_beacon_subscription_url)));

                } else if (key.contains("in_range")) {

                    preference.setSummary(sharedPreferences.getString(
                            key, getResources().getString(R.string.default_in_range_notification_url)));

                } else if (key.contains("out_of_range")) {

                    preference.setSummary(sharedPreferences.getString(key, getResources().getString(R.string.default_out_of_range_notification_url)));

                }
            }
        }
    }
}
