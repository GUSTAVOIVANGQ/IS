package com.web.book.version.controller;

import com.web.book.version.model.RoleType;
import com.web.book.version.model.User;
import com.web.book.version.service.UserService;
import jakarta.validation.Valid;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import com.web.book.version.config.APIKeyConfig;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import javax.servlet.http.HttpServletRequest;
import com.web.book.version.service.LoginAttemptService;


import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;

@Controller
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private APIKeyConfig apiKeyConfig;

    @Autowired
    private LoginAttemptService loginAttemptService;
    
    @GetMapping("/auth/login")
    public String login(Model model) {
        model.addAttribute("googleAuthEnabled", apiKeyConfig.isGoogleAuthEnabled());
        return "auth/login";
    }
    
    @GetMapping("/auth/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new User());
        return "auth/register";
    }

    @GetMapping("/hom")
    public String home() {
        return "home";
    }


    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginRequest.getUsername(),
                loginRequest.getPassword()
            )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        return ResponseEntity.ok("User logged in successfully!");
    }

    @PostMapping("/auth/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        try {
            // Verificar intentos de login fallidos
            String ipAddress = request.getRemoteAddr();
            if (loginAttemptService.isBlocked(ipAddress)) {
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Demasiados intentos fallidos. Por favor, espere antes de intentar nuevamente.");
            }

            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
                )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            return ResponseEntity.ok("Login successful");
        } catch (AuthenticationException e) {
            // Registrar intento fallido
            loginAttemptService.loginFailed(request.getRemoteAddr());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("Invalid credentials");
        }
    }

    @PostMapping("/auth/register") 
    public String registerUser(@ModelAttribute @Valid User user, BindingResult result, Model model) {
        try {
            // Validar entrada XSS
            if (containsXSS(user.getUsername()) || containsXSS(user.getEmail())) {
                result.rejectValue("username", "error.xss", "Invalid characters detected");
                model.addAttribute("user", user);
                return "auth/register";
            }

            // Validar si el usuario ya existe
            if (userService.existsByUsername(user.getUsername())) {
                result.rejectValue("username", "error.username", "Username already exists");
                model.addAttribute("user", user);
                return "auth/register";
            }

            // Validar errores de binding
            if (result.hasErrors()) {
                model.addAttribute("user", user);
                return "auth/register";
            }

            // Set default values
            user.setFechaRegistro(new java.util.Date());
            user.setRol(RoleType.USER);
            user.setActivo(true);
            
            // Save user
            userService.save(user);
            
            return "redirect:/auth/login?success";
        } catch (Exception e) {
            result.reject("error.global", "Registration failed: " + e.getMessage());
            model.addAttribute("user", user);
            return "auth/register";
        }
    }

    private boolean containsXSS(String value) {
        if (value == null) {
            return false;
        }
        // Lista básica de patrones XSS
        String[] xssPatterns = {
            "<script>", "</script>", "javascript:", "onerror=", "onload=",
            "eval(", "expression(", "onclick=", "onmouseover=", "alert("
        };
        
        value = value.toLowerCase();
        for (String pattern : xssPatterns) {
            if (value.contains(pattern.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
}

class LoginRequest {
    private String username;
    private String password;

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}