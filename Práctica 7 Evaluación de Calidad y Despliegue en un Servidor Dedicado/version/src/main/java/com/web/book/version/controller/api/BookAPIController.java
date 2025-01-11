package com.web.book.version.controller.api;

import com.web.book.version.model.Book;
import com.web.book.version.model.User;
import com.web.book.version.service.OpenLibraryService;
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
import java.util.Optional;

@RestController
@RequestMapping("/api/books")
@CrossOrigin(origins = "*", maxAge = 3600)
public class BookAPIController {
    
    private static final Logger logger = LoggerFactory.getLogger(BookAPIController.class);
    
    @Autowired
    private OpenLibraryService openLibraryService;
    
    @Autowired
    private UserService userService;

    @GetMapping("/search")
    public ResponseEntity<?> searchBooks(
            @RequestParam String query,
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
            
            return ResponseEntity.ok(books);
        } catch (Exception e) {
            logger.error("Error searching books: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Error searching books: " + e.getMessage()));
        }
    }

    @GetMapping("/{isbn}")
    public ResponseEntity<?> getBookByIsbn(@PathVariable String isbn) {
        try {
            Book book = openLibraryService.getBookDetails(isbn);
            if (book == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(book);
        } catch (Exception e) {
            logger.error("Error fetching book details for ISBN {}: {}", isbn, e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Error fetching book details: " + e.getMessage()));
        }
    }

    @GetMapping("/featured")
    public ResponseEntity<?> getFeaturedBooks() {
        try {
            List<Book> featuredBooks = openLibraryService.getFeaturedBooks();
            return ResponseEntity.ok(featuredBooks);
        } catch (Exception e) {
            logger.error("Error fetching featured books: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Error fetching featured books: " + e.getMessage()));
        }
    }

    @GetMapping("/similar")
    public ResponseEntity<?> getSimilarBooks(
            @RequestParam String genre,
            @RequestParam(required = false) String author) {
        try {
            List<Book> similarBooks = openLibraryService.getSimilarBooks(genre, author);
            return ResponseEntity.ok(similarBooks);
        } catch (Exception e) {
            logger.error("Error fetching similar books: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Error fetching similar books: " + e.getMessage()));
        }
    }
}


