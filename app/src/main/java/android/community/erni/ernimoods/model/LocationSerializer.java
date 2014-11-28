package android.community.erni.ernimoods.model;

import android.location.Location;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

/**
 * Since the backend provides the location as an array of coordinates and our Model-Object
 * needs a Location-object, the GSON-library needs a custom serialization class to post moods.
 */
public class LocationSerializer implements JsonSerializer<Location> {
    public JsonElement serialize(Location t, Type type,
                                 JsonSerializationContext jsc) {
        JsonArray ja = new JsonArray();
        ja.add(new JsonPrimitive(t.getLatitude()));
        ja.add(new JsonPrimitive(t.getLongitude()));
        return ja;
    }

}
