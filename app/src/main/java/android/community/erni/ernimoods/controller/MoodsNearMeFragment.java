package android.community.erni.ernimoods.controller;

import android.app.Fragment;
import android.community.erni.ernimoods.R;
import android.community.erni.ernimoods.api.JSONResponseException;
import android.community.erni.ernimoods.api.PlacesBackend;
import android.community.erni.ernimoods.api.UserBackend;
import android.community.erni.ernimoods.model.GooglePlace;
import android.community.erni.ernimoods.model.Mood;
import android.community.erni.ernimoods.model.User;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/** This fragment is used to enter your current mood
 *
 */
public class MoodsNearMeFragment extends Fragment {

    /** Local variables **/
    GoogleMap googleMap; //store the map
    //variables for map view
    private MapView mMapView;
    private Bundle mBundle;

    private PlacesBackend.OnConversionCompleted callHandlerGetPlaces;

    //map moods to their icon-ressources
    private Map<Integer, Integer> iconMap = new HashMap();
    //keep the relation between map-markers and the objects providing data for the markers
    private Map<Marker, GooglePlace> barMap = new HashMap();
    private Map<Marker, Mood> moodMap = new HashMap();

    private View thisView;

    //storage variable to handle the user-request
    private UserBackend.OnConversionCompleted callHandlerGetUser;
    //error handler to handle errors from the user retrieval
    private UserBackend.OnJSONResponseError errorHandlerUser;

    private User clickedUser = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final View view = inflater.inflate(R.layout.fragment_moods_near_me, container, false);

        // show the action bar when this fragment is displayed
        getActivity().getActionBar().show();
        //make sure the NearMe Tab is highlighted
        getActivity().getActionBar().setSelectedNavigationItem(0);

        //get application context
        Context context = getActivity().getApplicationContext();

        //attach call handler. this method is called as soon as the places (bars) hav been fetched
        callHandlerGetPlaces = new PlacesBackend.OnConversionCompleted<ArrayList<GooglePlace>>() {
            @Override
            //what to do on successful conversion?
            public void onConversionCompleted(ArrayList<GooglePlace> place) {
                for (int i = 0; i < place.size(); i++) {
                    addMarker(place.get(i));
                }

            }
        };

        //event handler when user could not be loaded
        callHandlerGetUser = new UserBackend.OnConversionCompleted<User>() {
            @Override
            public void onConversionCompleted(User user) {
                //display username
                Log.d("User successfully loaded", user.getUsername());
                clickedUser = user;
            }
        };

        //event handler for loading the user. log that something went wrong
        //call the change fragment method, which redirects to the sign-up page
        errorHandlerUser = new UserBackend.OnJSONResponseError() {
            @Override
            public void onJSONResponseError(JSONResponseException e) {
                //user does not exist or something else went wrong
                Log.d("Something went wrong", e.getErrorCode() + ": " + e.getErrorMessage());
            }
        };

