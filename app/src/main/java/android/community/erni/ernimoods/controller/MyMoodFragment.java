package android.community.erni.ernimoods.controller;

import android.app.AlertDialog;
import android.app.Fragment;
import android.community.erni.ernimoods.R;
import android.community.erni.ernimoods.api.MoodsBackend;
import android.community.erni.ernimoods.model.JSONResponseException;
import android.community.erni.ernimoods.model.Mood;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * This fragment is used to enter your current mood
 */
public class MyMoodFragment extends Fragment {

    //debug tag
    private static final String TAG = "MyMoodFragment";
    //storage variable to handle the mood-request
    private MoodsBackend.OnConversionCompleted callHandlerPostMood;
    private MoodsBackend.OnJSONResponseError callHandlerPostError;
    // the greeting message, will be programmatically altered depending on the time of day
    private TextView greeting;
    // the user
    private String user;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getActivity().getActionBar().show();
        getActivity().getActionBar().setSelectedNavigationItem(1);

        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_my_mood, container, false);

        setRetainInstance(true);

        // references to the UI objects we need
        greeting = (TextView) view.findViewById(R.id.textViewGreeting);

        // set the greeting depending on the time of day
        createGreeting();

        callHandlerPostMood = new MoodsBackend.OnConversionCompleted<String>() {
            @Override
            //what to do on successful conversion?
            public void onConversionCompleted(String id) {
                //Log some data from the retrieved objects
                Log.d("Mood create with id", id);
                ((EntryPoint) getActivity()).updateMoodList();
                SimpleDateFormat myDateFormat = new SimpleDateFormat("dd.MM.yyyy hh:mm");
                Date time = new Date();
                String now = myDateFormat.format(time);
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("lastPost", now);
                editor.commit();
                ((EntryPoint) getActivity()).stopProgress();
            }
        };

        callHandlerPostError = new MoodsBackend.OnJSONResponseError() {
            @Override
            public void onJSONResponseError(JSONResponseException e) {
                Toast.makeText(
                        getActivity().getBaseContext(),
                        e.getErrorMessage(),
                        Toast.LENGTH_LONG).show();
                ((EntryPoint) getActivity()).stopProgress();
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

                user = myPreferences.getString("pref_username", "nothing");

                // show a dialog
                AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                alert.setTitle(R.string.comment_alert_title);
                alert.setMessage(R.string.comment_alert_message);
                final EditText commentInput = new EditText(getActivity());
                alert.setView(commentInput);
                alert.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String commentText = commentInput.getText().toString();
                        makeTheMood(user, commentText, moodId);
                    }
                });

                alert.setNegativeButton(getString(R.string.no_comment), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        commentInput.setText(getString(R.string.no_comment));
                        String commentText = commentInput.getText().toString();
                        makeTheMood(user, commentText, moodId);
                    }
                });


                alert.show();


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
            timeOfDay = getString(R.string.greetings_morning);
        } else if (hour > 12 && hour < 17) {
            timeOfDay = getString(R.string.greetings_afternoon);
        } else {
            timeOfDay = getString(R.string.greetings_evening);
        }
        greeting.setText(getString(R.string.greetings_good) + " " + timeOfDay + getString(R.string.greetings_how));


    }

    private void makeTheMood(String user, String commentText, int moodId) {
        if (((EntryPoint) getActivity()).isOnline()) {
            // create a mood object
            Location loc = ((EntryPoint) getActivity()).getCurrentLocation();
            Mood myCurrentMood = new Mood(user, loc, commentText, moodId);

            Log.d(TAG, "Created mood: " + myCurrentMood.toString());

            //create a moods backend object
            MoodsBackend getMoods = new MoodsBackend();
            //set listener to handle successful retrieval
            getMoods.setListener(callHandlerPostMood);
            getMoods.setErrorListener(callHandlerPostError);
            //set event handler for the errors
            // getMoods.setErrorListener(errorHandler);
            //start async-task
            getMoods.postMood(myCurrentMood);
            ((EntryPoint) getActivity()).startProgress("Posting mood");
        } else {
            Toast.makeText(
                    getActivity().getBaseContext(),
                    getString(R.string.no_network),
                    Toast.LENGTH_SHORT).show();
        }
    }
}
