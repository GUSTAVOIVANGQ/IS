package com.web.book.version.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.web.book.version.model.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.ui.Model;

import com.web.book.version.service.FileUploadService;
import com.web.book.version.service.UserService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
@RequestMapping("/user")
public class ProfileController {

    private static final Logger logger = LoggerFactory.getLogger(ProfileController.class);
    
    @Autowired
    private final FileUploadService fileUploadService;
    @Autowired
    private UserService userService;

    @Autowired
    public ProfileController(FileUploadService fileUploadService, UserService userService) {
        this.fileUploadService = fileUploadService;
        this.userService = userService;
    }
    
    
    
    @GetMapping("/profile")
    public String showProfile(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            if (userDetails == null) {
                return "redirect:/auth/login";
            }
            
            User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
                
            model.addAttribute("user", user);
            return "user/profile";
            
        } catch (Exception e) {
            logger.error("Error showing profile: ", e);
            return "redirect:/error";
        }
    }

    @PostMapping("/profile/photo")
    public String uploadProfilePhoto(@RequestParam("photo") MultipartFile file,
                                   @AuthenticationPrincipal UserDetails userDetails) {
        try {
            if (userDetails == null) {
                return "redirect:/auth/login";
            }
            
            String filename = fileUploadService.uploadProfilePhoto(file, userDetails.getUsername());
            userService.updateProfilePhoto(userDetails.getUsername(), filename);
            return "redirect:/user/profile";
            
        } catch (IOException e) {
            return "redirect:/user/profile?error=upload";
        }
    }
}