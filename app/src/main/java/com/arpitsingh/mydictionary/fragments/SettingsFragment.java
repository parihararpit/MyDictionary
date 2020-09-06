package com.arpitsingh.mydictionary.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;

import androidx.annotation.Nullable;

import com.arpitsingh.mydictionary.R;

/**
 * Created by ARPIT SINGH
 * 18/10/19
 */


public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener{
    SharedPreferences sharedPreferences;

    @Override
    public void onStop() {
        super.onStop();
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings_preferences);
        PreferenceScreen preferenceScreen = getPreferenceScreen();
        if (preferenceScreen != null && preferenceScreen.getPreferenceCount() > 0) {
            Preference preference = preferenceScreen.getPreference(0);
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String val = sharedPreferences.getString(preference.getKey(),"");
            setPreferenceSummary( preference,val );
            sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        }
    }

    private void setPreferenceSummary(Preference preference, String value) {
        if (preference instanceof ListPreference) {
            // For list preferences, figure out the label of the selected value
            ListPreference listPreference = (ListPreference) preference;
            int prefIndex = listPreference.findIndexOfValue(value);
            if (prefIndex >= 0) {
                // Set the summary to that label
                listPreference.setSummary(listPreference.getEntries()[prefIndex]);
            }
        }

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference preference = findPreference(key);
        if (key.equals(getString(R.string.setting_fav_sort_key))){
            String val = sharedPreferences.getString(getString(R.string.setting_fav_sort_key),"");
            setPreferenceSummary(preference,val);
        }
    }
}
