package android.community.erni.ernimoods.model;

/**
 * represents a User object
 */
public class User {

    private String username;
    private String phone;
    private String email = "";
    private String password = "";
    private String id = "";

    /**
     * Constructor
     * Invoke this on signup
     *
     * @param username
     * @param phone
     * @param email
     * @param password
     */
    public User(String username, String phone, String email, String password) {

        setUsername(username);
        setPassword(password);
        setEmail(email);
        setPhone(phone);
    }

    public User(String username, String phone, String email, String password, String id) {

        setUsername(username);
        setPassword(password);
        setEmail(email);
        setPhone(phone);
        setId(id);
    }

    // getters and setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getID() {
        return this.id;
    }


}
