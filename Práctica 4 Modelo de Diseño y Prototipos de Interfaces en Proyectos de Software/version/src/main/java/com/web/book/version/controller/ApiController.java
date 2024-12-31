package com.web.book.version.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.web.book.version.service.OpenLibraryService;
import java.util.List;
import com.web.book.version.model.Book;
import com.web.book.version.model.User;
import com.web.book.version.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

@Controller
public class ApiController {
    
    @Autowired
    private OpenLibraryService openLibraryService;
    
    @Autowired
    private UserService userService;

    @GetMapping("/api/home")  // Changed from "/home" to "/api/home"
    public String home() {
        return "home";
    }

    @GetMapping("/search/books")
    public String searchBooks(@RequestParam String query, 
                            Model model,
                            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            List<Book> books;
            if (userDetails != null) {
                User user = userService.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
                books = openLibraryService.searchBooks(query, user);
            } else {
                books = openLibraryService.searchBooks(query);
            }
            
            model.addAttribute("books", books);
            model.addAttribute("searchQuery", query);
            return "books";
        } catch (Exception e) {
            model.addAttribute("error", "Error searching books: " + e.getMessage());
            return "error";
        }
    }
}