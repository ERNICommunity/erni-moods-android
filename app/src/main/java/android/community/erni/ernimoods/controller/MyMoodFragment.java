package android.community.erni.ernimoods.controller;

import android.app.Fragment;
import android.community.erni.ernimoods.R;
import android.community.erni.ernimoods.api.MoodsBackend;
import android.community.erni.ernimoods.model.Mood;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;

/**
 * This fragment is used to enter your current mood
 */
public class MyMoodFragment extends Fragment {

    //debug tag
    private static final String TAG = "MyMoodFragment";
    //storage variable to handle the mood-request
    private MoodsBackend.OnConversionCompleted callHandlerPostMood;
    // the greeting message, will be programmatically altered depending on the time of day
    private TextView greeting;
    private EditText comment;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_mood, container, false);

        // references to the UI objects we need
        greeting = (TextView)view.findViewById(R.id.textViewGreeting);
        comment = (EditText)view.findViewById(R.id.commentText);


        // show the action bar when this fragment is displayed
        getActivity().getActionBar().show();

        //make sure the MyMood Tab is highlighted
        getActivity().getActionBar().setSelectedNavigationItem(1);

        // set the greeting depending on the time of day
        createGreeting();

        callHandlerPostMood = new MoodsBackend.OnConversionCompleted<String>() {
            @Override
            //what to do on successful conversion?
            public void onConversionCompleted(String id) {
                //Log some data from the retrieved objects
                Log.d("Mood create with id", id);
                ((EntryPoint) getActivity()).updateMoodList();
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

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void registerClickListener(View view, int buttonId, final int moodId) {
        view.findViewById(buttonId).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Retrieve username from application shared preferences
                Resources res = getResources();
                SharedPreferences myPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getBaseContext());

                String user = myPreferences.getString("pref_username", "nothing");
                String commentText = comment.getText().toString();


                if (((EntryPoint) getActivity()).isOnline()) {
                    // create a mood object
                    Mood myCurrentMood = new Mood(user, ((EntryPoint) getActivity()).getCurrentLocation(), commentText, moodId);

                    Log.d(TAG, "Created mood: " + myCurrentMood.toString());

                    //create a moods backend object
                    MoodsBackend getMoods = new MoodsBackend();
                    //set listener to handle successful retrieval
                    getMoods.setListener(callHandlerPostMood);
                    //set event handler for the errors
                    // getMoods.setErrorListener(errorHandler);
                    //start async-task
                    getMoods.postMood(myCurrentMood);
                } else {
                    Toast.makeText(
                            getActivity().getBaseContext(),
                            "No network servide. Enable service and try again.",
                            Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    /**
     * changes the greeting depending on the time of day
     */
    private void createGreeting() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        String timeOfDay;
        if (hour > 0 && hour < 12) {
            timeOfDay = "morning";
        } else if (hour > 12 && hour < 17) {
            timeOfDay = "afternoon";
        } else {
            timeOfDay = "evening";
        }
        greeting.setText("Good " + timeOfDay + ". How are you today?");


    }
}
