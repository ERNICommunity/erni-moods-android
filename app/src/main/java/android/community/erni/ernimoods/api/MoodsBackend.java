package android.community.erni.ernimoods.api;

import android.community.erni.ernimoods.model.Mood;
import android.community.erni.ernimoods.service.MoodsJSONParser;
import android.net.Uri;

/**
 * Implementation of the abstract class to query mood data from the backend
 */
public class MoodsBackend implements IMoodsBackend {

    private OnConversionCompleted listener = null; //stores the event listener for completed http-response parsing
    private InternetAccess task = new InternetAccess(); //class variable to store an Internet-Access object
    //event handler for an error message handler
    private OnJSONResponseError errorListener;
    private Uri.Builder baseUri = new Uri.Builder();

    public MoodsBackend() {
        baseUri.scheme("http");
        baseUri.authority("moodyrest.azurewebsites.net");
        baseUri.appendPath("moods");
    }

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
        task.execute(baseUri.toString(), jsonString);
    }


    /**
     * @see android.community.erni.ernimoods.api.IMoodsBackend
     */
    public void getAllMoods() {
        // attach listener

        task.setListener(getMoodsListener);
        //start async task
        task.execute(baseUri.toString());
    }

    /**
     * @param username Specifies the user
     * @see android.community.erni.ernimoods.api.IMoodsBackend
     */
    public void getMoodsByUsername(String username) {
        // call AsyncTask to perform network operation on separate thread
        task.setListener(getMoodsListener);
        //call background task
        baseUri.appendQueryParameter("username", username);
        task.execute(baseUri.toString());
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
        baseUri.appendQueryParameter("lat", latitude.toString());
        baseUri.appendQueryParameter("lon", longitude.toString());
        baseUri.appendQueryParameter("dist", distance.toString());
        task.execute(baseUri.toString());
    }

    public void getMoodById(String id) {
        task.setListener(getMoodsListener);
        //call background task
        baseUri.appendPath(id);
        task.execute(baseUri.toString());
    }

    public void deleteMood(String id) {
        task.setMethod("DELETE");
        task.setListener(deleteMoodListener);
        baseUri.appendPath(id);
        task.execute(baseUri.toString());
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

    /**
     * Event handler to process an async http call to the moods-backend to delete a mood
     */
    private InternetAccess.OnTaskCompleted deleteMoodListener = new InternetAccess.OnTaskCompleted() {
        /**
         * Implementation of the interface's method
         *
         * @param result Response data as a string
         */
        public void onTaskCompleted(String result) {
            //cut off error marker
            if (result.indexOf("Error") != -1) {
                //if there is an error, create an error message
                if (errorListener != null) {
                    errorListener.onJSONResponseError(new JSONResponseException("The moods object could not be deleted. The object with the specified id was not found", "Ressource not found"));
                }
                //if there is no error message
            } else {
                if (listener != null) {
                    //parse the retrieved json-string and send id of the created mood object to the listener
                    //return type is String
                    //the mood object in the backend is created, even if there is no event attached
                    listener.onConversionCompleted(true);
                }
            }
        }
    };

}
