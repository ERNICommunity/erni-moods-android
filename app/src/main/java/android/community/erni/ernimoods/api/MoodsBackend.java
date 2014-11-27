package android.community.erni.ernimoods.api;

import android.community.erni.ernimoods.model.JSONResponseException;
import android.community.erni.ernimoods.model.LocationDeserializer;
import android.community.erni.ernimoods.model.LocationSerializer;
import android.community.erni.ernimoods.model.Mood;
import android.location.Location;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.List;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
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
    private MoodsService service;
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
     * @param mood Mood object to post
     */
    public void postMood(Mood mood) {
        service.postMoodAPI(mood, postCallback);
    }

    public void getAllMoods() {
        service.getAllMoodsAPI(listCallback);
    }

    /**
     * @param username Specifies the user
     */
    public void getMoodsByUsername(String username) {
        service.getMoodsByUserAPI(username, listCallback);
    }

    /**
     * @param latitude  latitude in degrees
     * @param longitude longitude in degrees
     * @param distance  distance from the origin in meters
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
}



