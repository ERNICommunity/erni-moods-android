package android.community.erni.ernimoods.controller;

import android.app.Fragment;
import android.community.erni.ernimoods.R;
import android.community.erni.ernimoods.api.MoodsBackend;
import android.community.erni.ernimoods.model.Mood;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/** This fragment is used to enter your current mood
 *
 */
public class MoodsNearMeFragment extends Fragment {

    /** Local variables **/
    GoogleMap googleMap;
    private MapView mMapView;
    private Bundle mBundle;
    private BitmapDescriptorFactory bitmapFactory;
    //storage variable to handle the mood-request
    private MoodsBackend.OnConversionCompleted callHandlerGetMoods;
    //error handler to handle errors from the request
    private MoodsBackend.OnJSONResponseError errorHandler;

    private Map<Integer, Integer> iconMap = new HashMap();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_moods_near_me, container, false);
        // show the action bar when this fragment is displayed
        getActivity().getActionBar().show();

        //attach call handler. this method is called as soon as the moods-list is loaded
        callHandlerGetMoods = new MoodsBackend.OnConversionCompleted<ArrayList<Mood>>() {
            @Override
            //what to do on successful conversion?
            public void onConversionCompleted(ArrayList<Mood> moods) {
                //Add markers for all moods
                Log.d("Number of moods in database", String.valueOf(moods.size()));
                for (int i = 0; i < moods.size(); i++) {
                    addMarker(moods.get(i));
                }

            }
        };

        //create a map between image and mood
        Context context = getActivity().getApplicationContext();
        MapsInitializer.initialize(context);
        iconMap.put(5, R.drawable.smiley_very_happy);
        iconMap.put(4, R.drawable.smiley_good);
        iconMap.put(3, R.drawable.smiley_sosolala);
        iconMap.put(2, R.drawable.smiley_not_amused);
        iconMap.put(1, R.drawable.smiley_very_moody);

        mMapView = (MapView) view.findViewById(R.id.map);
        mMapView.onCreate(mBundle);
        createMapView(view);

        //create a moods backend object
        MoodsBackend getMoods = new MoodsBackend();
        //set listener to handle successful retrieval
        getMoods.setListener(callHandlerGetMoods);
        //set event handler for the errors
        getMoods.setErrorListener(errorHandler);
        //start async-task
        getMoods.getAllMoods();

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
                            "Error creating map", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (NullPointerException exception){
            Log.e("mapApp", exception.toString());
        }
    }

    /**
     * Adds a marker to the map
     */
    private void addMarker(Mood mood) {

        /** Make sure that the map has been initialised **/
        if(null != googleMap){
            Log.d("resid", Integer.toString(R.drawable.smiley_sosolala));
            googleMap.addMarker(new MarkerOptions()
                            .position(new LatLng(mood.getLocation().getLongitude(), mood.getLocation().getLatitude()))
                            .title(mood.getUsername())
                            .snippet(mood.getComment())
                                    //.icon(BitmapDescriptorFactory.fromResource(iconMap.get(mood.getMood())))
                            .icon(BitmapDescriptorFactory.fromResource(iconMap.get(mood.getMood())))

            );
        }
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBundle = savedInstanceState;
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
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