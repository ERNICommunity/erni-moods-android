package android.community.erni.ernimoods.controller;



import android.community.erni.ernimoods.R;
import android.community.erni.ernimoods.api.MoodsBackend;
import android.community.erni.ernimoods.model.Mood;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 *
 */
public class MoodsNearMeFragment extends Fragment {


    /** Local variables **/
    GoogleMap googleMap;
    //storage variable to handle the mood-request
    private MoodsBackend.OnConversionCompleted callHandlerGetMoods;


    public MoodsNearMeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        //attach call handler. this method is called as soon as the moods-list is loaded
        callHandlerGetMoods = new MoodsBackend.OnConversionCompleted<ArrayList<Mood>>() {
            @Override
            //what to do on successful conversion?
            public void onConversionCompleted(ArrayList<Mood> moods) {
                //Log some data from the retrieved objects
                for(int i =0;i<moods.size();i++){
                    addMarker(moods.get(i).getLocation().getLatitude(),moods.get(i).getLocation().getLongitude(),moods.get(i).getUsername());
                }


            }
        };

        createMapView();

        //create a moods backend object
        MoodsBackend getMoods = new MoodsBackend();
        //set listener to handle successful retrieval
        getMoods.setListener(callHandlerGetMoods);
        //set event handler for the errors
        getMoods.setErrorListener(null);
        //start async-task
        getMoods.getMoodsByLocation(0.0, 0.0, 1000000.0);

        return inflater.inflate(R.layout.fragment_moods_near_me, container, false);
    }

    /**
     * Initialises the mapview
     */
    private void createMapView(){
        /**
         * Catch the null pointer exception that
         * may be thrown when initialising the map
         */
        try {
            if(null == googleMap){
                googleMap = ((MapFragment) getFragmentManager().findFragmentById(
                        R.id.mapView)).getMap();

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
    private void addMarker(double lon, double lat, String title){

        /** Make sure that the map has been initialised **/
        if(null != googleMap){
            googleMap.addMarker(new MarkerOptions()
                            .position(new LatLng(lon, lat))
                            .title(title)
                            .draggable(true)
            );
        }
    }
}
