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
 * Created by ue65403 on 18.11.2014.
 */
public abstract class AbstractBackend implements IBackendEventHandler {
    protected RestAdapter restAdapter;
    protected OnConversionCompleted listener = null;
    protected OnJSONResponseError errorListener;

    protected final Callback rawCallback = new Callback<Response>() {
        @Override
        public void success(Response myResponse, Response response) {
            if (listener != null) {
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
