package android.community.erni.ernimoods.controller;

import android.app.Fragment;
import android.community.erni.ernimoods.R;
import android.community.erni.ernimoods.api.MoodsBackend;
import android.community.erni.ernimoods.model.Mood;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * This fragment is used to enter your current mood
 */
public class MyMoodFragment extends Fragment {

    //storage variable to handle the mood-request
    private MoodsBackend.OnConversionCompleted callHandlerPostMood;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_mood, container, false);

        callHandlerPostMood = new MoodsBackend.OnConversionCompleted<String>() {
            @Override
            //what to do on successful conversion?
            public void onConversionCompleted(String id) {
                //Log some data from the retrieved objects
                Log.d("Mood create with id", id);
//
            }
        };

        Context context = getActivity().getApplicationContext();
        registerClickListener(view, R.id.button_VeryHappy, context.getResources().getInteger(R.integer.int_VeryHappy));
        registerClickListener(view, R.id.button_Good, context.getResources().getInteger(R.integer.int_Good));
        registerClickListener(view, R.id.button_SoSoLaLa, context.getResources().getInteger(R.integer.int_SosoLala));
        registerClickListener(view, R.id.button_NotAmused, context.getResources().getInteger(R.integer.int_NotAmused));
        registerClickListener(view, R.id.button_VeryMoody, context.getResources().getInteger(R.integer.int_VeryMoody));

        return view;
    }

    private void registerClickListener(View view, int buttonId, final int moodId) {
        view.findViewById(buttonId).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Retrieve username from application shared preferences
                Resources res = getResources();
                SharedPreferences myPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getBaseContext());

                String comment = "These are test entries for test purpose";
                String user = myPreferences.getString("pref_username", "nothing");

                Mood myCurrentMood = new Mood(user, getCurrentLocation(), comment, moodId);

                // Show Mood with toast
                Context context = getActivity().getApplicationContext();
                String toastText = myCurrentMood.toString() + " - " + "You pressed a mood button!";
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, toastText, duration);
                toast.show();

                //create a moods backend object
                MoodsBackend getMoods = new MoodsBackend();
                //set listener to handle successful retrieval
                getMoods.setListener(callHandlerPostMood);
                //set event handler for the errors
                // getMoods.setErrorListener(errorHandler);
                //start async-task
                getMoods.postMood(myCurrentMood);

            }
        });
    }

    private Location getCurrentLocation() {
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        if (location == null) {
            location = new Location("ERNI ZH");
            location.setLatitude(47.414892d);
            location.setLongitude(8.552031d);
        }

        return location;
    }

}
