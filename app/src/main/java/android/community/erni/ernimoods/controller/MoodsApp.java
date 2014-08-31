package android.community.erni.ernimoods.controller;

import android.app.Application;
import android.community.erni.ernimoods.model.Mood;
import android.nfc.Tag;
import android.util.Log;
import java.util.ArrayList;

/**
 * Classes that extend Application keep global variables and methods that remain during the whole
 * lifecycle of the activity regardless of which activity is running
 * The name of this class needs to be added to the manifest (android:name), so Android knows about it
 */
public class MoodsApp extends Application {

    /**
     * this is the array list that will cont8ain references to all the Mood objects
     * retrieved from the API
     */
    public ArrayList<Mood> moodsList;

    @Override
    public void onCreate() {
        // this is called when the application is started
        // e.g. initialise the variables here

        moodsList = new ArrayList<Mood>();
        Log.d("MoodsApp", "Initialised an empty moodsList"); // log a debug message to the logcat
    }


}
