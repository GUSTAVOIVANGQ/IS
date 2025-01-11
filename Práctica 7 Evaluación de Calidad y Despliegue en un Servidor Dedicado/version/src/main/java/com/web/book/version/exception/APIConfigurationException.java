package com.web.book.version.exception;

public class APIConfigurationException extends RuntimeException {
    private final String apiName;
    private final String errorCode;

    public APIConfigurationException(String message) {
        super(message);
        this.apiName = "UNKNOWN";
        this.errorCode = "CONFIG_ERROR";
    }

    public APIConfigurationException(String message, Throwable cause) {
        super(message, cause);
        this.apiName = "UNKNOWN";
        this.errorCode = "CONFIG_ERROR";
    }

    public APIConfigurationException(String apiName, String message) {
        super(message);
        this.apiName = apiName;
        this.errorCode = "CONFIG_ERROR";
    }

    public APIConfigurationException(String apiName, String errorCode, String message) {
        super(message);
        this.apiName = apiName;
        this.errorCode = errorCode;
    }

    public APIConfigurationException(String apiName, String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.apiName = apiName;
        this.errorCode = errorCode;
    }

    public String getApiName() {
        return apiName;
    }

    public String getErrorCode() {
        return errorCode;
    }

    @Override
    public String toString() {
        return String.format("APIConfigurationException[API: %s, Error Code: %s, Message: %s]", 
            apiName, errorCode, getMessage());
    }

    // Métodos de utilidad estáticos para crear excepciones comunes
    public static APIConfigurationException missingGoogleClientId() {
        return new APIConfigurationException(
            "Google OAuth2",
            "MISSING_CLIENT_ID",
            "Google OAuth2 client ID is not configured"
        );
    }

    public static APIConfigurationException missingGoogleClientSecret() {
        return new APIConfigurationException(
            "Google OAuth2",
            "MISSING_CLIENT_SECRET",
            "Google OAuth2 client secret is not configured"
        );
    }

    public static APIConfigurationException invalidConfiguration(String details) {
        return new APIConfigurationException(
            "Google OAuth2",
            "INVALID_CONFIG",
            "Invalid OAuth2 configuration: " + details
        );
    }
}
