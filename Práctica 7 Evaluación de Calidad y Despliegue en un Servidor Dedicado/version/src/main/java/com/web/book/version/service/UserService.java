package com.web.book.version.service;

import com.web.book.version.model.User;
import com.web.book.version.repository.UserRepository;
import com.web.book.version.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;

import java.util.Optional;
import java.util.List;

@Service
@Transactional
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    public UserService(
        UserRepository userRepository,
        @Lazy PasswordEncoder passwordEncoder) {  // Add @Lazy here
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    @Cacheable(value = "userCache", key = "#username")
    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + username));

        return UserDetailsImpl.build(user);
    }

    @CachePut(value = "userCache", key = "#user.username")
    public User save(User user) {
        try {
            logger.info("Attempting to save user: {}", user.getUsername());
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            User savedUser = userRepository.save(user);
            logger.info("Successfully saved user with ID: {}", savedUser.getId());
            return savedUser;
        } catch (Exception e) {
            logger.error("Error saving user: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Cacheable(value = "userExistsCache", key = "#username")
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Cacheable(value = "userExistsCache", key = "#email")
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Cacheable(value = "userCache", key = "#username", unless = "#result.isEmpty()")
    public Optional<User> findByUsername(String username) {
        try {
            return userRepository.findByUsername(username);
        } catch (Exception e) {
            logger.error("Error finding user by username: {}", username, e);
            return Optional.empty();
        }
    }

    @Cacheable(value = "userCache", key = "#id", unless = "#result.isEmpty()")
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }
    
    @CachePut(value = "userCache", key = "#username")
    public void updateProfilePhoto(String username, String filename) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
        user.setProfilePhotoUrl(filename);
        userRepository.save(user);
    }

    @Cacheable(value = "allUsersCache")
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    @CachePut(value = "userCache", key = "#id")
    public void updateUser(Long id, User updatedUser) {
        User user = findById(id)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setNombre(updatedUser.getNombre());
        user.setApellido(updatedUser.getApellido());
        user.setEmail(updatedUser.getEmail());
        user.setActivo(updatedUser.isActivo());
        user.setRol(updatedUser.getRol());
        
        userRepository.save(user);
    }

    public void updateUserProfilePhoto(Long id, String filename) {
        User user = findById(id)
            .orElseThrow(() -> new RuntimeException("User not found"));
        user.setProfilePhotoUrl(filename);
        userRepository.save(user);
    }

    @CacheEvict(value = "userCache", key = "#id")
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    @Cacheable(value = "userCache", key = "'email:' + #email", unless = "#result.isEmpty()")
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @CachePut(value = "userCache", key = "#id")
    public User updateUserAPI(Long id, User userDetails) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + id));

        // Actualizar solo los campos permitidos
        if (userDetails.getNombre() != null) {
            user.setNombre(userDetails.getNombre());
        }
        if (userDetails.getApellido() != null) {
            user.setApellido(userDetails.getApellido());
        }
        if (userDetails.getEmail() != null) {
            user.setEmail(userDetails.getEmail());
        }
        if (userDetails.getProfilePhotoUrl() != null) {
            user.setProfilePhotoUrl(userDetails.getProfilePhotoUrl());
        }

        return userRepository.save(user);
    }

    @Cacheable(value = "userCache", key = "#username")
    public User getCurrentUser(String username) {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));
    }

    @CachePut(value = "userCache", key = "#id")
    @Transactional
    public void toggleUserStatus(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + id));
        user.setActivo(!user.isActivo());
        userRepository.save(user);
    }
}