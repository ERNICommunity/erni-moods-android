package android.community.erni.ernimoods.api;

import android.community.erni.ernimoods.model.Mood;
import android.community.erni.ernimoods.service.MoodsJSONParser;

/**
 * Implementation of the abstract class to query mood data from the backend
 */
public class MoodsBackend implements IMoodsBackend {

    private OnConversionCompleted listener = null; //stores the event listener for completed http-response parsing
    private InternetAccess task = new InternetAccess(); //class variable to store an Internet-Access object
    //event handler for an error message handler
    private OnJSONResponseError errorListener;

    /**
     * Method to set a listener to handle the converted data
     *
     * @param listener Setter method to set the listener
     */
    public void setListener(OnConversionCompleted listener) {
        this.listener = listener;
    }


    /**
     * Setter for the event handler of backend-error messages
     *
     * @param listener
     */
    public void setErrorListener(OnJSONResponseError listener) {
        this.errorListener = listener;
    }


    /**
     * @param mood Mood object to post
     * @see android.community.erni.ernimoods.api.IMoodsBackend
     */
    public void postMood(Mood mood) {
        //set method of the internet call to post
        task.setMethod("POST");
        //attach the listener
        task.setListener(postMoodListener);
        //create a json-object as a string from a mood object
        String jsonString = MoodsJSONParser.createJSONMood(mood);
        //start the asynchronous background task to post a mood
        task.execute("http://moodyrest.azurewebsites.net/moods", jsonString);
    }


    /**
     * @see android.community.erni.ernimoods.api.IMoodsBackend
     */
    public void getAllMoods() {
        // attach listener

        task.setListener(getMoodsListener);
        //start async task
        task.execute("http://moodyrest.azurewebsites.net/moods");
    }

    /**
     * @param username Specifies the user
     * @see android.community.erni.ernimoods.api.IMoodsBackend
     */
    public void getMoodsByUsername(String username) {
        // call AsyncTask to perform network operation on separate thread
        task.setListener(getMoodsListener);
        //call background task
        //TODO proper url encoding
        task.execute("http://moodyrest.azurewebsites.net/moods" + "?username=" + username);
    }

    /**
     * @param latitude  latitude in degrees
     * @param longitude longitude in degrees
     * @param distance  distance from the origin in meters
     * @see android.community.erni.ernimoods.api.IMoodsBackend
     */
    public void getMoodsByLocation(Double latitude, Double longitude, Double distance) {
        // call AsyncTask to perform network operation on separate thread
        task.setListener(getMoodsListener);
        //call background task
        //TODO proper url encoding
        task.execute("http://moodyrest.azurewebsites.net/moods" + "?lat=" + latitude.toString() + "&lon=" + longitude.toString() + "&dist=" + distance.toString());
    }

    /**
     * Event handler to process an async http call to the moods-backend to retrieve moods
     */
    private InternetAccess.OnTaskCompleted getMoodsListener = new InternetAccess.OnTaskCompleted() {
        /**
         * Implementation of the interface's method
         *
         * @param result Response data as a string
         */
        public void onTaskCompleted(String result) {
            //If the sync-internet task has marked the response as an error message
            if (result.indexOf("Error") != -1) {
                //cut off the error-marker
                int pos = result.indexOf("{");
                String resultWithoutError = result.substring(pos);
                //call the json-error message parser. the parser creates an exception object passes it to the error handler
                if (errorListener != null) {
                    errorListener.onJSONResponseError(MoodsJSONParser.parserError(resultWithoutError));
                }
                //if there is no error message
            } else {
                if (listener != null) {
                    //parse the retrieved json-string and send the created list of mood objects to the listener
                    //return type is ArrayList<Mood>
                    listener.onConversionCompleted(MoodsJSONParser.getMoodsList(result));

                }
            }
        }
    };

    /**
     * Event handler to process an async http call to the moods-backend to post a mood
     */
    private InternetAccess.OnTaskCompleted postMoodListener = new InternetAccess.OnTaskCompleted() {
        /**
         * Implementation of the interface's method
         *
         * @param result Response data as a string
         * @throws JSONResponseException
         */
        public void onTaskCompleted(String result) {
            //cut off error marker
            if (result.indexOf("Error") != -1) {
                int pos = result.indexOf("{");
                String resultWithoutError = result.substring(pos);
                //call the json-error message parser. the parser creates an exception object passes it to the error handler
                if (errorListener != null) {
                    errorListener.onJSONResponseError(MoodsJSONParser.parserError(resultWithoutError));
                }
                //if there is no error message
            } else {
                if (listener != null) {
                    //parse the retrieved json-string and send id of the created mood object to the listener
                    //return type is String
                    //the mood object in the backend is created, even if there is no event attached
                    listener.onConversionCompleted(MoodsJSONParser.getId(result));
                }
            }
        }
    };

}
