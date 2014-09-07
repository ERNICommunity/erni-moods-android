package android.community.erni.ernimoods.controller;

import android.app.Activity;
import android.community.erni.ernimoods.R;
import android.community.erni.ernimoods.model.Mood;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Toast;

/** This activity is used to enter your current mood
 * Created by gus on 24.08.14.
 */
public class MyMoodActivity  extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_mood);
    }

    public void setMood(View v) {

        //Initialize Mood values
        String user = "None";
        Location location = null;
        String comment = "These are test entries for test purpose";
        Mood myCurrentMood = null;

        //Initialize toast message
        Context context = getApplicationContext();
        CharSequence text = "You pressed a mood button!";
        int duration = Toast.LENGTH_SHORT;

        //Retrieve username from application shared preferences
        Resources res = getResources();
        SharedPreferences myPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        user = myPreferences.getString("pref_username","nothing");

        //Depending on clicked Button create different mood
        switch (v.getId()) {
            case R.id.button_VeryHappy:
                text = getString(R.string.text_moodVeryHappy);
                myCurrentMood = new Mood(user, location, comment, res.getInteger(R.integer.int_VeryHappy));
                break;
            case R.id.button_Good:
                text = getString(R.string.text_moodGood);
                myCurrentMood = new Mood(user, location, comment, res.getInteger(R.integer.int_Good));
                break;
            case R.id.button_SoSoLaLa:
                text = getString(R.string.text_moodSoSoLaLa);
                myCurrentMood = new Mood(user, location, comment, res.getInteger(R.integer.int_SosoLala));
                break;
            case R.id.button_NotAmused:
                text = getString(R.string.text_moodNotAmused);
                myCurrentMood = new Mood(user, location, comment, res.getInteger(R.integer.int_NotAmused));
                break;
            case R.id.button_VeryMoody:
                text = getString(R.string.text_moodVeryMoody);
                myCurrentMood = new Mood(user, location, comment, res.getInteger(R.integer.int_VeryMoody));
                break;
            default:
                text = "No Button recognized!";
        }

        //Show Mood with toast
        String toastText = myCurrentMood.toString() + " - " + text;
        Toast toast = Toast.makeText(context, toastText, duration);
        toast.show();

        //postMood() - somehow :-)
    }



}
