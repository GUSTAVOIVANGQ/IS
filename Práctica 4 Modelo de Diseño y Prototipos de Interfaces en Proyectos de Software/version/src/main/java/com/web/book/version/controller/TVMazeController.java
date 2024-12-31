package com.web.book.version.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import com.web.book.version.service.TVMazeService;
import com.web.book.version.service.UserService;
import com.web.book.version.model.Show;
import com.web.book.version.model.User;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/tvmaze")  // Changed from /movies to /tvmaze
public class TVMazeController {
    
    @Autowired
    private TVMazeService tvMazeService;

    @Autowired
    private UserService userService;

    @GetMapping("")
    public String showMovies(Model model) {
        try {
            List<Show> shows = tvMazeService.getFeaturedShows();
            model.addAttribute("featuredShows", shows);
            return "movies";
        } catch (Exception e) {
            model.addAttribute("error", "Error loading shows: " + e.getMessage());
            return "movies";
        }
    }

    // Página principal de TVMaze
    @GetMapping("/home")
    public String showTVMazeHome() {
        return "tvmaze-search";  // Cambiado a tvmaze-search.html
    }

    // Búsqueda de shows
    @GetMapping("/search")
    public String searchShows(
            @RequestParam String query, 
            Model model,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            List<Show> shows;
            
            // Si el usuario está autenticado, registrar la búsqueda
            if (userDetails != null) {
                User user = userService.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
                shows = tvMazeService.searchShows(query, user);
            } else {
                shows = tvMazeService.searchShows(query);
            }
            
            model.addAttribute("shows", shows);
            model.addAttribute("searchQuery", query);
            return "movies/search";
        } catch (Exception e) {
            model.addAttribute("error", "Error searching shows: " + e.getMessage());
            return "movies/search";
        }
    }

    // Detalles del show
    @GetMapping("/show/{id}")
    public String showDetails(@PathVariable Long id, Model model) {
        Show show = tvMazeService.getShowById(id);
        if (show != null) {
            model.addAttribute("show", show);
            return "details";  // Cambiado a tvmaze-details.html
        }
        return "error";
    }
}