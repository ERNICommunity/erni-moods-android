package android.community.erni.ernimoods.controller;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.community.erni.ernimoods.R;
import android.community.erni.ernimoods.api.MoodsBackend;
import android.community.erni.ernimoods.api.UserBackend;
import android.community.erni.ernimoods.model.JSONResponseException;
import android.community.erni.ernimoods.model.LocationDeserializer;
import android.community.erni.ernimoods.model.LocationSerializer;
import android.community.erni.ernimoods.model.Mood;
import android.community.erni.ernimoods.model.User;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
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
    //storage variable to handle the mood-request to get the users mood
    private MoodsBackend.OnConversionCompleted callHandlerGetMyMoods;
    //error handler to handle errors from the request
    private MoodsBackend.OnJSONResponseError errorHandlerGetMoods;

    //stores the most current moods
    private ArrayList<Mood> cleanMoodsList = null;
    //stores the current user's moods
    private ArrayList<Mood> myMoods = null;

    private ProgressDialog progress;

    private LoginFragment loginFragment = null;
    private MoodHistoryFragment moodHistoryFragment = null;
    private MoodsNearMeFragment moodsNearMeFragment = null;
    private MyMoodFragment myMoodFragment = null;
    private SignUpFragment signUpFragment = null;

    private Fragment shownFragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry_point);

        FragmentManager fm = getFragmentManager();

        if (savedInstanceState != null) {
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(Location.class, new LocationDeserializer())
                    .registerTypeAdapter(Location.class, new LocationSerializer())
                    .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                    .serializeNulls()
                    .create();
            if (savedInstanceState.containsKey("userID")) {
                userID = savedInstanceState.getString("userID");
            }
            if (savedInstanceState.containsKey("moodsList")) {
                cleanMoodsList = gson.fromJson(savedInstanceState.getString("moodsList"), new TypeToken<ArrayList<Mood>>() {
                }.getType());
            }
            if (savedInstanceState.containsKey("myMoods")) {
                myMoods = gson.fromJson(savedInstanceState.getString("myMoods"), new TypeToken<ArrayList<Mood>>() {
                }.getType());
            }
        }

        loginFragment = (LoginFragment) fm.findFragmentByTag("loginFragment");
        moodHistoryFragment = (MoodHistoryFragment) fm.findFragmentByTag("moodHistoryFragment");
        moodsNearMeFragment = (MoodsNearMeFragment) fm.findFragmentByTag("moodsNearMeFragment");
        myMoodFragment = (MyMoodFragment) fm.findFragmentByTag("myMoodFragment");
        signUpFragment = (SignUpFragment) fm.findFragmentByTag("signUpFragment");

        if (loginFragment == null) {
            loginFragment = new LoginFragment();
            fm.beginTransaction().add(R.id.fragmentContainer, loginFragment, "loginFragment").commit();
        }
        if (moodHistoryFragment == null) {
            moodHistoryFragment = new MoodHistoryFragment();
            fm.beginTransaction().add(R.id.fragmentContainer, moodHistoryFragment, "moodHistoryFragment").commit();
        }
        if (moodsNearMeFragment == null) {
            moodsNearMeFragment = new MoodsNearMeFragment();
            fm.beginTransaction().add(R.id.fragmentContainer, moodsNearMeFragment, "moodsNearMeFragment").commit();
        }
        if (signUpFragment == null) {
            signUpFragment = new SignUpFragment();
            fm.beginTransaction().add(R.id.fragmentContainer, signUpFragment, "signUpFragment").commit();
        }
        if (myMoodFragment == null) {
            myMoodFragment = new MyMoodFragment();
            fm.beginTransaction().add(R.id.fragmentContainer, myMoodFragment, "myMoodsFragment").commit();
        }

        //setup the action bar to show tabs
        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);


        // hard code the tabs
        actionBar.addTab(actionBar.newTab().setText(getString(R.string.tab_near_me)).setTabListener(this), true);
        actionBar.addTab(actionBar.newTab().setText(getString(R.string.tab_my_mood)).setTabListener(this), false);
        actionBar.addTab(actionBar.newTab().setText(getString(R.string.tab_mood_history)).setTabListener(this), false);

        fm.beginTransaction().hide(myMoodFragment).commit();
        fm.beginTransaction().hide(signUpFragment).commit();
        fm.beginTransaction().hide(moodHistoryFragment).commit();
        fm.beginTransaction().hide(loginFragment).commit();
        fm.beginTransaction().hide(moodsNearMeFragment).commit();


        /*
        to avoid problems if no location is accessible, we create a dummy-location first
         */
        currentLocation = new Location(getString(R.string.dummy_location));
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
                //after authentication of the user, update the mood list
                if (!actionBar.isShowing()) {
                    actionBar.show();
                }
                updateMoodList();
                //store userID
                userID = user.getID();
            }
        };
        //very important event
        //event handler for loading the user. log that something went wrong
        //call the change fragment method, which redirects to the sign-up page
        errorHandlerUser = new UserBackend.OnJSONResponseError() {
            @Override
            public void onJSONResponseError(JSONResponseException e) {
                //user does not exist or something else went wrong
                Log.d("Something went wrong", e.getErrorCode() + ": " + e.getErrorMessage());
                //redirect to the signup activity
                if (actionBar.isShowing()) {
                    actionBar.hide();
                }
                hideFragment();
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.show(loginFragment);
                shownFragment = loginFragment;
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
                //redirect to the moods near me
                progress.dismiss();

                Calendar cal = Calendar.getInstance();
                cal.setTime(new Date());
                cal.add(Calendar.DATE, -1);
                Date oneDayAgo = cal.getTime();

                SimpleDateFormat myDateFormat = new SimpleDateFormat("dd.MM.yyyy hh:mm");

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                try {
                    if (!prefs.contains("lastPost") || myDateFormat.parse(prefs.getString("lastPost", null)).before(oneDayAgo)) {
                        actionBar.setSelectedNavigationItem(1);
                        //fragmentTransaction.show(myMoodFragment);
                    } else {
                        //fragmentTransaction.show(moodsNearMeFragment);
                        actionBar.setSelectedNavigationItem(0);
                    }
                } catch (ParseException e) {
                    actionBar.setSelectedNavigationItem(1);
                }
            }
        };

        /**
         * Each time when the activity resumes, the moods from the current user are loaded.
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

        progress = new ProgressDialog(this);
        progress.setTitle(getString(R.string.loading_moods));
        progress.setCancelable(false);
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
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

    /**
     * On application resume, start listening for the location and check whether the user is registered
     * If yes, the mood-lists are updated
     */
    @Override
    public void onResume() {
        super.onResume();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Boolean fromOrientation = prefs.getBoolean("pref_orientation", false);

        if (!fromOrientation) {

            //again, create an object to call the user-backend
            UserBackend getUser = new UserBackend();
            //attached the specified handlers
            getUser.setListener(callHandlerGetUser);
            getUser.setErrorListener(errorHandlerUser);

            //load username and password from preferences
            String username = prefs.getString(getString(R.string.pref_username), null);
            String pwd = prefs.getString(getString(R.string.pref_password), null);

            //get user by username and password. the handlers will redirect to either the signup
            //or the mymood, depending on whether the user exists or not
            if (isOnline()) {
                getUser.getUserByPassword(username, pwd);
            } else {
                Toast.makeText(
                        getBaseContext(), getString(R.string.no_network),
                        Toast.LENGTH_SHORT).show();
            }

            //when the app is resumed, the location might have changed
            //we get updates not more often than every 500 ms and if the change is smaller than 50m
            locationManager.requestLocationUpdates(provider, 500, 50, this);

        } else {
            getActionBar().setSelectedNavigationItem(prefs.getInt("actionBarTab", 0));
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("pref_orientation", false);
            editor.commit();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        //as soon as the application pauses, we stop getting location updates (if we still receive)
        locationManager.removeUpdates(this);
        hideFragment();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString("userID", userID);
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Location.class, new LocationDeserializer())
                .registerTypeAdapter(Location.class, new LocationSerializer())
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                .serializeNulls()
                .create();
        savedInstanceState.putString("moodsList", gson.toJson(cleanMoodsList));
        savedInstanceState.putString("myMoods", gson.toJson(myMoods));
        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = prefs.edit();
        if (isChangingConfigurations()) {
            editor.putBoolean("pref_orientation", true);
        }
        editor.putInt("actionBarTab", getActionBar().getSelectedTab().getPosition());
        editor.commit();
    }


    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
        hideFragment();

        switch (tab.getPosition()) {
            case 0:
                ft.show(moodsNearMeFragment);
                moodsNearMeFragment.updateMap();
                shownFragment = moodsNearMeFragment;
                break;
            case 1:
                ft.show(myMoodFragment);
                shownFragment = myMoodFragment;
                break;
            case 2:
                ft.show(moodHistoryFragment);
                moodHistoryFragment.updateChart();
                shownFragment = moodHistoryFragment;
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

    /**
     * This method can be called by the fragments to access the mood-list
     *
     * @return Mood-list
     */
    public ArrayList<Mood> getMoodsList() {
        return cleanMoodsList;
    }

    /**
     * This method can be called by the fragments to access the current user's moods
     *
     * @return Mood-list
     */
    public ArrayList<Mood> getMyMoods() {
        return this.myMoods;
    }

    public String getUserName() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        return prefs.getString("pref_username", null);
    }

    /**
     * Check, whether the network connection is available
     *
     * @return True if yes, False if no
     */
    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }

    /**
     * This method can be called to reload the mood lists from the moods-backend. This is helpful
     * on application start or if a mood has been posted
     */
    public void updateMoodList() {
        progress.show();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String username = prefs.getString("pref_username", null);

        //if connection is available
        if (isOnline()) {
            //create a moods backend object
            MoodsBackend getMoods = new MoodsBackend();
            //set listener to handle successful retrieval
            getMoods.setListener(callHandlerGetMoods);
            //set event handler for the errors
            getMoods.setErrorListener(errorHandlerGetMoods);
            //start async-task to update the moods
            getMoods.getAllMoods();

            //create a moods backend object
            MoodsBackend getMyMoods = new MoodsBackend();
            //set listener to handle successful retrieval
            getMyMoods.setListener(callHandlerGetMyMoods);
            getMyMoods.setErrorListener(errorHandlerGetMoods);
            getMyMoods.getMoodsByUsername(username);
        } else {
            Toast.makeText(
                    getBaseContext(),
                    getString(R.string.no_network),
                    Toast.LENGTH_SHORT).show();
        }
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

    public UserBackend.OnConversionCompleted getUserAuthCallback() {
        return this.callHandlerGetUser;
    }

    public UserBackend.OnJSONResponseError getUserNonauthCallback() {
        return this.errorHandlerUser;
    }

    private void hideFragment() {
        FragmentManager fm = getFragmentManager();
        if (shownFragment != null) {
            fm.beginTransaction().hide(shownFragment).commit();
        }
    }
}
