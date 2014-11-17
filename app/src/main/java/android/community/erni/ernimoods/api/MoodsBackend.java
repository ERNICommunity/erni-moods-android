package android.community.erni.ernimoods.api;

import android.community.erni.ernimoods.model.Mood;
import android.community.erni.ernimoods.service.MoodsJSONParser;
import android.location.Location;
import android.net.Uri;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.converter.ConversionException;
import retrofit.converter.Converter;
import retrofit.converter.GsonConverter;
import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;
import retrofit.mime.TypedInput;
import retrofit.mime.TypedOutput;

/**
 * Implementation of the abstract class to query mood data from the backend
 */
public class MoodsBackend implements IMoodsBackend {

    private OnConversionCompleted listener = null; //stores the event listener for completed http-response parsing
    //event handler for an error message handler
    private OnJSONResponseError errorListener;
    private RestAdapter restAdapter;
    private MoodsService service;

    public interface MoodsService {
        String SERVICE_ENDPOINT = "http://moodyrest.azurewebsites.net";

        @GET("/moods")
        void getAllMoodsAPI(Callback<List<Mood>> listCallback);

        @GET("/moods")
        void getMoodsByUserAPI(@Query("username") String username, Callback<List<Mood>> listCallback);

        @GET("/moods")
        void getMoodsByLocationAPI(@Query("lat") Double lat, @Query("lon") Double lon, @Query("dist") Double dist, Callback<List<Mood>> listCallback);

        @POST("/moods")
        void postMoodAPI(@Body Mood newMood, Callback<Response> postCallback);

        @GET("/moods/{id}")
        void getMoodByIdAPI(@Path("id") String id, Callback<Mood> moodCallback);

        @DELETE("moods/{id}")
        void deleteMoodAPI(@Path("id") String id, Callback<Response> rawCallback);
    }


    public MoodsBackend() {
        Gson gson = new GsonBuilder()
                //.registerTypeAdapter(Date.class, new DateDeserializer())
                .registerTypeAdapter(Location.class, new LocationDeserializer())
                .registerTypeAdapter(Location.class, new LocationSerializer())
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                //.excludeFieldsWithoutExposeAnnotation()
                .create();

        restAdapter = new RestAdapter.Builder()
                .setEndpoint(MoodsService.SERVICE_ENDPOINT)
                .setConverter(new GsonConverter(gson))
                        .build();

        service = restAdapter.create(MoodsService.class);
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
        service.postMoodAPI(mood, postCallback);
    }


    /**
     * @see android.community.erni.ernimoods.api.IMoodsBackend
     */
    public void getAllMoods() {
        service.getAllMoodsAPI(listCallback);
    }

    /**
     * @param username Specifies the user
     * @see android.community.erni.ernimoods.api.IMoodsBackend
     */
    public void getMoodsByUsername(String username) {
        service.getMoodsByUserAPI(username, listCallback);
    }

    /**
     * @param latitude  latitude in degrees
     * @param longitude longitude in degrees
     * @param distance  distance from the origin in meters
     * @see android.community.erni.ernimoods.api.IMoodsBackend
     */
    public void getMoodsByLocation(Double latitude, Double longitude, Double distance) {
        service.getMoodsByLocationAPI(latitude, longitude, distance, listCallback);
    }

    public void getMoodById(String id) {
        service.getMoodByIdAPI(id, moodCallback);
    }

    public void deleteMood(String id) {
        service.deleteMoodAPI(id, rawCallback);
    }

    private String getResponseBodyAsString(Response r){
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

    private JSONResponseException getResponseException(RetrofitError err){
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

    private final Callback postCallback = new Callback<Response>() {
        @Override
        public void success(Response resp, Response response) {
            String id = getId(resp);
            Log.d("Post",getId(resp));
        }

        /**
         * On errors inside the framework, en error message is created
         * @param retrofitError
         */
        @Override
        public void failure(RetrofitError retrofitError) {
            JSONResponseException error = getResponseException(retrofitError);
            Log.d("Error",error.toString());
            if(errorListener != null){
                errorListener.onJSONResponseError(error);
            }
        }
    };

    private final Callback listCallback = new Callback<List<Mood>>() {
        @Override
        public void success(List<Mood> moodsList, Response response) {
            if(listener != null) {
                listener.onConversionCompleted(moodsList);
            }
        }

        /**
         * On errors inside the framework, en error message is created
         * @param retrofitError
         */
        @Override
        public void failure(RetrofitError retrofitError) {
            JSONResponseException error = getResponseException(retrofitError);
            Log.d("Error",error.toString());
            if(errorListener != null){
                errorListener.onJSONResponseError(error);
            }
        }
    };

    private final Callback moodCallback = new Callback<Mood>() {
        @Override
        public void success(Mood mood, Response response) {
            if(listener != null) {
                listener.onConversionCompleted(mood);
            }
        }

        /**
         * On errors inside the framework, en error message is created
         * @param retrofitError
         */
        @Override
        public void failure(RetrofitError retrofitError) {
            JSONResponseException error = getResponseException(retrofitError);
            Log.d("Error",error.toString());
            if(errorListener != null){
                errorListener.onJSONResponseError(error);
            }
        }
    };

    private final Callback rawCallback = new Callback<Response>() {
        @Override
        public void success(Response myResponse, Response response) {
            if(listener != null) {
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
            Log.d("Error",error.toString());
            if(errorListener != null){
                errorListener.onJSONResponseError(error);
            }
        }
    };

}

class LocationSerializer implements JsonSerializer<Location>
{
    public JsonElement serialize(Location t, Type type,
                                 JsonSerializationContext jsc)
    {
        JsonArray ja = new JsonArray();
        ja.add(new JsonPrimitive(t.getLatitude()));
        ja.add(new JsonPrimitive(t.getLongitude()));
        return ja;
    }

}

class LocationDeserializer implements JsonDeserializer<Location>
{
    public Location deserialize(JsonElement je, Type type,
                                JsonDeserializationContext jdc)
            throws JsonParseException
    {
        JsonArray locArray = je.getAsJsonArray();
        Location l = new Location("Backend");
        l.setLatitude(locArray.get(0).getAsDouble());
        l.setLongitude(locArray.get(1).getAsDouble());
        return l;
    }
}

