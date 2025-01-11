package com.web.book.version.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileUploadService {
    private final String uploadDir = "uploads/profiles/";
    
    public String uploadProfilePhoto(MultipartFile file) throws IOException {
        // Create directory if it doesn't exist
        Files.createDirectories(Paths.get(uploadDir));
        
        // Generate unique filename
        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path filePath = Paths.get(uploadDir + filename);
        
        // Save file
        Files.copy(file.getInputStream(), filePath);
        
        // Return the URL path
        return "/uploads/profiles/" + filename;
    }
}