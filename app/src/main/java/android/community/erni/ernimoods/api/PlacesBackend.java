package android.community.erni.ernimoods.api;

import android.community.erni.ernimoods.service.MoodsJSONParser;
import android.net.Uri;

/**
 * Created by ue65403 on 19.09.2014.
 */
public class PlacesBackend implements IPlacesBackend {

    private OnConversionCompleted listener = null; //stores the event listener for completed http-response parsing
    private InternetAccess task = new InternetAccess(); //class variable to store an Internet-Access object
    //event handler for an error message handler
    private OnJSONResponseError errorListener;
    private Uri.Builder baseUri = new Uri.Builder();
    private int maxResults = 10;

    public PlacesBackend() {
        baseUri.scheme("https");
        baseUri.authority("maps.googleapis.com");
        baseUri.appendPath("maps");
        baseUri.appendPath("api");
        baseUri.appendPath("place");
        baseUri.appendPath("nearbysearch");
        baseUri.appendPath("json");
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
    public void setErrorListener(IBackendEventHandler.OnJSONResponseError listener) {
        this.errorListener = listener;
    }

    public void getBars(Double lat, Double lng, Integer radius, Integer maxResults) {
        this.maxResults = maxResults;
        //attach a listener to handle the queried response-string
        task.setListener(getPlacesListener);
        //make a post-request to the user base-url
        baseUri.appendQueryParameter("key", "AIzaSyC0DFg9ARJTr3I_52lXEk_q58jzO-fb_S0");
        baseUri.appendQueryParameter("location", Double.toString(lat) + "," + Double.toString(lng));
        baseUri.appendQueryParameter("types", "bar");
        baseUri.appendQueryParameter("radius", Integer.toString(radius));
        baseUri.appendQueryParameter("rankBy", "distance");
        task.execute(baseUri.toString());
    }

    private InternetAccess.OnTaskCompleted getPlacesListener = new InternetAccess.OnTaskCompleted() {
        /**
         * Implementation of the interface's method
         * @param result Response status as a string
         */
        public void onTaskCompleted(String result) {
            if (listener != null) {
                if (result.indexOf("Error") != -1) {
                    int pos = result.indexOf("{");
                    String resultWithoutError = result.substring(pos);
                    //call the json-error message parser. the parser creates an exception object passes it to the error handler
                    if (errorListener != null) {
                        errorListener.onJSONResponseError(new JSONResponseException("Google Places could not be accessed", "Places API"));
                    }
                    //if the response is no error message
                } else {
                    if (listener != null) {
                        //parse the response message. the id of the create user is extracted an returned
                        listener.onConversionCompleted(MoodsJSONParser.parsePlacesJSON(result, maxResults));
                    }
                }
            }
        }
    };
}
