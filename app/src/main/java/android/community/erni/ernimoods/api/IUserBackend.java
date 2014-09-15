package android.community.erni.ernimoods.api;

import android.community.erni.ernimoods.model.User;

/**
 * This interface specifies the call methods to interact with the Moods-Backend for User-queries
 */
public interface IUserBackend extends IBackendEventHandler {
    /**
     * Method to create a user. Needs a user object. E-Mailaddress attribute is optional
     *
     * @param user User object
     * @see android.community.erni.ernimoods.model.User
     */
    public void createUser(User user);

    /**
     * Method to get userdata with a password and a username
     *
     * @param username Username
     * @param password Password
     */
    public void getUserByPassword(String username, String password);

    /**
     * Method to get userdata with username and phone number
     *
     * @param username Username
     * @param phone    Password
     */
    public void getUserByPhone(String username, String phone);

    /**
     * Delete a user by providing its id
     *
     * @param id user object-id
     */
    public void deleteUser(String id);

}
