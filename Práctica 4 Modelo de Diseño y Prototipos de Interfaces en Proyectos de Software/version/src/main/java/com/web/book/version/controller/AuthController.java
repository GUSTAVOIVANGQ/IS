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

@Controller
//@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

//    @Autowired
//    private PasswordEncoder passwordEncoder;
    
    @GetMapping("/auth/login")
    public String login() {
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

    @PostMapping("/auth/register") 
    public String registerUser(@ModelAttribute @Valid User user, BindingResult result) {
        try {
            // Set default values
            user.setFechaRegistro(new java.util.Date());
            user.setRol(RoleType.USER);
            user.setActivo(true);
            
            // Save user
            User savedUser = userService.save(user);
            
            return "redirect:/auth/login?success";
        } catch (Exception e) {
            return "auth/register?error";
        }
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