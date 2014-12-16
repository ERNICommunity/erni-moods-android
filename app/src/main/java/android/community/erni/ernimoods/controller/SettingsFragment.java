package android.community.erni.ernimoods.controller;

import android.community.erni.ernimoods.R;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

/**
 * Shows the preferences screen. Gets its info from /xml/preferences.xml
 * Created by gus on 24.08.14.
 */
public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        Preference button = (Preference) getPreferenceManager().findPreference("buttonLogout");
        button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference arg0) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("pref_username", "");
                editor.commit();
                editor.putString("pref_password", "");
                editor.commit();

                Intent i = new Intent(getActivity(), EntryPoint.class);
                startActivity(i);
                return true;

            }
        });

        Preference button1 = (Preference) getPreferenceManager().findPreference("buttonBack");
        button1.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference arg0) {

                Intent i = new Intent(getActivity(), EntryPoint.class);
                startActivity(i);
                return true;

            }
        });
    }
}
