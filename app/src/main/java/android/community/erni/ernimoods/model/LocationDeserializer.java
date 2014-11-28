package android.community.erni.ernimoods.model;

import android.location.Location;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

/**
 * Since the backend provides the location as an array of coordinates and our Model-Object
 * needs a Location-object, the GSON-library needs a custom deserialization class.
 */
public class LocationDeserializer implements JsonDeserializer<Location> {
    public Location deserialize(JsonElement je, Type type,
                                JsonDeserializationContext jdc)
            throws JsonParseException {
        JsonArray locArray = je.getAsJsonArray();
        Location l = new Location("Backend");
        l.setLatitude(locArray.get(0).getAsDouble());
        l.setLongitude(locArray.get(1).getAsDouble());
        return l;
    }
}
