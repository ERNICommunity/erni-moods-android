package android.community.erni.ernimoods.model;

import android.location.Location;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Date;

/**
 * represents a Mood object
 * The idea is that we store an arraylist of mood objects in the Moods class
 * And use this to create the user's current mood
 */
public class Mood implements Serializable {


    /**
     * Comparator class to sort moods by username first and then date
     */
    public static Comparator<Mood> sortMoods = new Comparator<Mood>() {
        @Override
        public int compare(Mood mood1, Mood mood2) {
            //check whether the usernames are equal
            int flag = mood1.getUsername().compareTo(mood2.getUsername());
            //if yes, sort the objects by the date posted
            if (flag == 0) flag = mood2.getDate().compareTo(mood1.getDate());
            return flag;
        }
    };
    private String username;
    private Location location;
    private String comment;
    private String id = null;
    //tell the gson-serializar that date in the json-string is called time
    @SerializedName("time")
    private Date date = null;
    private int mood;

    public Mood() {

    }

    public Mood(String username, Location location, String comment, int mood) {
        this.username = username;
        this.location = location;
        this.comment = comment;
        this.mood = mood;
    }


    // just getters and setters

    public Mood(String username, Location location, String comment, int mood, String id) {
        this.username = username;
        this.location = location;
        this.comment = comment;
        this.mood = mood;
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public int getMood() {
        return mood;
    }

    public void setMood(int mood) {
        this.mood = mood;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String toString() {
        return "username: " + username + ", mood: " + mood + ", comment: " + comment;
    }
}
