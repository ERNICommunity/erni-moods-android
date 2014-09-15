package android.community.erni.ernimoods.api;

import android.community.erni.ernimoods.model.User;
import android.community.erni.ernimoods.service.MoodsJSONParser;
import android.net.Uri;

/**
 * Implementation of the abstract class to query user data from the moods-backend
 */

//TODO sending an uncrypted password hurts

public class UserBackend implements IUserBackend {

    private OnConversionCompleted listener = null; //stores the event listener for completed http-response parsing
    private InternetAccess task = new InternetAccess(); //class variable to store an Internet-Access object
    //event handler for an error message handler
    private OnJSONResponseError errorListener;
    private Uri.Builder baseUri = new Uri.Builder();

    public UserBackend() {
        baseUri.scheme("http");
        baseUri.authority("moodyrest.azurewebsites.net");
        baseUri.appendPath("users");
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
        task.execute(baseUri.toString(), jsonString);
    }

    public void getUserByPassword(String username, String password) {
        //attach a listener to handle the queried response-string
        task.setListener(getUserListener);
        //make a post-request to the user base-url
        baseUri.appendPath(username).appendPath(password);
        task.execute(baseUri.toString());
    }

    public void getUserByPhone(String username, String phone) {
        //attach a listener to handle the queried response-string
        task.setListener(getUserListener);
        //make a post-request to the user base-url
        baseUri.appendPath(username).appendPath(phone);
        task.execute(baseUri.toString());
    }


    public void deleteUser(String id) {
        task.setListener(deleteUserListener);
        task.setMethod("DELETE");
        //append the id of the user to the base url
        baseUri.appendPath(id);
        //start background task
        task.execute(baseUri.toString());
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

    /**
     * Event handler to process an async http call to the moods-backend to delete a mood
     */
    private InternetAccess.OnTaskCompleted deleteUserListener = new InternetAccess.OnTaskCompleted() {
        /**
         * Implementation of the interface's method
         *
         * @param result return true if the user has been delete
         */
        public void onTaskCompleted(String result) {
            if (result.indexOf("Error") != -1) {
                //if there is an error, create an error message
                if (errorListener != null) {
                    errorListener.onJSONResponseError(new JSONResponseException("The moods object could not be deleted. The object with the specified id was not found", "Ressource not found"));
                }
                //if there is no error message
            } else {
                if (listener != null) {
                    //return true to indicate that the user has been deleted
                    listener.onConversionCompleted(true);
                }
            }
        }
    };

}