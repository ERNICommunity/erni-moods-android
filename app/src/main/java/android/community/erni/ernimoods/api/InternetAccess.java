package android.community.erni.ernimoods.api;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * This class allows to get a string through a REST http request
 */

//TODO check connectivity before starting any activity
public class InternetAccess extends AsyncTask<String, Void, String> {

    //Default method for a request is get
    private String method = "GET";

    /**
     * Interface that allows to implement a resultEventHandler in other classes. Need to be implemented
     * in classes using the class InternetAccess in order to retrieve the data.
     */
    public interface OnTaskCompleted {
        /**
         * Needs to be implemented in class that uses the InternetAccess
         *
         * @param result Response data as a string
         * @throws android.community.erni.ernimoods.api.JSONResponseException If the JSON-response
         *                                                                    is an error message an exception is thrown and handled by this class
         */
        void onTaskCompleted(String result);
    }

    //stores the event listener for a completed internet request
    private OnTaskCompleted listener;

    /**
     * Setter for the event handler of a completed internet request. Ist used by the Backend-classes
     *
     * @param listener
     */
    public void setListener(OnTaskCompleted listener) {
        this.listener = listener;
    }

    /**
     * Set the method of the http call
     * "GET"
     * "POST"
     * "DELETE"
     *
     * @param method one of the options to set the method
     */
    public void setMethod(String method) {
        this.method = method;
    }

    /**
     * Helper method for classes parsing the retrieved data. If there is an error message (http-code
     * 404 or 500, this can be added to the response-string in order to mark a message as erroneous.
     *
     * @param resultString Add the prefix "Error" to this string
     * @return String with added prefix "Error"
     */
    private static String addErrorCode(String resultString) {
        return "Error" + resultString;
    }


    /**
     * Method to actually query get data
     *
     * @param url REST url
     * @return queried url
     */
    private static String GET(String url) {
        InputStream inputStream = null;
        String result = "";
        try {
            // create HttpClient
            HttpClient httpclient = new DefaultHttpClient();
            httpclient.getParams().setParameter("http.socket.timeout", new Integer(5000));
            // make GET request to the given URL
            HttpResponse httpResponse = httpclient.execute(new HttpGet(url));
            // receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();
            // convert inputstream to string
            result = convertInputStreamToString(inputStream);
            //check whether the status-code in the header indicates an erroneous processing
            if (httpResponse.getStatusLine().getStatusCode() == 404 || httpResponse.getStatusLine().getStatusCode() == 500) {
                //add the Prefix "Error"
                //TODO Is there a mor elegant way do to it?
                result = addErrorCode(result);
            }
        } catch (Exception e) {
            //Log.d("InputStream", e.getLocalizedMessage());
        }
        return result;
    }

    /**
     * Method to perform a delete request
     *
     * @param url REST url
     * @return status message if the request wasn't successful
     */
    private static String DELETE(String url) {
        InputStream inputStream = null;
        String result = "";
        try {
            // create HttpClient
            HttpClient httpclient = new DefaultHttpClient();
            httpclient.getParams().setParameter("http.socket.timeout", new Integer(5000));
            // make DELETE request to the given URL
            HttpResponse httpResponse = httpclient.execute(new HttpDelete(url));
            result = "";
            //check whether the status-code in the header indicates an erroneous processing
            if (httpResponse.getStatusLine().getStatusCode() == 404 || httpResponse.getStatusLine().getStatusCode() == 500) {
                //add the Prefix "Error"
                result = "Error";
            }
        } catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
        }
        //return string Error if there was an error, empty string if everything went fine
        return result;
    }

    /**
     * Method to perform a post request
     *
     * @param url REST url
     * @return queried url
     */
    private static String POST(String url, String payload) {
        InputStream inputStream = null;
        String result = "";
        try {
            // 1. create HttpClient
            HttpClient httpclient = new DefaultHttpClient();
            httpclient.getParams().setParameter("http.socket.timeout", new Integer(5000));
            // 2. make POST request to the given URL
            HttpPost httpPost = new HttpPost(url);
            // 5. set json to StringEntity
            StringEntity se = new StringEntity(payload);
            //6. set httpPost Entity
            httpPost.setEntity(se);
            // 7. Set some headers to inform server about the type of the content
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");
            // 8. Execute POST request to the given URL
            HttpResponse httpResponse = httpclient.execute(httpPost);
            // 9. receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();
            // 10. convert inputstream to string
            result = convertInputStreamToString(inputStream);
            //check whether there is a processing error in the backend and optionally add
            //the "Error" Prefix
            //TODO Better solution to do it?
            if (httpResponse.getStatusLine().getStatusCode() == 404 || httpResponse.getStatusLine().getStatusCode() == 500) {
                result = addErrorCode(result);
            }
        } catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
        }
        // 11. return result
        return result;
    }

    /**
     * convert input stream to string
     *
     * @param inputStream
     * @return converted Stream
     * @throws IOException
     */
    public static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while ((line = bufferedReader.readLine()) != null)
            result += line;
        inputStream.close();
        return result;
    }

    /**
     * the actual background task
     *
     * @param urls url to query
     * @return data retrieved from the http call
     */
    @Override
    protected String doInBackground(String... urls) {
        //perform a get query
        if (method == "GET") {
            return GET(urls[0]);
        } else if (method == "POST") {
            return POST(urls[0], urls[1]);
        } else if (method == "DELETE") {
            return DELETE(urls[0]);

        } else {
            return "";
        }
    }

    /**
     * Call the eventListener as soon as the task has finished
     *
     * @param result result as a string
     */
    @Override
    protected void onPostExecute(String result) {
        //if there is and event handler attached, pass the retrieved string to the handler
        if (listener != null) {
            listener.onTaskCompleted(result);
        }

    }

}

