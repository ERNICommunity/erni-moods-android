package android.community.erni.ernimoods.controller;

import android.app.Activity;
import android.app.Fragment;
import android.community.erni.ernimoods.R;
import android.community.erni.ernimoods.model.Mood;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

/** This fragment is used to enter your current mood
 *
 */
public class MyMoodFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_mood, container, false);

        registerClickListener(view, R.id.button_VeryHappy, R.integer.int_VeryHappy);
        registerClickListener(view, R.id.button_Good, R.integer.int_Good);
        registerClickListener(view, R.id.button_SoSoLaLa, R.integer.int_SosoLala);
        registerClickListener(view, R.id.button_NotAmused, R.integer.int_NotAmused);
        registerClickListener(view, R.id.button_VeryMoody,R.integer.int_VeryMoody);

        return view;
    }

    private void registerClickListener(View view, int buttonId, final int moodId) {
        view.findViewById(buttonId).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setMood(moodId);
            }
        });
    }

    private void setMood(int moodId) {

        //Retrieve username from application shared preferences
        Resources res = getResources();
        SharedPreferences myPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getBaseContext());

        String comment = "These are test entries for test purpose";
        String user = myPreferences.getString("pref_username", "nothing");
        // TODO GRA: add location
        Location location = null;
        Mood myCurrentMood = new Mood(user, location, comment, moodId);

        //Show Mood with toast
        Context context = getActivity().getApplicationContext();
        String toastText = myCurrentMood.toString() + " - " + "You pressed a mood button!";
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, toastText, duration);
        toast.show();

        // TODO GRA: post mood
    }



}
