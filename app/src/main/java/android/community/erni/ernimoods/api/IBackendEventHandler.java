package android.community.erni.ernimoods.api;

/**
 * Interface class that describes the event handlers for backend-calls
 */
public interface IBackendEventHandler {
    /**
     * This interface can be implemented by a class using the backend. The event handler is called
     * as soon as the required data has been loaded and parsed.
     *
     * @param <T> The parameter type depends on the method you are calling.
     */
    public interface OnConversionCompleted<T> {
        /**
         * This method has to be implemented to handle the retrieved data from the backend
         *
         * @param apiResponse Contains the return value (generic data type)
         */
        void onConversionCompleted(T apiResponse);
    }

    /**
     * This interface can be implemented by classes using the Moods-backend. It allows to retrieve a
     * json-message that specifies an error in the mood-backend
     */
    public interface OnJSONResponseError {
        /**
         * Implement method in order to handle an Error Message from the backend.
         *
         * @param error Specific error type for Moods-backend errors
         * @see android.community.erni.ernimoods.api.JSONResponseException
         */
        void onJSONResponseError(JSONResponseException error);
    }
}
