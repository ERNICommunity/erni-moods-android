package android.community.erni.ernimoods.model;

import android.location.Location;

/**
 * represents a Mood object
 * The idea is that we store an arraylist of mood objects in the Moods class
 * And use this to create the user's current mood
 */
public class Mood {

    private String username;
    private Location location;
    private String comment;
    private String id = "";
    private int mood;

    public Mood (String username, Location location, String comment, int mood) {
        this.username = username;
        this.location = location;
        this.comment = comment;
        this.mood = mood;
    }

    public Mood(String username, Location location, String comment, int mood, String id) {
        this.username = username;
        this.location = location;
        this.comment = comment;
        this.mood = mood;
        this.id = id;
    }


    // just getters and setters

    public String getUsername() {
        return username;
    }

    public Location getLocation() {
        return location;
    }

    public String getComment() {
        return comment;
    }

    public int getMood() {
        return mood;
    }

    public String getId() {
        return id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setMood(int mood) {
        this.mood = mood;
    }

    public String toString() {
        return "username: " + username + " mood: " + mood;
    }

    public void setId(String id) {
        this.id = id;
    }
}
