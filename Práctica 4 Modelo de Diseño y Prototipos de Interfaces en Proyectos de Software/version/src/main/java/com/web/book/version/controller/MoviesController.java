package com.web.book.version.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

import com.web.book.version.model.Show;
import com.web.book.version.service.TVMazeService;
import com.web.book.version.service.UserService;
import com.web.book.version.model.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
@RequestMapping("/movies")
public class MoviesController {
    
    @Autowired
    private TVMazeService tvMazeService;
    
    @Autowired
    private UserService userService;
    
    private static final Logger logger = LoggerFactory.getLogger(MoviesController.class);

    @GetMapping("")
    public String showMovies(Model model) {
        try {
            List<Show> featuredShows = tvMazeService.getFeaturedShows();
            if (featuredShows.isEmpty()) {
                model.addAttribute("error", "No shows available at the moment");
            } else {
                model.addAttribute("featuredShows", featuredShows);
            }
            return "movies/movies";
        } catch (Exception e) {
            model.addAttribute("error", "Error loading shows: " + e.getMessage());
            return "movies/movies";
        }
    }
    
    @GetMapping("/search")
    public String searchMovies(
            @RequestParam(required = false) String query, 
            Model model,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            if (query != null && !query.isEmpty()) {
                List<Show> searchResults;
                
                // Si el usuario está autenticado, registrar la búsqueda
                if (userDetails != null) {
                    User user = userService.findByUsername(userDetails.getUsername())
                        .orElseThrow(() -> new RuntimeException("User not found"));
                    searchResults = tvMazeService.searchShows(query, user);
                } else {
                    searchResults = tvMazeService.searchShows(query);
                }
                
                model.addAttribute("shows", searchResults);
                model.addAttribute("searchQuery", query);
            }
            return "movies/search";
        } catch (Exception e) {
            logger.error("Error searching shows: {}", e.getMessage(), e);
            model.addAttribute("error", "Error searching shows: " + e.getMessage());
            return "movies/search";
        }
    }

    @GetMapping("/details/{id}")
    public String showMovieDetails(@PathVariable Long id, Model model) {
        try {
            logger.info("Fetching details for show ID: {}", id);
            Show show = tvMazeService.getShowById(id);
            
            if (show == null) {
                logger.warn("Show not found with ID: {}", id);
                model.addAttribute("error", "Show not found");
                return "movies/movies"; // Cambiado para redirigir a la página principal si no hay show
            }

            logger.info("Show found: {}", show.getName());
            model.addAttribute("show", show);
            return "movies/details"; 
            
        } catch (Exception e) {
            logger.error("Error fetching show details: {}", e.getMessage(), e);
            model.addAttribute("error", "Error loading show details: " + e.getMessage());
            return "movies/movies"; // Cambiado para redirigir a la página principal si hay error
        }
    }
}