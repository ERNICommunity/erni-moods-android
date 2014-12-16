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

public class UserBackend extends AbstractBackend {

    /**
     * This callback method is called, if the retrofit api tries to gather
     * a user object. Either the response is automatically converted to
     * User object and forwarded to the registered listener, or an error message
     * is forwarded to the listener.
     */
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

    //instance variable of retrofit-service, which is actually used to query data from the ERNI-backend
    private UserService service;

    /**
     * Public constructor, which sets up the gson converter (automatic conversion from json to
     * our object-models). Additionally it creates the retrofit rest-adapter and specifies the
     * endpoint of the service
     */
    public UserBackend() {

        //OkHttpClient okHttpClient = new OkHttpClient();
        //okHttpClient.setReadTimeout(10 * 1000, TimeUnit.MILLISECONDS);
        restAdapter = new RestAdapter.Builder()
                .setEndpoint(UserService.SERVICE_ENDPOINT)
                        //.setClient(new OkClient(okHttpClient))
                .build();

        service = restAdapter.create(UserService.class);
    }

    /**
     * create a new user in the database
     * @param user User object
     */
    public void createUser(User user) {
        service.postUserAPI(user, postCallback);
    }

    /**
     * Get user information based on username+password
     *
     * @param username
     * @param password
     */
    public void getUserByPassword(String username, String password) {
        service.getUserByPasswordAPI(username, password, userCallback);
    }

    /**
     * Get user information based on username+phone
     * @param username
     * @param phone
     */
    public void getUserByPhone(String username, String phone) {
        service.getUserByPhoneAPI(username, phone, userCallback);
    }

    /**
     * Get user information based on a username and a valid user-id. The user-id
     * can belong to any user in the backend. This is used to retrieve user information
     * with e-mail, such that we can send out mails and texts to all users
     * @param username
     * @param key
     */
    public void getUserByKey(String username, String key) {
        service.getUserByKeyAPI(username, key, userCallback);
    }

    /**
     * Delete a user based on the object's id
     * @param id
     */
    public void deleteUser(String id) {
        service.deleteUserAPI(id, rawCallback);
    }

    /**
     * This interface specifies all the methods that can be used together with user-backend.
     * Retrofit-API is annotation based. To make use of the asynchronous handling and automatic
     * json-conversion to model objects, an appropriate callback needs to be specified.
     */
    public interface UserService {
        //service endpoint of moods-backend
        String SERVICE_ENDPOINT = "http://moodyrest.azurewebsites.net";

        /**
         * Perform a post-request to generate a new user-obkject in the db
         * @param newUser
         * @param postCallback
         */
        @POST("/users")
        void postUserAPI(@Body User newUser, Callback<Response> postCallback);

        /**
         * perform an http get-request to query user data
         * @param user
         * @param pwd
         * @param userCallback
         */
        @GET("/users/{user}/{pwd}")
        //username and password are part of the url-path
        void getUserByPasswordAPI(@Path("user") String user, @Path("pwd") String pwd, Callback<User> userCallback);

        /**
         * perform a get-request to query user data
         * @param user
         * @param phone
         * @param userCallback
         */
        @GET("/users/{user}/{phone}")
        //username and phone are path-variables
        void getUserByPhoneAPI(@Path("user") String user, @Path("phone") String phone, Callback<User> userCallback);

        /**
         * Get a user object based on a username and an arbitrary, valid user-id
         * @param user
         * @param key
         * @param userCallback
         */
        @GET("/users")
        //name and key are query-params of the request
        void getUserByKeyAPI(@Query("username") String user, @Query("key") String key, Callback<User> userCallback);

        /**
         * Perform a delete-request to delete a user based on the object's id
         * @param id
         * @param rawCallback
         */
        @DELETE("moods/{id}")
        void deleteUserAPI(@Path("id") String id, Callback<Response> rawCallback);
    }
}