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
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

/**
 * This is the starting Activity for the application.
 */
public class EntryPoint extends Activity implements ActionBar.TabListener, LocationListener {
    public static final String TAG = "EntryPoint";

    //always stores the current location
    private Location currentLocation = null;
    //location manager is used to get the location
    private LocationManager locationManager;
    //location provider, this implementation always uses network-provider since it works
    //better in closed rooms
    private String provider;

    //storage variable to handle the user-request
    private UserBackend.OnConversionCompleted callHandlerGetUser;
    //error handler to handle errors from the user retrieval
    private UserBackend.OnJSONResponseError errorHandlerUser;

    //used to query the backend for a user providing e-mail and phone-number
    private String userID = "";

    //storage variable to handle the mood-request
    private MoodsBackend.OnConversionCompleted callHandlerGetMoods;
    //error handler to handle errors from the request
    private MoodsBackend.OnJSONResponseError errorHandler;

    //storage variable to handle the mood-request
    private MoodsBackend.OnConversionCompleted callHandlerGetMyMoods;
    //error handler to handle errors from the request
    private MoodsBackend.OnJSONResponseError errorHandlerGetMoods;


    private ArrayList<Mood> cleanMoodsList = null;
    private ArrayList<Mood> myMoods = null;

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
        actionBar.addTab(actionBar.newTab().setText("Mood History").setTabListener(this));
        // etc for mood history in future


        /*
        to avoid problems if no location is accessible, we create a dummy-location first
         */
        currentLocation = new Location("Dummy location");
        currentLocation.setLongitude(0.0);
        currentLocation.setLatitude(0.0);
        //get a handle to the location manager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //select network_provider for location services; accurate enough for our purposes
        provider = LocationManager.NETWORK_PROVIDER;
        //request a single update to begin with
        locationManager.requestSingleUpdate(provider, this, null);
        //store the last known location
        currentLocation = locationManager.getLastKnownLocation(provider);

        //event handler when user could not be loaded
        callHandlerGetUser = new UserBackend.OnConversionCompleted<User>() {
            @Override
            public void onConversionCompleted(User user) {
                //display username
                Log.d("User successfully loaded", user.getUsername());
                updateMoodList();
                userID = user.getID();
                //change the fragment to mymood
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction
                        .replace(R.id.fragmentContainer, new MyMoodFragment())
                        .commit();
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

        //attach call handler. this method is called as soon as the moods-list is loaded
        callHandlerGetMoods = new MoodsBackend.OnConversionCompleted<ArrayList<Mood>>() {
            @Override
            //what to do on successful conversion?
            public void onConversionCompleted(ArrayList<Mood> moods) {
                //Add markers for all moods

                //Sort moods by username and keep only the most recent post
                cleanMoodsList = sortAndCleanMoods(moods);
            }
        };

        /**
         * Each time when the fragment resumes, the moods from the current user are loaded.
         * The handler adds the datapoints to the chart and creates a hashmap to map from
         * timestamp to comment.
         */
        callHandlerGetMyMoods = new MoodsBackend.OnConversionCompleted<ArrayList<Mood>>() {
            @Override
            //what to do on successful conversion?
            public void onConversionCompleted(ArrayList<Mood> moods) {
                myMoods = moods;
            }
        };

        /**
         * Well, we want to be informed if the moods could not be loaded
         */
        errorHandlerGetMoods = new MoodsBackend.OnJSONResponseError() {
            @Override
            public void onJSONResponseError(JSONResponseException e) {
                //user does not exist or something else went wrong
                Log.d("Something went wrong", e.getErrorCode() + ": " + e.getErrorMessage());
            }
        };
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

    @Override
    public void onResume() {
        super.onResume();

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

        //when the app is resumed, the location might have changed
        //we get updates not more often than every 500 ms and if the change is smaller than 50m
        locationManager.requestLocationUpdates(provider, 500, 50, this);
    }

    @Override
    public void onPause() {
        super.onPause();
        //as soon as the application pauses, we stop getting location updates (if we still receive)
        locationManager.removeUpdates(this);
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
            case 2:
                ft.replace(R.id.fragmentContainer, new MoodHistoryFragment());
                Log.d(TAG, "Created MoodHistoryFragment");
                break;
        }

    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {

    }

    /*
    When the location has changed, we store the new location to be accessible for the fragments.
    As soon as the accuracy is within 50m, we stop requesting updates
     */
    @Override
    public void onLocationChanged(Location location) {
        this.currentLocation = location;
        if (location.getAccuracy() < 50) {
            locationManager.removeUpdates(this);
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    /**
     * This method can be called by the fragments to access the current location
     *
     * @return Last measured location
     */
    public Location getCurrentLocation() {
        return this.currentLocation;
    }

    public String getUserID() {
        return this.userID;
    }

    public ArrayList<Mood> getMoodsList() {
        return cleanMoodsList;
    }

    public ArrayList<Mood> getMyMoods() {
        return this.myMoods;
    }

    public void updateMoodList() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String username = prefs.getString("pref_username", null);

        //create a moods backend object
        MoodsBackend getMoods = new MoodsBackend();
        //set listener to handle successful retrieval
        getMoods.setListener(callHandlerGetMoods);
        //set event handler for the errors
        getMoods.setErrorListener(errorHandlerGetMoods);
        //start async-task
        getMoods.getAllMoods();

        //create a moods backend object
        MoodsBackend getMyMoods = new MoodsBackend();
        //set listener to handle successful retrieval
        getMyMoods.setListener(callHandlerGetMyMoods);
        getMyMoods.setErrorListener(errorHandlerGetMoods);
        getMyMoods.getMoodsByUsername(username);
    }


    /**
     * This methods can be used to keep only the most recent post from each user
     *
     * @param moods array list of moods
     * @return clean array list of moods
     */
    private ArrayList<Mood> sortAndCleanMoods(ArrayList<Mood> moods) {
        //use the comparator of the mood class to sort the moods by username and then date
        Collections.sort(moods, Mood.sortMoods);
        //iterate through all moods. keep the first mood belonging to the same user
        Iterator<Mood> it = moods.iterator();
        String username = "";
        Mood currentMood = null;
        while (it.hasNext()) {
            currentMood = it.next();
            if (username.equals(currentMood.getUsername())) {
                it.remove();
            }
            username = currentMood.getUsername();
        }
        return moods;
    }
}