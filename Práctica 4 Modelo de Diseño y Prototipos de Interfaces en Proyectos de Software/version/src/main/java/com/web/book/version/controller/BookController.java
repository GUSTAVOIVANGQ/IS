package com.web.book.version.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

import com.web.book.version.model.Book;
import com.web.book.version.model.User;
import com.web.book.version.service.OpenLibraryService;
import com.web.book.version.service.UserService;

import java.util.List;

@Controller
@RequestMapping("/books")
public class BookController {
    private static final Logger logger = LoggerFactory.getLogger(RecommendationController.class);
    
    @Autowired
    private OpenLibraryService openLibraryService;

    @Autowired
    private UserService userService;

    @GetMapping("")
    public String showBooks(Model model) {
        List<Book> featuredBooks = openLibraryService.getFeaturedBooks();
        model.addAttribute("featuredBooks", featuredBooks);
        return "books/book";
    }
    
    @GetMapping("/search")
    public String searchBooks(
            @RequestParam(required = false) String query, 
            Model model,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        if (query != null && !query.isEmpty()) {
            List<Book> searchResults;
            
            // Si el usuario está autenticado, registrar la búsqueda
            if (userDetails != null) {
                User user = userService.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
                searchResults = openLibraryService.searchBooks(query, user);
            } else {
                searchResults = openLibraryService.searchBooks(query);
            }
            
            model.addAttribute("books", searchResults);
            model.addAttribute("searchQuery", query);
        }
        return "books/search";
    }
        
    @GetMapping("/{id}")
    public String bookDetails() { return "books/details"; }
    
    @GetMapping("/details/{isbn}")
    public String showBookDetails(@PathVariable String isbn, Model model) {
        try {
            logger.info("Fetching details for book ISBN: {}", isbn);
            Book book = openLibraryService.getBookDetails(isbn);
            
            if (book == null) {
                logger.warn("Book not found for ISBN: {}", isbn);
                model.addAttribute("error", "Lo sentimos, no pudimos encontrar los detalles del libro solicitado.");
                return "error";
            }
            
            model.addAttribute("book", book);
            return "books/details";
        } catch (Exception e) {
            logger.error("Error showing book details for ISBN {}: {}", isbn, e.getMessage());
            model.addAttribute("error", "Ocurrió un error al cargar los detalles del libro. Por favor, intente nuevamente.");
            return "error";
        }
    }
}
