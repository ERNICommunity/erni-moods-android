package android.community.erni.ernimoods.model;

/**
 * A custom exception class, which is used to handle error-codes from the moods-backend. If
 * the database is accessed in a wrong way, the request can be executed without any problems. How
 * ever we need a mechanism to access these codes.
 */
public class JSONResponseException extends Exception {

    private String code; //storage for the error code
    private String message; //storage for the error message

    /**
     * Constructor overrides the original Exception-constructor. The object ist created by passing
     * the error code an the error message
     *
     * @param message   message-body of an error
     * @param errorCode message code
     */
    public JSONResponseException(String message, String errorCode) {
        //Pass the error string to the original constructor
        super("Error Code: " + errorCode + "; Error Message " + message);
        //store the error code and message
        this.code = errorCode;
        this.message = message;
    }

    /**
     * Getter method for the error code
     *
     * @return Error code
     */
    public String getErrorCode() {
        return this.code;
    }

    /**
     * Getter method for the error message
     *
     * @return Error message
     */
    public String getErrorMessage() {
        return this.message;
    }
}

