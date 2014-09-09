package android.community.erni.ernimoods.api;

import android.community.erni.ernimoods.model.User;
import android.community.erni.ernimoods.service.MoodsJSONParser;

/**
 * Implementation of the abstract class to query user data from the moods-backend
 */
public class UserBackend implements IUserBackend {

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
    public void setErrorListener(IBackendEventHandler.OnJSONResponseError listener) {
        this.errorListener = listener;
    }

    /**
     * @param user User object
     * @see android.community.erni.ernimoods.api.IUserBackend
     */
    public void createUser(User user) {
        //set method of the internet task to post
        task.setMethod("POST");
        //attach a listener to handle the queried response-string
        task.setListener(createUserListener);
        //create a json string from the user update
        String jsonString = MoodsJSONParser.createJSONUser(user);
        //make a post-request to the user base-url
        task.execute("http://moodyrest.azurewebsites.net/users", jsonString);
    }

    public void getUserByPassword(String username, String password) {
        //attach a listener to handle the queried response-string
        task.setListener(getUserListener);
        //make a post-request to the user base-url
        //TODO proper url encoding
        task.execute("http://moodyrest.azurewebsites.net/users/" + username + "/" + password);
    }

    public void getUserByPhone(String username, String phone) {
        //attach a listener to handle the queried response-string
        task.setListener(getUserListener);
        //make a post-request to the user base-url
        //TODO proper url encoding
        task.execute("http://moodyrest.azurewebsites.net/users/" + username + "/" + phone);
    }

    /**
     * Event handler to process an async http call to the moods-backend to create a user
     */
    private InternetAccess.OnTaskCompleted createUserListener = new InternetAccess.OnTaskCompleted() {
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
                        errorListener.onJSONResponseError(MoodsJSONParser.parserError(resultWithoutError));
                    }
                    //if the response is no error message
                } else {
                    if (listener != null) {
                        //parse the response message. the id of the create user is extracted an returned
                        listener.onConversionCompleted(MoodsJSONParser.getId(result));
                    }
                }
            }
        }
    };

    /**
     * Event handler to process an async http call to the moods-backend to retrieve a user
     */
    private InternetAccess.OnTaskCompleted getUserListener = new InternetAccess.OnTaskCompleted() {
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
                        errorListener.onJSONResponseError(MoodsJSONParser.parserError(resultWithoutError));
                    }
                    //if the response is no error message
                } else {
                    if (listener != null) {
                        //parse the response message. return a User-Object
                        listener.onConversionCompleted(MoodsJSONParser.getUser(result));
                    }
                }
            }
        }
    };
}