package com.version.nutrition.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class StorageConfig {

    @Bean
    CommandLineRunner init() {
        return args -> {
            Path uploadDir = Paths.get("uploads/profile-photos");
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }
        };
    }
}
