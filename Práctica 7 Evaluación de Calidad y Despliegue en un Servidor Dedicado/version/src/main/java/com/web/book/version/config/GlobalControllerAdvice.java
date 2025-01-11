package com.web.book.version.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {

    @Autowired
    private APIKeyConfig apiKeyConfig;

    @ModelAttribute("googleAuthEnabled")
    public boolean getGoogleAuthEnabled() {
        return apiKeyConfig.isGoogleAuthEnabled();
    }
}
