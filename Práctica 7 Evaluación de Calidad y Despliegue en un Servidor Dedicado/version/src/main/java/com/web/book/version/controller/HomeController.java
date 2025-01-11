package com.web.book.version.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.web.book.version.service.OpenLibraryService;
import com.web.book.version.service.TVMazeService;
import com.web.book.version.service.UserService;
import com.web.book.version.service.SearchHistoryService;
import com.web.book.version.service.FavoriteService;
import com.web.book.version.model.Book;
import com.web.book.version.model.Show;
import com.web.book.version.model.User;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.web.book.version.service.RecommendationService;
import com.web.book.version.model.Recommendation;

import java.util.Map;
import java.util.Objects;
import java.util.ArrayList;

@Controller
public class HomeController {
    
    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);
    
    @Autowired
    private OpenLibraryService openLibraryService;
    
    @Autowired
    private TVMazeService tvMazeService;
    
    @Autowired
    private SearchHistoryService searchHistoryService;

    @Autowired
    private FavoriteService favoriteService;

    @Autowired
    private UserService userService;

    @Autowired
    private RecommendationService recommendationService;

    @GetMapping({"/", "/home", "/dashboard"}) // Agregar múltiples rutas
    public String home(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            logger.info("Starting home method with user: {}", userDetails != null ? userDetails.getUsername() : "null");
            
            if (userDetails == null) {
                return "redirect:/auth/login";
            }

            User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

            // Verificar historial y favoritos
            long historyCount = searchHistoryService.getUserSearchCount(user);
            long favoritesCount = favoriteService.getUserFavoritesCount(user);

            // Obtener recomendaciones
            List<Book> recommendedBooks = openLibraryService.getFeaturedBooks();
            List<Show> recommendedShows = tvMazeService.getFeaturedShows();

            // Preparar el modelo de manera segura
            model.addAttribute("userName", user.getUsername());
            model.addAttribute("hasRecommendations", historyCount > 0 || favoritesCount > 0);
            model.addAttribute("featuredBooks", recommendedBooks != null ? recommendedBooks : new ArrayList<>());
            model.addAttribute("featuredShows", recommendedShows != null ? recommendedShows : new ArrayList<>());

            // Log de debug
            logger.info("Data loaded - User: {}, History: {}, Favorites: {}, Books: {}, Shows: {}", 
                user.getUsername(), historyCount, favoritesCount, 
                recommendedBooks != null ? recommendedBooks.size() : 0,
                recommendedShows != null ? recommendedShows.size() : 0);

            return "home";
        } catch (Exception e) {
            logger.error("Error in home page: {}", e.getMessage(), e);
            model.addAttribute("error", "Error loading content: " + e.getMessage());
            return "error";
        }
    }

    private void setDefaultContent(Model model) {
        model.addAttribute("featuredBooks", openLibraryService.getFeaturedBooks());
        model.addAttribute("featuredShows", tvMazeService.getFeaturedShows());
        model.addAttribute("hasRecommendations", false);
    }

    // Métodos auxiliares para convertir recomendaciones
    private List<Book> convertToBooks(Map<Recommendation.ContentType, List<Recommendation>> groupedRecommendations) {
        return groupedRecommendations
            .getOrDefault(Recommendation.ContentType.BOOK, List.of())
            .stream()
            .map(rec -> {
                Book book = new Book();
                book.setTitle(rec.getTitle());
                book.setAuthor(rec.getAuthor());
                book.setIsbn(rec.getContentId());
                book.setCoverUrl(rec.getImageUrl());
                book.setRating(rec.getRelevanceScore().toString());
                return book;
            })
            .collect(Collectors.toList());
    }

    private List<Show> convertToShows(Map<Recommendation.ContentType, List<Recommendation>> groupedRecommendations) {
        return groupedRecommendations
            .getOrDefault(Recommendation.ContentType.SERIES, List.of())
            .stream()
            .map(rec -> {
                Show show = tvMazeService.getShowById(Long.parseLong(rec.getContentId()));
                if (show != null && show.getRating() == null) {
                    show.setRating(new Show.Rating());
                }
                if (show != null) {
                    show.getRating().setAverage(rec.getRelevanceScore());
                }
                return show;
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }



}
