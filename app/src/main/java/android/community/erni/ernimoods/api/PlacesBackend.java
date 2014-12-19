package android.community.erni.ernimoods.api;

import android.community.erni.ernimoods.model.JSONResponseException;
import android.community.erni.ernimoods.model.Places;
import android.location.Location;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.squareup.okhttp.OkHttpClient;

import java.lang.reflect.Type;
import java.util.concurrent.TimeUnit;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.OkClient;
import retrofit.client.Response;
import retrofit.converter.GsonConverter;
import retrofit.http.GET;
import retrofit.http.Query;

/**
 * Created by ue65403 on 19.09.2014.
 */
public class PlacesBackend extends AbstractBackend {

    /**
     * This callback method is called, if the retrofit api tries to gather
     * google places object from the google API. Either the response is automatically converted to
     * User object and forwarded to the registered listener, or an error message
     * is forwarded to the listener.
     */
    private final Callback listCallback = new Callback<Places>() {
        @Override
        public void success(Places places, Response response) {
            if (listener != null) {
                listener.onConversionCompleted(places.getResults());
            }
        }

        /**
         * On errors inside the framework, en error message is created
         *
         * @param retrofitError
         */
        @Override
        public void failure(RetrofitError retrofitError) {
            if (errorListener != null) {
                errorListener.onJSONResponseError(new JSONResponseException("Google Places could not be accessed", "Places API"));
            }
        }
    };

    //instance variable of retrofit-service, which is actually used to query data from the Google Places API
    private PlacesService service;

    /**
     * Public constructor, which sets up the gson converter (automatic conversion from json to
     * our object-models). Additionally it creates the retrofit rest-adapter and specifies the
     * endpoint of the service
     */
    public PlacesBackend() {
        Gson gson = new GsonBuilder()
                //deserialization of the location-attribute of our model is not straightforward
                //that's why we need a custom deserialization class
                .registerTypeAdapter(Location.class, new LocationDeserializer())
                .create();

        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.setReadTimeout(10 * 1000, TimeUnit.MILLISECONDS);

        restAdapter = new RestAdapter.Builder()
                .setEndpoint(PlacesService.SERVICE_ENDPOINT)
                .setClient(new OkClient(okHttpClient))
                .setConverter(new GsonConverter(gson))
                .build();

        service = restAdapter.create(PlacesService.class);
    }

    /**
     * Query 20 (default) bars from the Google places API within a range around a pair of coordinates
     *


     */
    public void getBars(String location, String placeTypes, boolean openNow) {
        //call to the retrofit-service
        String opennow = null;
        if (openNow) {
            opennow = "true";
        }
        service.getPlacesAPI(location, placeTypes, opennow, listCallback);
    }

    /**
     * This interface specifies all the methods that can be used together with Google Places API.
     * Retrofit-API is annotation based. To make use of the asynchronous handling and automatic
     * json-conversion to model objects, an appropriate callback needs to be specified.
     */
    public interface PlacesService {
        //service endpoint
        String SERVICE_ENDPOINT = "https://maps.googleapis.com";

        /**
         * HTTP-call to the places API. Types=bar, and the API-key are hardcoded.
         * @param location location as a string, containing lng and lat
         * @param listCallback
         */
        @GET("/maps/api/place/nearbysearch/json?rankby=distance&key=AIzaSyC0DFg9ARJTr3I_52lXEk_q58jzO-fb_S0")
        //all query params
        void getPlacesAPI(@Query("location") String location, @Query("types") String types, @Query("opennow") String openNow, Callback<Places> listCallback);
    }

    /**
     * Since the Google-places API provides the location as an array of coordinates and our Model-Object
     * needs a Location-object, the GSON-library needs a custom deserialization class.
     */
    private class LocationDeserializer implements JsonDeserializer<Location> {
        public Location deserialize(JsonElement je, Type type,
                                    JsonDeserializationContext jdc)
                throws JsonParseException {
            JsonObject geoObject = je.getAsJsonObject();
            JsonObject locObject = geoObject.getAsJsonObject("location");
            Location l = new Location("Places API");
            l.setLatitude(locObject.get("lat").getAsDouble());
            l.setLongitude(locObject.get("lng").getAsDouble());
            return l;
        }
    }
}
