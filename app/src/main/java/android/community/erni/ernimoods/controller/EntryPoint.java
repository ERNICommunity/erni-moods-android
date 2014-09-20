package android.community.erni.ernimoods.controller;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.community.erni.ernimoods.R;
import android.community.erni.ernimoods.api.JSONResponseException;
import android.community.erni.ernimoods.api.MoodsBackend;
import android.community.erni.ernimoods.api.UserBackend;
import android.community.erni.ernimoods.model.Mood;
import android.community.erni.ernimoods.model.User;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * This is the starting Activity for the application.
 */
public class EntryPoint extends Activity implements ActionBar.TabListener {
    TextView welcomeText;
    public static final String TAG = "EntryPoint";


    //storage variable to handle the mood-request
    private MoodsBackend.OnConversionCompleted callHandlerGetMoods;
    //storage variable to handle the user-request
    private UserBackend.OnConversionCompleted callHandlerGetUser;
    //error handler to handle errors from the request
    private MoodsBackend.OnJSONResponseError errorHandler;
    //error handler to handle errors from the user retrieval
    private UserBackend.OnJSONResponseError errorHandlerUser;
    //storage variable to handle the mood-request
    private MoodsBackend.OnConversionCompleted callHandlerDeleteMood;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry_point);

        //setup the action bar to show tabs
       final ActionBar actionBar = getActionBar();
       actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // hard code the tabs
       actionBar.addTab(actionBar.newTab().setText("Near Me").setTabListener(this));
       actionBar.addTab(actionBar.newTab().setText("My Mood").setTabListener(this));
        // etc for mood history in future


        //attach call handler. this method is called as soon as the moods-list is loaded
        callHandlerGetMoods = new MoodsBackend.OnConversionCompleted<ArrayList<Mood>>() {
            @Override
            //what to do on successful conversion?
            public void onConversionCompleted(ArrayList<Mood> moods) {
                //Log some data from the retrieved objects
                Log.d("Number of moods in database", String.valueOf(moods.size()));
                if (moods.size() > 0) {
                    Log.d("Latest moods object", "Username: " + moods.get(0).getUsername() +
                        "; Mood: " + String.valueOf(moods.get(0).getMood()) + "; Comment: " + moods.get(0).getComment());
                    Log.d("Status", "Moods successfully loaded");
                }
                //this is to demonstrate the delete functionality
                //the latest mood object retrieved is deleted
                //however, we don't want to delete a mood each time we start the app, huh? :)

                /*
                MoodsBackend deleteMood = new MoodsBackend();
                deleteMood.setListener(callHandlerDeleteMood);
                deleteMood.setErrorListener(errorHandler);
                deleteMood.deleteMood(moods.get(0).getId());
                */
            }
        };

        //attach call handler. this method is called as soon as a moods object has been deleted
        callHandlerDeleteMood = new MoodsBackend.OnConversionCompleted<Boolean>() {
            @Override
            //what to do on successful deletion?
            public void onConversionCompleted(Boolean status) {
                //Write a log message if delete was successful
                if (status == true) {
                    Log.d("Deleted", "Mood object deleted");
                }
            }
        };

        //event handler when user could not be loaded
        callHandlerGetUser = new UserBackend.OnConversionCompleted<User>() {
            @Override
            public void onConversionCompleted(User user) {
                //display username
                Log.d("User successfully loaded", user.getUsername());
                //change the fragment to mymood
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction
                        .replace(R.id.fragmentContainer, new MoodsNearMeFragment())
                        .commit();
            }
        };

        //what happens if there is an error loading the moods
        errorHandler = new MoodsBackend.OnJSONResponseError() {
            @Override
            public void onJSONResponseError(JSONResponseException e) {
                Log.d("Something went wrong", e.getErrorCode() + ": " + e.getErrorMessage());
            }
        };

        //event handler for loading the user. log that something went wrong
        //call the change fragment method, which redirects to the sign-up page
        errorHandlerUser = new UserBackend.OnJSONResponseError() {
            @Override
            public void onJSONResponseError(JSONResponseException e) {
                //user does not exist or something else went wrong
                Log.d("Something went wrong", e.getErrorCode() + ": " + e.getErrorMessage());
                //redirect to the signup activity
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction
                        .replace(R.id.fragmentContainer, new SignUpFragment())
                        .commit();
            }
        };

        //create a moods backend object
        MoodsBackend getMoods = new MoodsBackend();
        //set listener to handle successful retrieval
        getMoods.setListener(callHandlerGetMoods);
        //set event handler for the errors
        getMoods.setErrorListener(errorHandler);
        //start async-task
        getMoods.getMoodsByLocation(0.0, 0.0, 1000.0);

        //again, create an object to call the user-backend
        UserBackend getUser = new UserBackend();
        //attached the specified handlers
        getUser.setListener(callHandlerGetUser);
        getUser.setErrorListener(errorHandlerUser);

        //load username and password from preferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String username = prefs.getString("pref_username", null);
        String pwd = prefs.getString("pref_password", null);

        //get user by username and password. the handlers will redirect to either the signup
        //or the mymood, depending on whether the user exists or not
        getUser.getUserByPassword(username, pwd);

    }



    /*
    this creates the 'menu' which at the moment is only a preferences button rather than menu items
    */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
// Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_entry_point, menu);
        return true;
    }

    /*
    handles what to do when a menu item is selected
    */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
// Handle action bar item clicks here. The action bar will
// automatically handle clicks on the Home/Up button, so long
// as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_preferences) {
// launch the SettingsActivity (preferences screen)
            Intent i = new Intent(this, SettingsActivity.class);
            startActivity(i);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // check network connection
    public boolean isConnected() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(this.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected())
            return true;
        else
            return false;
    }

    public void changeFragment(Boolean userFound) {
        // depending on whether the user is registered, inflate the relevant fragment
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if (userFound) {
            fragmentTransaction
                    .replace(R.id.fragmentContainer, new MyMoodFragment())
                    .commit();
        } else {
            fragmentTransaction
                    .replace(R.id.fragmentContainer, new SignUpFragment())
                    .commit();
        }
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
        switch (tab.getPosition()) {
            case 0:
                ft.replace(R.id.fragmentContainer, new MoodsNearMeFragment());
                Log.d(TAG, "Created MOodsNearMeFragment");

            break;
            case 1:
                ft.replace(R.id.fragmentContainer, new MyMoodFragment());
                Log.d(TAG, "Created MyMoodFragment");
                break;
        }

    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {

    }
}