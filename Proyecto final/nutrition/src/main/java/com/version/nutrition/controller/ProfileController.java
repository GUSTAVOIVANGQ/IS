package com.version.nutrition.controller;

import com.version.nutrition.model.Usuario;
import com.version.nutrition.service.FileStorageService;
import com.version.nutrition.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ProfileController {

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private UserService userService;

    @PostMapping("/profile/upload-photo")
    public String uploadProfilePhoto(
            @RequestParam("photo") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        try {
            String fileName = fileStorageService.storeFile(file, "profile-photos");
            userService.updateProfilePhoto(userDetails.getUsername(), fileName);
            redirectAttributes.addFlashAttribute("message", "Foto de perfil actualizada exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al subir la foto: " + e.getMessage());
        }
        return "redirect:/profile";
    }
}
