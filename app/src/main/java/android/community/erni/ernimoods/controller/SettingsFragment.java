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

        Preference prefLogout = (Preference) getPreferenceManager().findPreference("buttonLogout");
        prefLogout.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
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


        Preference prefSendFeedback = (Preference) getPreferenceManager().findPreference("buttonSendFeedback");
        prefSendFeedback.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference arg0) {

                // create an implicit intent to start the users default mail program

                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("message/rfc822");
                intent.putExtra(Intent.EXTRA_EMAIL, new String[] {"angus.long@erni.ch", "richard.bumann@erni.ch"});
                intent.putExtra(Intent.EXTRA_SUBJECT, "Feedback on Erni Moods Beta 1.");
                intent.putExtra(Intent.EXTRA_TEXT, "I would like to give the following feedback about the Erni Moods app.");

                startActivity(Intent.createChooser(intent, "Send Email"));

                return true;

            }
        });
    }
}
