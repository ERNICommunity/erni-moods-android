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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static android.app.ActionBar.NAVIGATION_MODE_TABS;
import static android.app.ActionBar.Tab;
import static android.app.ActionBar.TabListener;

/**
 * This is the starting Activity for the application.
 */
public class EntryPoint extends Activity implements LocationListener {
    public static final String TAG = "EntryPoint";

    // constants for the mood tags
    public static final String MY_MOOD_FRAGMENT = "myMoodFragment";
    public static final String SIGN_UP_FRAGMENT = "signUpFragment";
    public static final String MOOD_HISTORY_FRAGMENT = "moodHistoryFragment";
    public static final String LOGIN_FRAGMENT = "loginFragment";
    public static final String MOODS_NEAR_ME_FRAGMENT = "moodsNearMeFragment";

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

    //progress bar to notify user about communication with the backend
    private ProgressDialog progress;

    //stores handles to the app's five fragments
    private Map<String, Fragment> fragmentMap = new HashMap();

    //an identifier of the currently displayed fragment
    private String shownFragment = "";

    //boolean that is set when the user has successfully been authorized
    private boolean isAuthorized = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry_point);

        // get a FragmentManager and call method to setup the fragments initially
        FragmentManager fm = setupFragments();

        //setup the action bar to show tabs
        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(NAVIGATION_MODE_TABS);

        // hard code the tabs
        actionBar.addTab(actionBar.newTab().setText(getString(R.string.tab_near_me)).setTabListener(new NearMeTabListener()), true);
        actionBar.addTab(actionBar.newTab().setText(getString(R.string.tab_my_mood)).setTabListener(new MyMoodTabListener()), false);
        actionBar.addTab(actionBar.newTab().setText(getString(R.string.tab_mood_history)).setTabListener(new MoodHistoryTabListener()), false);

        /**
         * By default, hide all fragments. Importantly: This is done after the action bar initialization,
         * since fragments are implicitly shown and hidden during initialization
         */
        fm.beginTransaction().hide(fragmentMap.get(MY_MOOD_FRAGMENT)).commit();
        fm.beginTransaction().hide(fragmentMap.get(SIGN_UP_FRAGMENT)).commit();
        fm.beginTransaction().hide(fragmentMap.get(MOOD_HISTORY_FRAGMENT)).commit();
        fm.beginTransaction().hide(fragmentMap.get(LOGIN_FRAGMENT)).commit();
        fm.beginTransaction().hide(fragmentMap.get(MOODS_NEAR_ME_FRAGMENT)).commit();

        /**
         * If an configuration change occured (back-button, orientation change important variables
         * of the previous activity instance have been stored and are recovered here. These include:
         * list of moods, user-ID of current user, authorization state and currently shown fragment
         */
        recoverSavedInstanceVariables(savedInstanceState);

        /*
        to avoid problems if no location is accessible, we create a dummy-location first
         */
        setUpLocationManager();

        attachUserCallbacks();
        attachMoodsCallbacks();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = prefs.edit();
        if (prefs.getString("pref_username", null) == null || prefs.getString("pref_password", null) == null) {
            editor.putString("pref_username", "");
            editor.commit();
            editor.putString("pref_password", "");
            editor.commit();
        }

        /**
         * Set up the progress dialog
         */
        progress = new ProgressDialog(this);
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
     * If the application resumes due to a configuration change, no communication with the backend ist done.
     * Instead we recover all data and the previously shown fragment.
     */
    @Override
    public void onResume() {
        super.onResume();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        if (!fromOrientation()) {

            //start authorization
            authorizeUser();

            //when the app is resumed, the location might have changed
            //we get updates not more often than every 500 ms and if the change is smaller than 50m
            locationManager.requestLocationUpdates(provider, 500, 50, this);

        } else {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("pref_orientation", false);
            editor.commit();
            //If the user has been on the apps "normal" fragments, recover it
            if (shownFragment != LOGIN_FRAGMENT && shownFragment != SIGN_UP_FRAGMENT) {
                getActionBar().setSelectedNavigationItem(prefs.getInt("actionBarTab", 0));
                /**
                 * Actually, the above line should be sufficient to recover the fragment. However the
                 * google-map and the mood history are not shown correctly, that's why we explcitly
                 * show the fragment and update the map/chart
                 */
                ((MoodsNearMeFragment) fragmentMap.get(MOODS_NEAR_ME_FRAGMENT)).updateMap();
                ((MoodHistoryFragment) fragmentMap.get(MOOD_HISTORY_FRAGMENT)).updateChart();
                getFragmentManager().beginTransaction().show(fragmentMap.get(shownFragment)).commit();
                //if the user was on either login-page oder sign-up page, recover fragment
            } else {
                getFragmentManager().beginTransaction().show(fragmentMap.get(shownFragment)).commit();
                getActionBar().hide();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        //as soon as the application pauses, we stop getting location updates (if we still receive)
        locationManager.removeUpdates(this);
        hideFragment();
    }

    /**
     * This method is called, when the activity is destroyed due to a configuration change. This is the
     * possibility to store important instance variables.
     *
     * @param savedInstanceState
     */
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString("userID", userID);
        /**
         * Mood-lists are converted to a json string by gson and then passed
         * to the next activity instance as a string in the bundle
         */
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Location.class, new LocationDeserializer())
                .registerTypeAdapter(Location.class, new LocationSerializer())
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                .serializeNulls()
                .create();
        savedInstanceState.putString("moodsList", gson.toJson(cleanMoodsList));
        savedInstanceState.putString("myMoods", gson.toJson(myMoods));
        savedInstanceState.putBoolean("isAuthorized", isAuthorized);
        savedInstanceState.putString("shownFragment", shownFragment);
        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    /**
     * Whenever the app is destroyed, this method is called. If the destruction is due to a configuration change,
     * we indicate this for the next activity creation and save the action-bar's tab index in the preferences
     */
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

    /**
     * Method that can be called by a fragment to obtain the authorized user's username
     *
     * @return Username as a string
     */
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
        startProgress(getString(R.string.loading_moods));

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

    /**
     * Hides the currently shown fragment
     */
    private void hideFragment() {
        FragmentManager fm = getFragmentManager();
        if (shownFragment != "") {
            fm.beginTransaction().hide(fragmentMap.get(shownFragment)).commit();
        }
    }

    /**
     * This methods starts the authorization process by checking the credentials in the
     * stored preferences against the backend. Method is public, such that fragments can
     * authorize a user as well. Authorization process starts update-process of moods-list if
     * successful
     */
    public void authorizeUser() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
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
            startProgress(getString(R.string.authorize_progress));
            progress.setTitle(getString(R.string.authorize_progress));
        } else {
            forwardToLogin();
            Toast.makeText(
                    getBaseContext(), getString(R.string.no_network),
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Check whether the activity has been create due to a configuration change
     *
     * @return True if due to configuration change, false if not
     */
    private boolean fromOrientation() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        return prefs.getBoolean("pref_orientation", false);
    }

    /**
     * The user can switch per link between login-form and sign-up-form. This methods enables
     * the respective fragments to easily switch between the corresponding forms
     */
    public void swapLoginSignUp() {
        if (shownFragment.equals(LOGIN_FRAGMENT)) {
            getFragmentManager().beginTransaction().show(fragmentMap.get(SIGN_UP_FRAGMENT)).commit();
            getFragmentManager().beginTransaction().hide(fragmentMap.get(LOGIN_FRAGMENT)).commit();
            shownFragment = SIGN_UP_FRAGMENT;
        } else {
            getFragmentManager().beginTransaction().hide(fragmentMap.get(SIGN_UP_FRAGMENT)).commit();
            getFragmentManager().beginTransaction().show(fragmentMap.get(LOGIN_FRAGMENT)).commit();
            shownFragment = LOGIN_FRAGMENT;
        }
    }

    /**
     * Display the login fragment and hide the action bar. Login-form is displayed if
     * a) user had wrong credentials
     * b) authorization not possible due to network issues
     */
    private void forwardToLogin() {
        if (getActionBar().isShowing()) {
            getActionBar().hide();
        }
        hideFragment();
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.show(fragmentMap.get(LOGIN_FRAGMENT)).commit();
        shownFragment = LOGIN_FRAGMENT;
    }


    /**
     * Setup Fragments. Called from onCreate()
     * This method tries to restore fragments from a saved instance, by identifying their tags.
     * If it cannot, then it creates them and adds them to the fragment container and Map
     */
    private FragmentManager setupFragments() {
        FragmentManager fm = getFragmentManager();

        fragmentMap.put(LOGIN_FRAGMENT, fm.findFragmentByTag(LOGIN_FRAGMENT));
        fragmentMap.put(SIGN_UP_FRAGMENT, fm.findFragmentByTag(SIGN_UP_FRAGMENT));
        fragmentMap.put(MOODS_NEAR_ME_FRAGMENT, fm.findFragmentByTag(MOODS_NEAR_ME_FRAGMENT));
        fragmentMap.put(MY_MOOD_FRAGMENT, fm.findFragmentByTag(MY_MOOD_FRAGMENT));
        fragmentMap.put(MOOD_HISTORY_FRAGMENT, fm.findFragmentByTag(MOOD_HISTORY_FRAGMENT));

        if (fragmentMap.get(LOGIN_FRAGMENT) == null) {
            fragmentMap.put(LOGIN_FRAGMENT, new LoginFragment());
            fm.beginTransaction().add(R.id.fragmentContainer, fragmentMap.get(LOGIN_FRAGMENT), LOGIN_FRAGMENT).commit();
        }
        if (fragmentMap.get(SIGN_UP_FRAGMENT) == null) {
            fragmentMap.put(SIGN_UP_FRAGMENT, new SignUpFragment());
            fm.beginTransaction().add(R.id.fragmentContainer, fragmentMap.get(SIGN_UP_FRAGMENT), SIGN_UP_FRAGMENT).commit();
        }
        if (fragmentMap.get(MOODS_NEAR_ME_FRAGMENT) == null) {
            fragmentMap.put(MOODS_NEAR_ME_FRAGMENT, new MoodsNearMeFragment());
            fm.beginTransaction().add(R.id.fragmentContainer, fragmentMap.get(MOODS_NEAR_ME_FRAGMENT), MOODS_NEAR_ME_FRAGMENT).commit();
        }
        if (fragmentMap.get(MY_MOOD_FRAGMENT) == null) {
            fragmentMap.put(MY_MOOD_FRAGMENT, new MyMoodFragment());
            fm.beginTransaction().add(R.id.fragmentContainer, fragmentMap.get(MY_MOOD_FRAGMENT), MY_MOOD_FRAGMENT).commit();
        }
        if (fragmentMap.get(MOOD_HISTORY_FRAGMENT) == null) {
            fragmentMap.put(MOOD_HISTORY_FRAGMENT, new MoodHistoryFragment());
            fm.beginTransaction().add(R.id.fragmentContainer, fragmentMap.get(MOOD_HISTORY_FRAGMENT), MOOD_HISTORY_FRAGMENT).commit();
        }
        return fm;
    }

    private void recoverSavedInstanceVariables(Bundle savedInstanceState) {
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
                ((MoodHistoryFragment) fragmentMap.get(MOOD_HISTORY_FRAGMENT)).updateChart();
            }
            if (savedInstanceState.containsKey("isAuthorized")) {
                isAuthorized = savedInstanceState.getBoolean("isAuthorized");
            }
            if (savedInstanceState.containsKey("shownFragment")) {
                shownFragment = savedInstanceState.getString("shownFragment");
            }
        }

    }

    private void setUpLocationManager() {
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
    }

    private void attachUserCallbacks() {
        //event handler when user could not be loaded
        callHandlerGetUser = new UserBackend.OnConversionCompleted<User>() {
            @Override
            public void onConversionCompleted(User user) {
                //display username
                Log.d("User successfully loaded", user.getUsername());
                //after authentication of the user, update the mood list
                if (!getActionBar().isShowing()) {
                    getActionBar().show();
                }
                stopProgress();
                hideFragment();
                updateMoodList();
                //store userID
                userID = user.getID();
                isAuthorized = true;
            }
        };
        //very important event
        //event handler for loading the user. log that something went wrong
        //and forward the user to the login-page (where he could navigate further to the
        //sign-up page
        errorHandlerUser = new UserBackend.OnJSONResponseError() {
            @Override
            public void onJSONResponseError(JSONResponseException e) {
                // Assuming that the error is due to invalid user/password. The actual error is printed to Log. Fixes issue #40.
                Toast.makeText(
                        getBaseContext(),
                        "Username and/or password not valid.\nPlease check and try again.",
                        Toast.LENGTH_LONG).show();

                Log.d("Something went wrong", e.getErrorCode() + ": " + e.getErrorMessage());
                stopProgress();
                forwardToLogin();
            }
        };
    }

    private void attachMoodsCallbacks() {
        //attach call handler. this method is called as soon as the moods-list is loaded
        callHandlerGetMoods = new MoodsBackend.OnConversionCompleted<ArrayList<Mood>>() {
            @Override
            //what to do on successful conversion?
            public void onConversionCompleted(ArrayList<Mood> moods) {
                //Add markers for all moods
                //Sort moods by username and keep only the most recent post
                cleanMoodsList = sortAndCleanMoods(moods);
                //redirect to the moods near me
                stopProgress();

                Calendar cal = Calendar.getInstance();
                cal.setTime(new Date());
                cal.add(Calendar.DATE, -1);
                Date oneDayAgo = cal.getTime();

                SimpleDateFormat myDateFormat = new SimpleDateFormat("dd.MM.yyyy hh:mm");

                /**
                 * Depending on whether the user has posted a mood within the last 24 hours, he the fragment to either
                 * post a mood or display the moods is displayed
                 */
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                try {
                    if (!prefs.contains("lastPost") || myDateFormat.parse(prefs.getString("lastPost", null)).before(oneDayAgo)) {
                        getActionBar().setSelectedNavigationItem(1);
                        //fragmentTransaction.show(myMoodFragment);
                    } else {
                        //fragmentTransaction.show(moodsNearMeFragment);
                        getActionBar().setSelectedNavigationItem(0);
                    }
                } catch (ParseException e) {
                    getActionBar().setSelectedNavigationItem(1);
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
                Toast.makeText(
                        getBaseContext(),
                        "Something went wrong getting the moods.",
                        Toast.LENGTH_LONG).show();
                Log.d("Something went wrong", e.getErrorCode() + ": " + e.getErrorMessage());
                stopProgress();
            }
        };

    }

    public void startProgress(String title){
        progress.setTitle(title);
        progress.show();
    }

    public void stopProgress() {
        progress.dismiss();
    }

    /**
     * AbstractTabListener provides an abstract class to handle the ActionBarTabs
     * When you click on a tab it exchanges the respecting fragments and provides tab-specific actions.
     * Concrete implementations for each tab provide the tab specific actions
     */
    private abstract class AbstractTabListener implements TabListener {
        private String fragmentName;

        public AbstractTabListener(String fragmentName) {
            this.fragmentName = fragmentName;
        }

        protected abstract void updateFragment(Fragment fragment);

        @Override
        public void onTabSelected(Tab tab, FragmentTransaction ft) {
            if (!fromOrientation()) {
                hideFragment();
                Fragment fragment = fragmentMap.get(fragmentName);
                ft.show(fragment);
                updateFragment(fragment);
                shownFragment = fragmentName;
            }
        }

        @Override
        public void onTabUnselected(Tab tab, FragmentTransaction ft) {

        }

        @Override
        public void onTabReselected(Tab tab, FragmentTransaction ft) {

        }
    }

    private class NearMeTabListener extends AbstractTabListener {

        public NearMeTabListener() {
            super(MOODS_NEAR_ME_FRAGMENT);
        }

        @Override
        protected void updateFragment(Fragment fragment) {
            ((MoodsNearMeFragment) fragment).updateMap();
        }
    }

    private class MyMoodTabListener extends AbstractTabListener {

        public MyMoodTabListener() {
            super(MY_MOOD_FRAGMENT);
        }

        @Override
        protected void updateFragment(Fragment fragment) {
            // empty (default implementation desired)
        }
    }

    private class MoodHistoryTabListener extends AbstractTabListener {

        public MoodHistoryTabListener() {
            super(MOOD_HISTORY_FRAGMENT);
        }
        @Override
        protected void updateFragment(Fragment fragment) {
            ((MoodHistoryFragment)fragment).updateChart();
        }
    }

}
