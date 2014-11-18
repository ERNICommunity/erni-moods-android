package android.community.erni.ernimoods.api;

import android.community.erni.ernimoods.model.JSONResponseException;
import android.community.erni.ernimoods.model.User;
import android.util.Log;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Implementation of the abstract class to query user data from the moods-backend
 */

//TODO sending an uncrypted password hurts

public class UserBackend extends AbstractBackend {

    private final Callback userCallback = new Callback<User>() {
        @Override
        public void success(User user, Response response) {
            if (listener != null) {
                listener.onConversionCompleted(user);
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
    private UserBackend.UserService service;

    public UserBackend() {
        restAdapter = new RestAdapter.Builder()
                .setEndpoint(UserService.SERVICE_ENDPOINT)
                .build();

        service = restAdapter.create(UserService.class);
    }

    /**
     * @param user User object
     */
    public void createUser(User user) {
        service.postUserAPI(user, postCallback);
    }

    public void getUserByPassword(String username, String password) {
        service.getUserByPasswordAPI(username, password, userCallback);
    }

    public void getUserByPhone(String username, String phone) {
        service.getUserByPhoneAPI(username, phone, userCallback);
    }

    public void getUserByKey(String username, String key) {
        service.getUserByKeyAPI(username, key, userCallback);
    }

    public void deleteUser(String id) {
        service.deleteUserAPI(id, rawCallback);
    }

    public interface UserService {
        String SERVICE_ENDPOINT = "http://moodyrest.azurewebsites.net";

        @POST("/users")
        void postUserAPI(@Body User newUser, Callback<Response> postCallback);

        @GET("/users/{user}/{pwd}")
        void getUserByPasswordAPI(@Path("user") String user, @Path("pwd") String pwd, Callback<User> userCallback);

        @GET("/users/{user}/{phone}")
        void getUserByPhoneAPI(@Path("user") String user, @Path("phone") String phone, Callback<User> userCallback);

        @GET("/users")
        void getUserByKeyAPI(@Query("username") String user, @Query("key") String key, Callback<User> userCallback);

        @DELETE("moods/{id}")
        void deleteUserAPI(@Path("id") String id, Callback<Response> rawCallback);
    }
}