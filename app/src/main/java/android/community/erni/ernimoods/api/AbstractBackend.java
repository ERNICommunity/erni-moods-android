package android.community.erni.ernimoods.api;

import android.community.erni.ernimoods.model.JSONResponseException;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedInput;

/**
 * This class describes all the methods, which are common to the three backend-classes Moods-, User- and Places.
 * The backend uses retrofit-api for rest-communication
 */
public abstract class AbstractBackend implements IBackendEventHandler {
    //RestAdapter object to user retrofit-API
    protected RestAdapter restAdapter;
    //whenever a backend-call completed, the calling classes callback is called
    protected OnConversionCompleted listener = null;
    //same if an error occured
    protected OnJSONResponseError errorListener;

    /**
     * This method is used to exploit a raw response from the retrofit rest-api,
     * if there is no appropriate model to automatically convert it by using gson. This method
     * is mainly used to extract DB-ids out of the response's status message
     */
    protected final Callback rawCallback = new Callback<Response>() {
        @Override
        /**
         * On successful communication call the listener
         */
        public void success(Response myResponse, Response response) {
            if (listener != null) {
                //return the DB-id with the handler
                listener.onConversionCompleted(getId(myResponse));
            }
        }

        /**
         * On errors inside the framework, en error message is created
         * @param retrofitError
         */
        @Override
        public void failure(RetrofitError retrofitError) {
            JSONResponseException error = getResponseException(retrofitError);
            Log.d("Error", error.toString());
            if (errorListener != null) {
                errorListener.onJSONResponseError(error);
            }
        }
    };

    /**
     * Similar to the raw callback, this method is used by the extending backend-classes
     * to obtain an ID, after an object had been posted
     */
    protected final Callback postCallback = new Callback<Response>() {
        @Override
        public void success(Response resp, Response response) {
            if (listener != null) {
                listener.onConversionCompleted(getId(response));
            }
        }

        /**
         * On errors inside the framework, en error message is created
         * @param retrofitError
         */
        @Override
        public void failure(RetrofitError retrofitError) {
            JSONResponseException error = getResponseException(retrofitError);
            Log.d("Error", error.toString());
            if (errorListener != null) {
                errorListener.onJSONResponseError(error);
            }
        }
    };


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
     * This method extracts the error message and code from a json-error message from
     * the ERNI backend
     *
     * @param err Retrofit error object
     * @return Exception Object contain the exception information
     */
    protected JSONResponseException getResponseException(RetrofitError err) {
        try {
            //create new object
            JSONObject jsonCode = new JSONObject(getResponseBodyAsString(err.getResponse()));
            //get the code-attribute and the method-attribute and create a new exception
            JSONResponseException e = new JSONResponseException(jsonCode.getString("message"), jsonCode.getString("code"));
            return e;
        } catch (JSONException e) {
            e.printStackTrace();
            return new JSONResponseException("Could not convert JSON-Error-message", "Conversion error");
        }
        //if convertion failed, create an artificial error message
    }

    /**
     * Extract an id from a raw http-response
     *
     * @param r Retrofit-response
     * @return extracted id as a string
     */
    private String getId(Response r) {
        //try to parse the json-string
        try {
            //create new object
            JSONObject jsonCode = new JSONObject(getResponseBodyAsString(r));
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
     * The raw response from retrofit is a common http-response with
     * a body as a ByteStream. This stream is converted to a string.
     *
     * @param r Retrofit response
     * @return Response body as a string
     */
    private String getResponseBodyAsString(Response r) {
        TypedInput body = r.getBody();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(body.in()));
            StringBuilder out = new StringBuilder();
            String newLine = System.getProperty("line.separator");
            String line;
            while ((line = reader.readLine()) != null) {
                out.append(line);
                out.append(newLine);
            }
            return out.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }


}
