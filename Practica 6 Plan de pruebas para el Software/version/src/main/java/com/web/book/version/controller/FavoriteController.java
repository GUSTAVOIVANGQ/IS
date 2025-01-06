package com.web.book.version.controller;

import com.web.book.version.model.User;
import com.web.book.version.model.Book;
import com.web.book.version.model.Show;
import com.web.book.version.model.Favorite;
import com.web.book.version.service.FavoriteService;
import com.web.book.version.service.UserService;
import com.web.book.version.service.OpenLibraryService;
import com.web.book.version.service.TVMazeService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
@RequestMapping("/favorites")
public class FavoriteController {
    
    private static final Logger logger = LoggerFactory.getLogger(FavoriteController.class);
    
    @Autowired
    private FavoriteService favoriteService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private OpenLibraryService openLibraryService;
    
    @Autowired
    private TVMazeService tvMazeService;

    // Mostrar página de favoritos
    @GetMapping("")
    public String showFavorites(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            logger.info("Attempting to show favorites for user: {}", userDetails != null ? userDetails.getUsername() : "null");
            
            if (userDetails == null) {
                logger.warn("No authenticated user found, redirecting to login");
                return "redirect:/login";
            }
    
            User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            logger.info("Loading favorites for user ID: {}", user.getId());
            
            List<Favorite> bookFavorites = new ArrayList<>();
            List<Favorite> showFavorites = new ArrayList<>();
            List<Favorite> movieFavorites = new ArrayList<>();
            
            try {
                bookFavorites = favoriteService.getBookFavorites(user);
                logger.info("Loaded {} book favorites", bookFavorites.size());
            } catch (Exception e) {
                logger.error("Error loading book favorites: {}", e.getMessage(), e);
            }
            
            try {
                showFavorites = favoriteService.getShowFavorites(user);
                logger.info("Loaded {} show favorites", showFavorites.size());
            } catch (Exception e) {
                logger.error("Error loading show favorites: {}", e.getMessage(), e);
            }
            
            try {
                movieFavorites = favoriteService.getMovieFavorites(user);
                logger.info("Loaded {} movie favorites", movieFavorites.size());
            } catch (Exception e) {
                logger.error("Error loading movie favorites: {}", e.getMessage(), e);
            }
    
            model.addAttribute("bookFavorites", bookFavorites);
            model.addAttribute("showFavorites", showFavorites);
            model.addAttribute("movieFavorites", movieFavorites);
            
            logger.info("Successfully prepared favorites model");
            return "favorites/favorites";
        } catch (Exception e) {
            logger.error("Error in showFavorites: {}", e.getMessage(), e);
            model.addAttribute("error", "Unable to load favorites. Please try again later.");
            return "error";
        }
    }

    // Agregar libro a favoritos
    @PostMapping("/books/{isbn}")
    public ResponseEntity<?> addBookToFavorites(
            @PathVariable String isbn,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            Book book = openLibraryService.getBookDetails(isbn);
            if (book == null) {
                return ResponseEntity.notFound().build();
            }
            
            Favorite favorite = favoriteService.addBookToFavorites(user, book);
            return ResponseEntity.ok().body(favorite);
        } catch (Exception e) {
            logger.error("Error adding book to favorites: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Agregar show a favoritos
    @PostMapping("/shows/{id}")
    public ResponseEntity<?> addShowToFavorites(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            Show show = tvMazeService.getShowById(id);
            if (show == null) {
                return ResponseEntity.notFound().build();
            }
            
            Favorite favorite = favoriteService.addShowToFavorites(user, show);
            return ResponseEntity.ok().body(favorite);
        } catch (Exception e) {
            logger.error("Error adding show to favorites: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Eliminar de favoritos
    @DeleteMapping("/{contentId}")
    public ResponseEntity<?> removeFavorite(
            @PathVariable String contentId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            favoriteService.removeFavorite(user, contentId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Error removing favorite: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Verificar si está en favoritos
    @GetMapping("/check/{contentId}")
    public ResponseEntity<Boolean> checkFavorite(
            @PathVariable String contentId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            boolean isFavorite = favoriteService.isFavorite(user, contentId);
            return ResponseEntity.ok(isFavorite);
        } catch (Exception e) {
            logger.error("Error checking favorite status: {}", e.getMessage());
            return ResponseEntity.badRequest().body(false);
        }
    }
}
