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

import java.lang.reflect.Type;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.converter.GsonConverter;
import retrofit.http.GET;
import retrofit.http.Query;

/**
 * Created by ue65403 on 19.09.2014.
 */
public class PlacesBackend extends AbstractBackend {

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
    private PlacesService service;

    public PlacesBackend() {
        Gson gson = new GsonBuilder()
                //.registerTypeAdapter(Date.class, new DateDeserializer())
                .registerTypeAdapter(Location.class, new LocationDeserializer())
                .create();

        restAdapter = new RestAdapter.Builder()
                .setEndpoint(PlacesService.SERVICE_ENDPOINT)
                .setConverter(new GsonConverter(gson))
                .build();

        service = restAdapter.create(PlacesService.class);
    }

    public void getBars(Double lat, Double lng, Integer radius, Integer maxResults) {
        service.getPlacesAPI(Double.toString(lat) + "," + Double.toString(lng), Integer.toString(radius), listCallback);
    }

    public interface PlacesService {
        String SERVICE_ENDPOINT = "https://maps.googleapis.com";

        @GET("/maps/api/place/nearbysearch/json?types=bar&rankgy=distance&key=AIzaSyC0DFg9ARJTr3I_52lXEk_q58jzO-fb_S0")
        void getPlacesAPI(@Query("location") String location, @Query("radius") String radius, Callback<Places> listCallback);
    }

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
