package android.community.erni.ernimoods.api;

/**
 * Created by ue65403 on 19.09.2014.
 */
public interface IPlacesBackend extends IBackendEventHandler {
    public void getBars(Double lat, Double lng, Integer radius, Integer maxResults);
}