        /* create google map*/
        mMapView = (MapView) view.findViewById(R.id.map);
        mMapView.onCreate(mBundle);
        createMapView(view);
        MapsInitializer.initialize(context);
        //zoom into the map, reflecting the current location
        Location currentLoc = ((EntryPoint) getActivity()).getCurrentLocation();
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentLoc.getLatitude(), currentLoc.getLongitude()), 12.0f));

        //this methods is called when a marker has been clicked
        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            //implement click handler
            public boolean onMarkerClick(Marker marker) {
                //check whether the marker corresponds to a mood or to a bar
                if (!barMap.containsKey(marker)) {
                    //if a mood-marker has been clicked iterate through all bar-markers and remove them
                    Iterator barIt = barMap.entrySet().iterator();
                    while (barIt.hasNext()) {
                        Map.Entry pair = (Map.Entry) barIt.next();
                        ((Marker) pair.getKey()).remove();
                        barIt.remove();
                    }
                    //if the zoom factor is smaller than 12, zoom in and focus the mood
                    if (googleMap.getCameraPosition().zoom <= 12.0f) {
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 12.0f));
                    }
                    //show marker info windows
                    marker.showInfoWindow();
                    if (((EntryPoint) getActivity()).isOnline()) {
                        //call the places backend to fetch nearby bars
                        PlacesBackend places = new PlacesBackend();
                        places.setListener(callHandlerGetPlaces);
                        //get the 10 closest bars within 10km around the clicked mood
                        places.getBars(marker.getPosition().latitude, marker.getPosition().longitude, 10000, 10);
                    }

                    ((TextView) thisView.findViewById(R.id.selectedUserTextView)).setText(moodMap.get(marker).getUsername());
                    ((TextView) thisView.findViewById(R.id.selectedBarTextView)).setText("");

                    //again, create an object to call the user-backend
                    UserBackend getUser = new UserBackend();
                    //attached the specified handlers
                    getUser.setListener(callHandlerGetUser);
                    getUser.setErrorListener(errorHandlerUser);

                    getUser.getUserByKey(moodMap.get(marker).getUsername(), ((EntryPoint) getActivity()).getUserID());

                    return true;
                } else {
                    //if a bar-marker has been clicked, only display its info window
                    marker.showInfoWindow();

                    ((TextView) thisView.findViewById(R.id.selectedBarTextView)).setText(barMap.get(marker).getName());
                    return true;
                }
            }


        });

        ((Button) view.findViewById(R.id.sendEmailButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (clickedUser != null && clickedUser.getEmail() != "") {
                    String user = ((EntryPoint) getActivity()).getUserName();
                    String bar = ((TextView) thisView.findViewById(R.id.selectedBarTextView)).getText().toString();
                    String subject = "";
                    String body = "";
                    if (bar != "") {
                        subject = getString(R.string.erni_moods) + user + getString(R.string.contacts);
                        body = getString(R.string.hello) +  " " + clickedUser.getUsername() + getString(R.string.meet) + " "+ bar + "?";
                    } else {
                        subject = getString(R.string.erni_moods) + user + getString(R.string.contacts);
                    }
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("message/rfc822");
                    intent.putExtra(Intent.EXTRA_EMAIL, new String[]{clickedUser.getEmail()});
                    intent.putExtra(Intent.EXTRA_SUBJECT, subject);
                    intent.putExtra(Intent.EXTRA_TEXT, body);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(Intent.createChooser(intent, getString(R.string.send_email)));
                } else {
                    Toast.makeText(getActivity().getApplicationContext(),
                            getString(R.string.no_user), Toast.LENGTH_SHORT).show();
                }
            }
        });

        ((Button) view.findViewById(R.id.sendMessageButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (clickedUser != null && clickedUser.getPhone() != "") {
                    String bar = ((TextView) thisView.findViewById(R.id.selectedBarTextView)).getText().toString();
                    String body = "";
                    if (bar != "") {
                        body = getString(R.string.hello) + " "+  clickedUser.getUsername() + getString(R.string.meet) + " "+ bar + "?";
                    }
                    Intent smsIntent = new Intent(Intent.ACTION_VIEW);
                    smsIntent.setType("vnd.android-dir/mms-sms");
                    smsIntent.putExtra("address", clickedUser.getPhone());
                    smsIntent.putExtra("sms_body", body);
                    startActivity(Intent.createChooser(smsIntent, getString(R.string.send_textmessage)));
                } else {
                    Toast.makeText(getActivity().getApplicationContext(),
                            getString(R.string.no_user), Toast.LENGTH_SHORT).show();
                }
            }
        });

        //create a map between image and mood
        iconMap.put(5, R.drawable.smiley_very_happy);
        iconMap.put(4, R.drawable.smiley_good);
        iconMap.put(3, R.drawable.smiley_sosolala);
        iconMap.put(2, R.drawable.smiley_not_amused);
        iconMap.put(1, R.drawable.smiley_very_moody);

        thisView = view;

        return view;
    }

    /**
     * Initialises the mapview
     */
    private void createMapView(View v) {
        /**
         * Catch the null pointer exception that
         * may be thrown when initialising the map
         */
        try {
            if(null == googleMap){
                googleMap = ((MapView) v.findViewById(R.id.map)).getMap();

                /**
                 * If the map is still null after attempted initialisation,
                 * show an error to the user
                 */
                if(null == googleMap) {
                    Toast.makeText(getActivity().getApplicationContext(),
                            getString(R.string.map_error), Toast.LENGTH_SHORT).show();
                }
            }
        } catch (NullPointerException exception){
            Log.e("mapApp", exception.toString());
        }
    }

    /**
     * Adds a marker for a mood object to the map
     */
    private void addMarker(Mood mood) {

        /** Make sure that the map has been initialised **/
        if(null != googleMap){
            String snippet;
            if (mood.getDate() != null) {
                String time = mood.getDate().toString();
                snippet = time + ": " + mood.getComment();
            } else {
                snippet = mood.getComment();
            }
            Marker marker = googleMap.addMarker(new MarkerOptions()
                    .position(new LatLng(mood.getLocation().getLatitude(), mood.getLocation().getLongitude()))
                            .title(mood.getUsername())
                    .snippet(snippet)
                                    //.icon(BitmapDescriptorFactory.fromResource(iconMap.get(mood.getMood())))
                            .icon(BitmapDescriptorFactory.fromResource(iconMap.get(mood.getMood())))
                    .draggable(true));
            //add the relationship between mood-object and marker to the map
            moodMap.put(marker, mood);
        }
    }

    /**
     * Adds a marker for a google-place object to the map
     */
    private void addMarker(GooglePlace place) {

        /** Make sure that the map has been initialised **/
        if (null != googleMap) {
            Marker marker = googleMap.addMarker(new MarkerOptions()
                    .position(new LatLng(place.getLocation().getLatitude(), place.getLocation().getLongitude()))
                    .title(place.getName())
                    .snippet(place.getAddress())
                    .draggable(true));
            //add the relationship between places-object and marker to the map
            barMap.put(marker, place);
        }
    }

    /*
    The following methods ar mandatory in order for the MapView to work. No functionality here.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBundle = savedInstanceState;
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
        //get list with moods
        ArrayList<Mood> cleanMoods = ((EntryPoint) getActivity()).getMoodsList();

        if (moodMap != null) {
            Iterator moodIt = moodMap.entrySet().iterator();
            while (moodIt.hasNext()) {
                Map.Entry pair = (Map.Entry) moodIt.next();
                ((Marker) pair.getKey()).remove();
                moodIt.remove();
            }
        }

        if (cleanMoods != null) {
            Log.d("Number of moods in database", String.valueOf(cleanMoods.size()));
            for (int i = 0; i < cleanMoods.size(); i++) {
                addMarker(cleanMoods.get(i));
            }
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        mMapView.onDestroy();
        super.onDestroy();
    }
}