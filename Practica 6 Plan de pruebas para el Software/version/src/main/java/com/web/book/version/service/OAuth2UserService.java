package com.web.book.version.service;

import com.web.book.version.model.User;
import com.web.book.version.service.UserService;
import com.web.book.version.model.RoleType;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Date;

@Service
public class OAuth2UserService extends DefaultOAuth2UserService {

    private final UserService userService;
    private static final Logger logger = LoggerFactory.getLogger(OAuth2UserService.class);

    public OAuth2UserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);
        
        try {
            return processOAuth2User(userRequest, oauth2User);
        } catch (Exception ex) {
            logger.error("Error processing OAuth2 user", ex);
            throw new OAuth2AuthenticationException(ex.getMessage());
        }
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest userRequest, OAuth2User oauth2User) {
        // Extraer información del usuario de Google
        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");
        String pictureUrl = oauth2User.getAttribute("picture");

        if (email == null) {
            throw new OAuth2AuthenticationException("Email not found from OAuth2 provider");
        }

        User user;

        if (!userService.existsByEmail(email)) {
            user = createNewUser(email, name, pictureUrl);
        } else {
            user = userService.findByEmail(email)
                    .orElseThrow(() -> new OAuth2AuthenticationException("User not found with email: " + email));
            updateExistingUser(user, name, pictureUrl);
        }

        return oauth2User;
    }

    private User createNewUser(String email, String name, String pictureUrl) {
        User user = new User();
        user.setEmail(email);
        user.setUsername(email.split("@")[0]); // Usar la parte antes del @ como username
        user.setNombre(name);
        user.setPassword(""); // Contraseña vacía para usuarios OAuth
        user.setFechaRegistro(new Date());
        user.setRol(RoleType.USER);
        user.setActivo(true);
        user.setProfilePhotoUrl(pictureUrl);
        
        return userService.save(user);
    }

    private void updateExistingUser(User user, String name, String pictureUrl) {
        boolean needsUpdate = false;

        if (name != null && !name.equals(user.getNombre())) {
            user.setNombre(name);
            needsUpdate = true;
        }

        if (pictureUrl != null && !pictureUrl.equals(user.getProfilePhotoUrl())) {
            user.setProfilePhotoUrl(pictureUrl);
            needsUpdate = true;
        }

        if (needsUpdate) {
            userService.save(user);
        }
    }
}