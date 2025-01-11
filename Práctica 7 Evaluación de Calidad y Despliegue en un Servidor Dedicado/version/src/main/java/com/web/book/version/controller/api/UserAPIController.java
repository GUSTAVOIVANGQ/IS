package com.web.book.version.controller.api;

import com.web.book.version.dto.UserProfileDTO;
import com.web.book.version.model.User;
import com.web.book.version.service.UserService;
import com.web.book.version.service.FileUploadService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpStatus;

import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserAPIController {

    @Autowired
    private UserService userService;

    @Autowired
    private FileUploadService fileUploadService;

    @GetMapping("/profile")
    public ResponseEntity<UserProfileDTO> getCurrentUserProfile() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByUsername(auth.getName())
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        UserProfileDTO profileDTO = convertToDTO(user);
        return ResponseEntity.ok(profileDTO);
    }

    @PutMapping("/profile")
    public ResponseEntity<UserProfileDTO> updateProfile(@Valid @RequestBody UserProfileDTO profileDTO) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByUsername(auth.getName())
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        user.setNombre(profileDTO.getNombre());
        user.setApellido(profileDTO.getApellido());
        user.setEmail(profileDTO.getEmail());
        
        User updatedUser = userService.updateUserAPI(user.getId(), user);
        return ResponseEntity.ok(convertToDTO(updatedUser));
    }

    @PostMapping("/profile/photo")
    public ResponseEntity<?> updateProfilePhoto(@RequestParam("photo") MultipartFile file) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User user = userService.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            String filename = fileUploadService.uploadProfilePhoto(file);
            user.setProfilePhotoUrl(filename);
            userService.updateUser(user.getId(), user);

            return ResponseEntity.ok().body(new PhotoUploadResponse(filename));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al subir la foto: " + e.getMessage());
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserProfileDTO>> getAllUsers() {
        List<UserProfileDTO> users = userService.findAllUsers().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok().build();
    }

    private UserProfileDTO convertToDTO(User user) {
        return new UserProfileDTO(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getNombre(),
            user.getApellido(),
            user.getProfilePhotoUrl(),
            user.getRol().toString(),
            user.getFechaRegistro()
        );
    }

    private static class PhotoUploadResponse {
        private String url;

        public PhotoUploadResponse(String url) {
            this.url = url;
        }

        public String getUrl() {
            return url;
        }
    }
}
