package android.community.erni.ernimoods.model;

import android.location.Location;

import com.google.gson.annotations.SerializedName;

/**
 * Created by ue65403 on 19.09.2014.
 */
public class GooglePlace {

    @SerializedName("geometry")
    private Location location;
    private String icon;
    private String name;
    private Double rating;
    @SerializedName("vicinity")
    private String address;

    public GooglePlace(String name, Location location, String address, Double rating, String icon) {
        setAddress(address);
        setIcon(icon);
        setLocation(location);
        setName(name);
        setRating(rating);
    }

    public GooglePlace() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
}
