package com.autenticacion.roles;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication(scanBasePackages = "com.autenticacion.roles")
public class RolesApplication {
    public static void main(String[] args) {
        SpringApplication.run(RolesApplication.class, args);
    }
}