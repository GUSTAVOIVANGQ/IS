package com.web.book.version.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.beans.factory.annotation.Autowired;
import com.web.book.version.service.UserService;
import com.web.book.version.model.User;
import com.web.book.version.model.RoleType;
import java.util.Date;

@Configuration
@ConditionalOnProperty(
    prefix = "api.keys",
    name = "google-auth-enabled",
    havingValue = "true",
    matchIfMissing = false
)
public class OAuth2Config {
    
    private final UserService userService;
    private final APIKeyConfig apiKeyConfig;

    @Autowired
    public OAuth2Config(UserService userService, APIKeyConfig apiKeyConfig) {
        this.userService = userService;
        this.apiKeyConfig = apiKeyConfig;
    }

    @Bean
    @ConditionalOnProperty(prefix = "api.keys", name = "google-auth-enabled", havingValue = "true")
    public OAuth2UserService<OAuth2UserRequest, OAuth2User> oauth2UserService() {
        if (!apiKeyConfig.isGoogleAuthEnabled()) {
            return null;
        }
        DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
        
        return request -> {
            OAuth2User oauth2User = delegate.loadUser(request);
            
            String email = oauth2User.getAttribute("email");
            String name = oauth2User.getAttribute("name");
            
            // Verificar si el usuario ya existe
            if (!userService.existsByEmail(email)) {
                User newUser = new User();
                newUser.setEmail(email);
                newUser.setUsername(email); // Usar email como username
                newUser.setNombre(name);
                newUser.setPassword(email); // Contraseña vacía para usuarios de OAuth
                newUser.setFechaRegistro(new Date());
                newUser.setRol(RoleType.USER);
                newUser.setActivo(true);
                
                userService.save(newUser);
            }
            
            return oauth2User;
        };
    }
}
