package com.web.book.version.controller;

import com.web.book.version.model.User;
import com.web.book.version.service.UserService;
import com.web.book.version.service.FileUploadService;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private FileUploadService fileUploadService;

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);
    
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        List<User> users = userService.findAllUsers();
        logger.info("Found {} users", users.size());
        model.addAttribute("users", users);
        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String listUsers(Model model) {
        List<User> users = userService.findAllUsers();
        model.addAttribute("users", users);
        return "admin/users";
    }

    @GetMapping("/users/{id}/edit")
    public String editUser(@PathVariable Long id, Model model) {
        User user = userService.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found"));
        model.addAttribute("user", user);
        return "admin/edit-user";
    }

    @PostMapping("/users/{id}/update")
    public String updateUser(@PathVariable Long id, @ModelAttribute User user) {
        userService.updateUser(id, user);
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/photo")
    public String updateUserPhoto(@PathVariable Long id, @RequestParam("photo") MultipartFile file) {
        try {
            String filename = fileUploadService.uploadProfilePhoto(file, id.toString());
            userService.updateUserProfilePhoto(id, filename);
            return "redirect:/admin/users";
        } catch (Exception e) {
            return "redirect:/admin/users?error=upload";
        }
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return "redirect:/admin/users";
    }
    
}
