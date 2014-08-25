package android.community.erni.ernimoods.model;

import java.util.ArrayList;

/**
 * Singleton class to manage the list of the moods
 * Moods are stored in an ArrayList moodsList
 * Created by gus on 25.08.14.
 */

// TODO figure out if this is really useful or not

public class Moods {

    /**
     * This stores all the moods locally as an array of Mood objects
     */
    private ArrayList<Mood> moodsList;


    /**
     * static variable to store the single moodsSingleton object
     */
    private static Moods moodsSingleton;

    /**
     * private constructor initialises an empty moods list
     */
    private Moods() {
        moodsList = new ArrayList<Mood>();
    }

    /**
     * To create a Moods object call this static method:
     * Moods.get();
     * @return a Moods object
     * if one already exists, does not create another one, but returns the existing one
     *
     */
    public static Moods get() {
        if (moodsSingleton == null) {
            moodsSingleton =  new Moods();
        }
        return moodsSingleton;
    }


    /**
     * @return the list of the Moods objects
     */
    public ArrayList<Mood> getMoodsList() {
        return moodsList;
    }
}
