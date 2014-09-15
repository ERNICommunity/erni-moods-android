package android.community.erni.ernimoods.api;

import android.community.erni.ernimoods.model.Mood;

/**
 * Created by niklausd on 02.09.2014.
 * This interface specifies the call methods to interact with the Moods-Backend
 */
public interface IMoodsBackend extends IBackendEventHandler {
    /**
     * Post a new mood for a user to the database
     *
     * @param mood Mood object to post
     */
    public void postMood(Mood mood);

    /**
     * Retrieve all moods objects in the database
     */
    public void getAllMoods();

    /**
     * Get all moods posted by a specific user
     *
     * @param username Specifies the user
     */
    public void getMoodsByUsername(String username);

    /**
     * Get all moods in a radial distance around a specified location
     *
     * @param latitude  latitude in degrees
     * @param longitude longitude in degrees
     * @param distance  distance from the origin in meters
     */
    public void getMoodsByLocation(Double latitude, Double longitude, Double distance);

    public void getMoodById(String id);

    public void deleteMood(String id);
}

