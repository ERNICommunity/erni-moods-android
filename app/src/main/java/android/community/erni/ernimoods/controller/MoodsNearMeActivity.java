package android.community.erni.ernimoods.controller;

import android.app.Activity;
import android.community.erni.ernimoods.R;
import android.community.erni.ernimoods.model.Mood;
import android.os.Bundle;

import java.util.ArrayList;

/** This Activity gets all the moods from the backend and displays on Google Maps
 * Created by gus on 24.08.14.
 */
public class MoodsNearMeActivity extends Activity {

    /**
     * this array list will hold all the moods objects that are retrieved from the backend
     * would make more sense to store this in the Application class, so that we don't
     * waste resources calling the API and downloading all moods everytime this activity is called
     */
    public ArrayList<Mood> moods;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_moods_near_me);

        // TODO should do something to get the moods from the API and display on a google map
    }

}
