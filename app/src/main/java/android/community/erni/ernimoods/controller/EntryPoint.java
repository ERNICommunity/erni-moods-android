package android.community.erni.ernimoods.controller;

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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * This is the starting Activity for the application.
 */
public class EntryPoint extends Activity {
    TextView welcomeText;

    //storage variable to handle the mood-request
    private MoodsBackend.OnConversionCompleted callHandlerGetMoods;
    //storage variable to handle the user-request
    private UserBackend.OnConversionCompleted callHandlerGetUser;
    //error handler to handle errors from the request
    private MoodsBackend.OnJSONResponseError errorHandler;
    //error handler to handle errors from the user retrieval
    private UserBackend.OnJSONResponseError errorHandlerUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry_point);


        //attach call handler. this method is called as soon as the moods-list is loaded
        callHandlerGetMoods = new MoodsBackend.OnConversionCompleted<ArrayList<Mood>>() {
            @Override
            //what to do on successful conversion?
            public void onConversionCompleted(ArrayList<Mood> moods) {
                //Log some data from the retrieved objects
                Log.d("Number of moods in database", String.valueOf(moods.size()));
                Log.d("Latest moods object", "Username: " + moods.get(0).getUsername() +
                        "; Mood: " + String.valueOf(moods.get(0).getMood()) + "; Comment: " + moods.get(0).getComment());
                Log.d("Status", "Moods successfully loaded");
            }
        };

        //event handler when user could be loaded
        callHandlerGetUser = new UserBackend.OnConversionCompleted<User>() {
            @Override
            public void onConversionCompleted(User user) {
                //display username
                Log.d("User successfully loaded", user.getUsername());
                //TODO if the user could be retrieved, it exists and redirect to the mymood page
                //set userRegistered = true
                ((MoodsApp) getApplication()).userRegistered = true;
                //change the fragment
                changeFragment();
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
                //don't set userRegistered and call function to redirect
                changeFragment();
            }
        };

        //create a moods backend object
        MoodsBackend getMoods = new MoodsBackend();
        //set listener to handle successful retrieval
        getMoods.setListener(callHandlerGetMoods);
        //set event handler for the errors
        getMoods.setErrorListener(errorHandler);
        //start async-task
        getMoods.getAllMoods();

        //again, create an object to call the user-backend
        UserBackend getUser = new UserBackend();
        //attached the specified handlers
        getUser.setListener(callHandlerGetUser);
        getUser.setErrorListener(errorHandlerUser);
        //TODO load user data from the settings
        //non existing user to demonstrate the error-code handler
        getUser.getUserByPassword("notexisting", "samplepassword");

        //if you want to check, what happens if a user exists use this code
        //getUser.getUserByPassword("dani.erni","password");

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

    public void changeFragment() {
        // depending on whether the user is registered, inflate the relevant fragment
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        boolean userRegistered = ((MoodsApp) getApplication()).userRegistered;
        if (userRegistered) {
            fragmentTransaction
                    .replace(R.id.fragmentContainer, new MyMoodFragment())
                    .commit();
        } else {
            fragmentTransaction
                    .replace(R.id.fragmentContainer, new SignUpFragment())
                    .commit();
        }
    }
}