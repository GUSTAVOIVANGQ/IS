package com.web.book.version.service;

import com.web.book.version.model.User;
import com.web.book.version.repository.UserRepository;
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

import java.util.Optional;
import java.util.List;

@Service
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

/*    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
*/
    @Autowired
    public UserService(
        UserRepository userRepository,
        @Lazy PasswordEncoder passwordEncoder) {  // Add @Lazy here
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + username));

        return UserDetailsImpl.build(user);
    }

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

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public Optional<User> findByUsername(String username) {
        try {
            return userRepository.findByUsername(username);
        } catch (Exception e) {
            logger.error("Error finding user by username: {}", username, e);
            return Optional.empty();
        }
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }
    
    public void updateProfilePhoto(String username, String filename) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
        user.setProfilePhotoUrl(filename);
        userRepository.save(user);
    }

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

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

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}