package com.web.book.version.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.web.book.version.model.User;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Find a user by username
    Optional<User> findByUsername(String username);
    
    // Check if a username exists
    Boolean existsByUsername(String username);
    
    // Find a user by email
    Optional<User> findByEmail(String email);
    
    // Check if an email exists
    Boolean existsByEmail(String email);
}