package com.web.book.version.controller;

import com.web.book.version.dto.SearchHistoryDTO;
import com.web.book.version.model.SearchHistory;
import com.web.book.version.model.User;
import com.web.book.version.service.SearchHistoryService;
import com.web.book.version.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
@RequestMapping("/history")
public class SearchHistoryController {
    private static final Logger logger = LoggerFactory.getLogger(SearchHistoryController.class);
    @Autowired
    private SearchHistoryService searchHistoryService;
    
    @Autowired
    private UserService userService;
    
    @GetMapping
    public String viewHistory(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String date,
            Model model, 
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            if (userDetails == null) {
                return "redirect:/auth/login";
            }
    
            User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
                
            boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    
            List<SearchHistory> histories;
            if (isAdmin) {
                histories = searchHistoryService.getAllHistoryWithFilters(search, type, date);
            } else {
                histories = searchHistoryService.getUserHistoryWithFilters(user, search, type, date);
            }
    
            List<SearchHistoryDTO> historiesDTO = histories.stream()
                .map(searchHistoryService::convertToDTO)
                .collect(Collectors.toList());
            
            model.addAttribute("histories", historiesDTO);
            return isAdmin ? "search/admin-history" : "search/history";
        } catch (Exception e) {
            model.addAttribute("error", "Error loading search history: " + e.getMessage());
            return "error";
        }
    }
    
    @GetMapping("/books")
    public String viewBookHistory(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            if (userDetails == null) {
                return "redirect:/auth/login";
            }

            User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

            List<SearchHistory> histories = searchHistoryService.getUserBookSearches(user);
            List<SearchHistoryDTO> historiesDTO = histories.stream()
                .map(searchHistoryService::convertToDTO)
                .collect(Collectors.toList());

            model.addAttribute("histories", historiesDTO);
            return "search/book-history";
        } catch (Exception e) {
            logger.error("Error loading book history: {}", e.getMessage(), e);
            model.addAttribute("error", "Error loading book history: " + e.getMessage());
            return "error";
        }
    }
    
    @GetMapping("/shows")
    public String viewShowHistory(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            if (userDetails == null) {
                return "redirect:/auth/login";
            }
    
            User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
                
            // Obtener solo el historial de shows/películas
            List<SearchHistory> histories = searchHistoryService.getUserShowSearches(user);
            List<SearchHistoryDTO> historiesDTO = histories.stream()
                .map(searchHistoryService::convertToDTO)
                .collect(Collectors.toList());
                
            model.addAttribute("histories", historiesDTO);
            return "search/show-history";
        } catch (Exception e) {
            logger.error("Error loading show history: {}", e.getMessage(), e);
            model.addAttribute("error", "Error loading show history: " + e.getMessage());
            return "error";
        }
    }
    
    @PostMapping("/clear")
    @Transactional
    public String clearHistory(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            if (userDetails == null) {
                return "redirect:/auth/login";
            }

            User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            searchHistoryService.deleteUserHistory(user);
            return "redirect:/history?deleted=true";
        } catch (Exception e) {
            logger.error("Error clearing history: {}", e.getMessage());
            return "redirect:/history?error=true";
        }
    }

    @DeleteMapping("/books/clear")
    @Transactional
    public String clearBookHistory(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            if (userDetails == null) {
                return "redirect:/auth/login";
            }
    
            User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Llamar al servicio para eliminar solo el historial de libros
            searchHistoryService.deleteUserBookHistory(user);
            return "redirect:/history/books?deleted=true";
        } catch (Exception e) {
            logger.error("Error clearing book history: {}", e.getMessage());
            return "redirect:/history/books?error=true";
        }
    }

    @DeleteMapping("/shows/clear")  // Cambiado de "/history/shows/clear" a "/shows/clear"
    public String clearShowHistory(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            if (userDetails == null) {
                return "redirect:/auth/login";
            }
    
            User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
                
            searchHistoryService.clearUserShowHistory(user);
            return "redirect:/history/shows?cleared=true";
        } catch (Exception e) {
            logger.error("Error clearing show history: {}", e.getMessage(), e);
            return "redirect:/history/shows?error=true";
        }
    }  
}
