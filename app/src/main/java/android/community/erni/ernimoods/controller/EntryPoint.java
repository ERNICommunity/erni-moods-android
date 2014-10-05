package android.community.erni.ernimoods.controller;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.community.erni.ernimoods.R;
import android.community.erni.ernimoods.api.JSONResponseException;
import android.community.erni.ernimoods.api.UserBackend;
import android.community.erni.ernimoods.model.User;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
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
import android.widget.TextView;

/**
 * This is the starting Activity for the application.
 */
public class EntryPoint extends Activity implements ActionBar.TabListener, LocationListener {
    TextView welcomeText;
    public static final String TAG = "EntryPoint";

    private Location currentLocation = null;
    private LocationManager locationManager;
    private String provider;

    //storage variable to handle the user-request
    private UserBackend.OnConversionCompleted callHandlerGetUser;
    //error handler to handle errors from the user retrieval
    private UserBackend.OnJSONResponseError errorHandlerUser;

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


        // Get the location manager
        currentLocation = new Location("Dummy location");
        currentLocation.setLongitude(0.0);
        currentLocation.setLatitude(0.0);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        //provider = locationManager.getBestProvider(criteria, false);
        provider = LocationManager.NETWORK_PROVIDER;
        locationManager.requestSingleUpdate(provider, this, null);
        currentLocation = locationManager.getLastKnownLocation(provider);

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

    @Override
    public void onResume() {
        super.onResume();
        locationManager.requestLocationUpdates(provider, 500, 50, this);
    }

    @Override
    public void onPause() {
        super.onPause();
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

    public Location getCurrentLocation() {
        return this.currentLocation;
    }
}