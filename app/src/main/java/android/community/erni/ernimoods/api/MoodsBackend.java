package android.community.erni.ernimoods.api;

import android.community.erni.ernimoods.model.JSONResponseException;
import android.community.erni.ernimoods.model.LocationDeserializer;
import android.community.erni.ernimoods.model.LocationSerializer;
import android.community.erni.ernimoods.model.Mood;
import android.location.Location;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.OkHttpClient;

import java.util.List;
import java.util.concurrent.TimeUnit;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.OkClient;
import retrofit.client.Response;
import retrofit.converter.GsonConverter;
import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Implementation of the abstract class to query mood data from the backend
 */
public class MoodsBackend extends AbstractBackend {

    /**
     * This callback methods is called, if the retrofit api tries to gather
     * a list of mood objects. Either the response is automatically converted to
     * a list of Mood objects and forwarded to the registered listener, or an error message
     * is forwarded to the listener.
     */
    private final Callback listCallback = new Callback<List<Mood>>() {
        @Override
        public void success(List<Mood> moodsList, Response response) {
            if (listener != null) {
                listener.onConversionCompleted(moodsList);
            }
        }

        /**
         * On errors inside the framework, en error message is created
         *
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
     * Callback to retrieve a single mood object
     */
    private final Callback moodCallback = new Callback<Mood>() {
        @Override
        public void success(Mood mood, Response response) {
            if (listener != null) {
                listener.onConversionCompleted(mood);
            }
        }

        /**
         * On errors inside the framework, en error message is created
         *
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

    //instance variable of retrofit-service, which is actually used to query data from the ERNI-backend
    private MoodsService service;

    /**
     * Public constructor, which sets up the gson converter (automatic conversion from json to
     * our object-models. Additionally it creates the retrofit rest-adapter and specifies the
     * endpoint of the service
     */
    public MoodsBackend() {
        Gson gson = new GsonBuilder()
                //custom handling of location objects
                .registerTypeAdapter(Location.class, new LocationDeserializer())
                .registerTypeAdapter(Location.class, new LocationSerializer())
                        //specify the date format
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                .create();

        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.setReadTimeout(10 * 1000, TimeUnit.MILLISECONDS);

        restAdapter = new RestAdapter.Builder()
                .setEndpoint(MoodsService.SERVICE_ENDPOINT)
                .setClient(new OkClient(okHttpClient))
                .setConverter(new GsonConverter(gson))
                .build();

        service = restAdapter.create(MoodsService.class);
    }

    /**
     * Post a mood object
     * @param mood Mood object to post
     */
    public void postMood(Mood mood) {
        service.postMoodAPI(mood, postCallback);
    }

    public void getAllMoods() {
        service.getAllMoodsAPI(listCallback);
    }

    /**
     * Get all moods belonging to a certain user
     * @param username Specifies the user
     */
    public void getMoodsByUsername(String username) {
        service.getMoodsByUserAPI(username, listCallback);
    }

    /**
     * Get all mods inside a certain range
     * @param latitude  latitude in degrees
     * @param longitude longitude in degrees
     * @param distance  distance from the origin in meters
     */
    public void getMoodsByLocation(Double latitude, Double longitude, Double distance) {
        service.getMoodsByLocationAPI(latitude, longitude, distance, listCallback);
    }

    /**
     * Gather a mood-object by the id
     *
     * @param id
     */
    public void getMoodById(String id) {
        service.getMoodByIdAPI(id, moodCallback);
    }

    /**
     * delete a mood-object by specifying its id
     * @param id
     */
    public void deleteMood(String id) {
        service.deleteMoodAPI(id, rawCallback);
    }

    /**
     * This interface specifies all the methods that can be used together with moods-backend.
     * Retrofit-API is annotation based. To make use of the asynchronous handling and autmatic
     * json-conversion to model objects, an appropriate callback needs to be specified.
     */
    public interface MoodsService {
        //service endpoint of moods-backend
        String SERVICE_ENDPOINT = "http://moodyrest.azurewebsites.net";

        /**
         * Perform a get-request to get all moods
         * @param listCallback
         */
        @GET("/moods")
        void getAllMoodsAPI(Callback<List<Mood>> listCallback);

        /**
         * Perform a get request to get moods for a specified user
         * @param username
         * @param listCallback
         */
        @GET("/moods")
        void getMoodsByUserAPI(@Query("username") String username, Callback<List<Mood>> listCallback);

        /**
         * Perform a get request to get all moods around a location in a specified radius
         * @param lat
         * @param lon
         * @param dist
         * @param listCallback
         */
        @GET("/moods")
        //params ar query params in the url
        void getMoodsByLocationAPI(@Query("lat") Double lat, @Query("lon") Double lon, @Query("dist") Double dist, Callback<List<Mood>> listCallback);

        //perform a post-request to save a new mood
        @POST("/moods")
        void postMoodAPI(@Body Mood newMood, Callback<Response> postCallback);

        /**
         * Get a mood object based on its id
         * @param id
         * @param moodCallback
         */
        @GET("/moods/{id}")
        //the id is part of the path
        void getMoodByIdAPI(@Path("id") String id, Callback<Mood> moodCallback);

        /**
         * Delete a mood object based on its id
         * @param id
         * @param rawCallback
         */
        @DELETE("moods/{id}")
        //the id is part of the path
        void deleteMoodAPI(@Path("id") String id, Callback<Response> rawCallback);
    }
}



