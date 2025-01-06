package com.web.book.version.controller;

import com.web.book.version.model.Recommendation;
import com.web.book.version.model.RecommendationCriteria;
import com.web.book.version.model.User;
import com.web.book.version.model.Book;
import com.web.book.version.model.Show;
import com.web.book.version.service.RecommendationService;
import com.web.book.version.service.UserService;
import com.web.book.version.service.OpenLibraryService;
import com.web.book.version.service.TVMazeService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.http.ResponseEntity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.HashMap;

@Controller
@RequestMapping("/recommendations")
public class RecommendationController {
    
    private static final Logger logger = LoggerFactory.getLogger(RecommendationController.class);

    @Autowired
    private RecommendationService recommendationService;

    @Autowired
    private UserService userService;

    @Autowired
    private OpenLibraryService openLibraryService;

    @Autowired
    private TVMazeService tvMazeService;

    // Vista principal de recomendaciones
    @GetMapping("/recommendations")
    public String showRecommendations(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        logger.info("Entering showRecommendations method");
        try {
            if (userDetails == null) {
                return "redirect:/auth/login";
            }

            User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

            // Obtener recomendaciones
            List<Recommendation> recommendations = recommendationService.getPersonalizedRecommendations(user);
            
            if (recommendations.isEmpty()) {
                logger.warn("No recommendations found for user {}", user.getUsername());
                // Intentar generar recomendaciones iniciales
                recommendations = recommendationService.generateInitialRecommendations(user);
            }

            // Separar por tipo
            List<Recommendation> bookRecommendations = recommendations.stream()
                .filter(r -> r.getContentType() == Recommendation.ContentType.BOOK)
                .collect(Collectors.toList());
            
            List<Recommendation> showRecommendations = recommendations.stream()
                .filter(r -> r.getContentType() == Recommendation.ContentType.SERIES || 
                            r.getContentType() == Recommendation.ContentType.MOVIE)
                .collect(Collectors.toList());

            logger.info("Found {} book recommendations and {} show recommendations", 
                bookRecommendations.size(), showRecommendations.size());

            model.addAttribute("bookRecommendations", bookRecommendations);
            model.addAttribute("showRecommendations", showRecommendations);
            model.addAttribute("userName", user.getUsername());

            return "recommendations/recommendations";
            
        } catch (Exception e) {
            logger.error("Error in recommendations page: ", e);
            model.addAttribute("error", "Error loading recommendations: " + e.getMessage());
            return "error";
        }
    }

    // Endpoint para recomendaciones de libros
    @GetMapping("/books")
    public String getBookRecommendations(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

            List<Recommendation> recommendations = recommendationService.getPersonalizedRecommendations(user)
                .stream()
                .filter(r -> r.getContentType() == Recommendation.ContentType.BOOK)
                .collect(Collectors.toList());

            model.addAttribute("recommendations", recommendations);
            return "recommendations/book-recommendations";
        } catch (Exception e) {
            logger.error("Error getting book recommendations: {}", e.getMessage());
            return "error";
        }
    }

    // Endpoint para recomendaciones de series y películas
    @GetMapping("/shows")
    public String getShowRecommendations(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

            List<Recommendation> recommendations = recommendationService.getPersonalizedRecommendations(user)
                .stream()
                .filter(r -> r.getContentType() == Recommendation.ContentType.SERIES || 
                            r.getContentType() == Recommendation.ContentType.MOVIE)
                .collect(Collectors.toList());

            model.addAttribute("recommendations", recommendations);
            return "recommendations/show-recommendations";
        } catch (Exception e) {
            logger.error("Error getting show recommendations: {}", e.getMessage());
            return "error";
        }
    }

    // API Endpoint para actualizar recomendaciones
    @PostMapping("/refresh")
    @ResponseBody
    public ResponseEntity<?> refreshRecommendations(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

            List<Recommendation> newRecommendations = 
                recommendationService.getPersonalizedRecommendations(user);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "recommendations", newRecommendations
            ));
        } catch (Exception e) {
            logger.error("Error refreshing recommendations: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "Error refreshing recommendations"
            ));
        }
    }

    // Endpoint para calificar recomendaciones
    @PostMapping("/{id}/rate")
    @ResponseBody
    public ResponseEntity<?> rateRecommendation(
            @PathVariable Long id,
            @RequestParam Double score,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            // Implementar lógica de calificación
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            logger.error("Error rating recommendation: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("success", false));
        }
    }

    @GetMapping("/debug")
    @ResponseBody
    public Map<String, Object> getDebugInfo(@AuthenticationPrincipal UserDetails userDetails) {
        Map<String, Object> debug = new HashMap<>();
        try {
            User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

            List<Recommendation> recommendations = recommendationService.getPersonalizedRecommendations(user);
            
            debug.put("recommendationsCount", recommendations.size());
            debug.put("recommendationTypes", recommendations.stream()
                .collect(Collectors.groupingBy(Recommendation::getContentType, Collectors.counting())));
            debug.put("success", true);
            
            return debug;
        } catch (Exception e) {
            debug.put("success", false);
            debug.put("error", e.getMessage());
            return debug;
        }
    }
}
