package com.web.book.version.controller.api;

import com.web.book.version.model.Show;
import com.web.book.version.model.User;
import com.web.book.version.service.TVMazeService;
import com.web.book.version.service.UserService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/shows")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ShowAPIController {
    
    private static final Logger logger = LoggerFactory.getLogger(ShowAPIController.class);
    
    @Autowired
    private TVMazeService tvMazeService;
    
    @Autowired
    private UserService userService;

    @GetMapping("/search")
    public ResponseEntity<?> searchShows(
            @RequestParam String query,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            List<Show> shows;
            if (userDetails != null) {
                User user = userService.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
                shows = tvMazeService.searchShows(query, user);
            } else {
                shows = tvMazeService.searchShows(query);
            }
            return ResponseEntity.ok(shows);
        } catch (Exception e) {
            logger.error("Error searching shows: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Error searching shows: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getShowById(@PathVariable Long id) {
        try {
            Show show = tvMazeService.getShowById(id);
            if (show == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(show);
        } catch (Exception e) {
            logger.error("Error fetching show details for ID {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Error fetching show details: " + e.getMessage()));
        }
    }

    @GetMapping("/featured")
    public ResponseEntity<?> getFeaturedShows() {
        try {
            List<Show> featuredShows = tvMazeService.getFeaturedShows();
            return ResponseEntity.ok(featuredShows);
        } catch (Exception e) {
            logger.error("Error fetching featured shows: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Error fetching featured shows: " + e.getMessage()));
        }
    }
}
