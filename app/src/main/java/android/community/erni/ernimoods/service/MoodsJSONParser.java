package android.community.erni.ernimoods.service;

import android.community.erni.ernimoods.api.JSONResponseException;
import android.community.erni.ernimoods.model.GooglePlace;
import android.community.erni.ernimoods.model.Mood;
import android.community.erni.ernimoods.model.User;
import android.location.Location;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * This class provides a set of static methods to parse json-messages from the moods-backend and to
 * to create json-representation of the moods- and user-model
 */
public class MoodsJSONParser {

    /**
     * Parse a server-error message from the backend and create a JSONResponseException object
     *
     * @param jsonString json representation of error mesage
     * @return The exception message
     * @see android.community.erni.ernimoods.api.JSONResponseException
     */
    public static JSONResponseException parserError(String jsonString) {
        //try to convert the json-string
        try {
            //create new object
            JSONObject jsonCode = new JSONObject(jsonString);
            //get the code-attribute and the method-attribute and create a new exception
            JSONResponseException e = new JSONResponseException(jsonCode.getString("message"), jsonCode.getString("code"));
            return e;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //if convertion failed, create an artificial error message
        return new JSONResponseException("Could not convert JSON-Error-message", "Conversion error");
    }

    /**
     * If a new object is created in the moods backend, the db-id is returned. This method parses the
     * the json-response and extracts the id from the message
     *
     * @param code json-string with the answer from the moods-backend
     * @return id of the created object
     */
    public static String getId(String code) {
        //try to parse the json-string
        try {
            //create new object
            JSONObject jsonCode = new JSONObject(code);
            //get message attribute
            String message = jsonCode.getString("message");
            //split by spaced
            String[] messageParts = message.split("\\s+");
            //the id is the forth substring
            return messageParts[3];
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //return empty string if conversion failed
        return "";
    }

    /**
     * This method creates an array-list of mood objects from a call to the backend that queried
     * a set of mood objects
     *
     * @param jsonString json-response to parse
     * @return array-list with mood objects
     */
    public static ArrayList<Mood> getMoodsList(String jsonString) {
        //initialize array list
        ArrayList<Mood> myMoods = new ArrayList<Mood>();
        //try to convert the json string
        try {
            //create the object
            JSONArray JSONMoodObjects = new JSONArray(jsonString);
            //loop through the items
            for (int i = 0; i < JSONMoodObjects.length(); i++) {
                //create a json-object from the n-th element
                JSONObject JSONmood = JSONMoodObjects.getJSONObject(i);
                //create a location-object
                Location moodLoc = new Location(JSONmood.getString("username") + "/'s Location");
                //get location array from response
                JSONArray JSONLocArray = JSONmood.getJSONArray("location");
                //set the coordinates extracted from the response to the location
                moodLoc.setLatitude(JSONLocArray.getDouble(0));
                moodLoc.setLongitude(JSONLocArray.getDouble(1));
                //create a new mood object
                Mood moodObject = new Mood("", moodLoc, "", 1);
                if (JSONmood.has("time")) {
                    String time = JSONmood.getString("time");
                    DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                    try {
                        Date date = format.parse(time);
                        moodObject.setDate(date);
                    } catch (ParseException e) {
                        Log.d("Parse exception", e.toString());
                    }
                }
                //extract and set username, comment and mood
                moodObject.setUsername(JSONmood.getString("username"));
                moodObject.setComment(JSONmood.getString("comment"));
                moodObject.setMood(JSONmood.getInt("mood"));
                moodObject.setId(JSONmood.getString("id"));
                //add the object to the list
                myMoods.add(moodObject);
            }
        } catch (JSONException e) {
            Log.d("JSON Exception", e.toString());
        }
        //return the list with the mood objects
        return myMoods;
    }

    /**
     * This method creates a json-representation out of a user-object. This string can be sent
     * to the moods-backend.
     *
     * @param user User object to convert
     * @return JSON-representation of the object
     * @see android.community.erni.ernimoods.model.User
     */
    public static String createJSONUser(User user) {
        //create new json object and try to fill
        JSONObject JSONUser = new JSONObject();
        try {
            //add password
            JSONUser.accumulate("password", user.getPassword());
            //add email if set
            if (user.getEmail() != "") JSONUser.accumulate("email", user.getEmail());
            //add phone and username
            JSONUser.accumulate("phone", user.getPhone());
            JSONUser.accumulate("username", user.getUsername());
        } catch (JSONException e) {
            Log.d("JSON Exception", e.toString());
        }
        //return json-string
        return JSONUser.toString();
    }

    public static User getUser(String jsonString) {
        //create new json object and try to fill
        try {
            //add password
            JSONObject JSONUser = new JSONObject(jsonString);
            User user = new User(JSONUser.getString("username"), JSONUser.getString("phone"), "", "");
            if (JSONUser.has("email")) {
                user.setEmail(JSONUser.getString("email"));
            }
            user.setId(JSONUser.getString("id"));
            return user;
        } catch (JSONException e) {
            Log.d("JSON Exception", e.toString());
        }
        //return json-string
        return null;
    }

    /**
     * This methods creates a json-string from a mood-object. This string can be sent to the
     * moods-backend to create a new mood.
     *
     * @param mood moods object
     * @return json-representation of the mood object
     * @see android.community.erni.ernimoods.model.Mood
     */
    public static String createJSONMood(Mood mood) {
        //create new json array for the location
        JSONArray JSONLocArray = new JSONArray();
        //creat new json object for the mood
        JSONObject JSONMood = new JSONObject();
        try {
            //fill the location-array with location data
            JSONLocArray.put(0, mood.getLocation().getLatitude());
            JSONLocArray.put(1, mood.getLocation().getLongitude());
            //fill the moods-object with location, username, mood and comment
            JSONMood.accumulate("location", JSONLocArray);
            JSONMood.accumulate(("username"), mood.getUsername());
            JSONMood.accumulate("mood", mood.getMood());
            JSONMood.accumulate("comment", mood.getComment());
        } catch (JSONException e) {
            Log.d("JSON Exception", e.toString());
        }
        //return the created json-object as a string
        return JSONMood.toString();
    }

    public static ArrayList<GooglePlace> parsePlacesJSON(String jsonString, int max) {
        ArrayList<GooglePlace> places = new ArrayList<GooglePlace>();
        try {
            JSONObject placesJSON = new JSONObject(jsonString);
            JSONArray placesArray = placesJSON.getJSONArray("results");
            for (int i = 0; i < placesArray.length() && i < max; i++) {
                JSONObject currentPlaceJSON = placesArray.getJSONObject(i);
                GooglePlace currentPlace = new GooglePlace();
                currentPlace.setName(currentPlaceJSON.getString("name"));
                currentPlace.setIcon(currentPlaceJSON.getString("icon"));
                if (currentPlaceJSON.has("rating"))
                    currentPlace.setRating(currentPlaceJSON.getDouble("rating"));
                currentPlace.setAddress(currentPlaceJSON.getString("vicinity"));
                Location currentPlaceLocation = new Location(currentPlace.getName());
                JSONObject geometry = currentPlaceJSON.getJSONObject("geometry");
                JSONObject coordinates = geometry.getJSONObject("location");
                currentPlaceLocation.setLatitude(coordinates.getDouble("lat"));
                currentPlaceLocation.setLongitude(coordinates.getDouble("lng"));
                currentPlace.setLocation(currentPlaceLocation);
                places.add(currentPlace);
            }
        } catch (JSONException e) {
            Log.d("Place parsing", "Something went wrong");
        }
        return places;
    }
}
