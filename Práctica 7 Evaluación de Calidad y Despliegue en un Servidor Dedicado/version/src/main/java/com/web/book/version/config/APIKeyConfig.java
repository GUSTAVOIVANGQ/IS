package com.web.book.version.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component("apiKeyConfig") // Añadir nombre específico al bean
@ConfigurationProperties(prefix = "api.keys")
public class APIKeyConfig {
    private String googleClientId = "disabled";
    private String googleClientSecret = "disabled";
    private boolean googleAuthEnabled = false;

    // Getters and setters
    public String getGoogleClientId() {
        return googleClientId;
    }

    public void setGoogleClientId(String googleClientId) {
        this.googleClientId = googleClientId;
        this.googleAuthEnabled = googleClientId != null && !googleClientId.isEmpty();
    }

    public String getGoogleClientSecret() {
        return googleClientSecret;
    }

    public void setGoogleClientSecret(String googleClientSecret) {
        this.googleClientSecret = googleClientSecret;
    }

    public boolean isGoogleAuthEnabled() {
        return googleAuthEnabled && 
               googleClientId != null && 
               !googleClientId.equals("disabled") && 
               googleClientSecret != null && 
               !googleClientSecret.equals("disabled");
    }
}
