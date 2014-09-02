package android.community.erni.ernimoods.api;

import android.community.erni.ernimoods.model.Mood;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by Admin on 02.09.2014.
 */
public class MoodsBackend implements IMoodsBackend {

    private final String USER_AGENT = "Mozilla/5.0";

    public void postMood(Mood mood){

    }
    public void updateMood(Mood mood){

    }
    public ArrayList<Mood> getAllMoods(){
        // call AsynTask to perform network operation on separate thread
        new HttpAsyncTask().execute("http://moodyrest.azurewebsites.net/moods");

        // todo: implement logic that gets data back from the async task

        // todo: convert the JSON string into the mood object list

        // todo : return mood object list
        return new ArrayList<Mood>(){};
    }

    private static String GET(String url){
        InputStream inputStream = null;
        String result = "";
        try {

            // create HttpClient
            HttpClient httpclient = new DefaultHttpClient();

            // make GET request to the given URL
            HttpResponse httpResponse = httpclient.execute(new HttpGet(url));

            // receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();

            // convert inputstream to string
            if(inputStream != null)
                result = convertInputStreamToString(inputStream);
            else
                result = "Did not work!";

        } catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
        }

        return result;
    }

    // convert inputstream to String
    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;
    }

    private class HttpAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            return GET(urls[0]);
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            //writes the JSON string to the Logcat...
            Log.d("Mood REST JSON string", result);
            // todo: implement mechanism to give result back to main thread
        }
    }
}
