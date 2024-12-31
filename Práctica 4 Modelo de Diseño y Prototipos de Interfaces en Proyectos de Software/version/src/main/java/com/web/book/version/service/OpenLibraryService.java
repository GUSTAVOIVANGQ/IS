package com.web.book.version.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.net.URLEncoder;
import java.util.stream.Collectors;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;

import org.springframework.web.reactive.function.client.WebClient;
import com.web.book.version.model.Book;
import com.web.book.version.model.RecommendationCriteria;
import com.web.book.version.model.User;
import reactor.core.publisher.Flux;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class OpenLibraryService {
    private static final Logger logger = LoggerFactory.getLogger(OpenLibraryService.class);
    private final String BASE_URL = "https://openlibrary.org/search.json";
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    private SearchHistoryService searchHistoryService;

    public OpenLibraryService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public List<Book> getFeaturedBooks() {
        try {
            List<Book> books = searchBooks("bestseller");
            return books.stream()
                       .limit(4)
                       .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error fetching featured books: ", e);
            return new ArrayList<>();
        }
    }
        
    public List<Book> searchBooks(String query) {
        try {
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8.toString());
            String url = "https://openlibrary.org/search.json?q=" + encodedQuery;
            
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            JsonNode root = new ObjectMapper().readTree(response.getBody());
            JsonNode docs = root.get("docs");
            
            List<Book> books = new ArrayList<>();
            if (docs.isArray()) {
                for (JsonNode doc : docs) {
                    Book book = new Book();
                    book.setTitle(doc.path("title").asText("Unknown Title"));
                    book.setAuthor(doc.has("author_name") ? 
                        doc.get("author_name").get(0).asText() : "Unknown Author");
                    book.setIsbn(doc.has("isbn") ? 
                        doc.get("isbn").get(0).asText() : "");
                    
                    if (doc.has("cover_i")) {
                        String coverId = doc.get("cover_i").asText();
                        book.setCoverUrl("https://covers.openlibrary.org/b/id/" + coverId + "-M.jpg");
                    }
                    
                    books.add(book);
                }
            }
            return books;
        } catch (Exception e) {
            logger.error("Error searching books: ", e);
            return new ArrayList<>();
        }
    }

    public List<Book> searchBooks(String query, User user) {
        try {
            List<Book> books = searchBooks(query);
            
            // Registrar la búsqueda solo si se encontraron resultados
            if (!books.isEmpty()) {
                Book firstBook = books.get(0);
                searchHistoryService.saveBookSearch(
                    user,
                    query,
                    firstBook.getTitle(),
                    firstBook.getAuthor()
                );
            }
            
            return books;
        } catch (Exception e) {
            logger.error("Error searching books: ", e);
            return new ArrayList<>();
        }
    }

    public Book getBookDetails(String isbn) {
        try {
            String url = "https://openlibrary.org/api/books?bibkeys=ISBN:" + isbn + "&format=json&jscmd=data";
            logger.info("Fetching book details from URL: {}", url);
            
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                logger.error("Error response from OpenLibrary API: {}", response.getStatusCode());
                return null;
            }
    
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode bookData = root.get("ISBN:" + isbn);
            
            if (bookData == null || bookData.isEmpty()) {
                logger.warn("No data found for ISBN: {}", isbn);
                return null;
            }
    
            Book book = new Book();
            book.setIsbn(isbn);
            book.setTitle(bookData.path("title").asText("Sin título"));
            
            // Manejar autores con más cuidado
            JsonNode authors = bookData.path("authors");
            if (authors.isArray() && authors.size() > 0) {
                book.setAuthor(authors.get(0).path("name").asText("Autor desconocido"));
            } else {
                book.setAuthor("Autor desconocido");
            }
    
            // Manejar fecha de publicación
            JsonNode publishDate = bookData.path("publish_date");
            if (!publishDate.isMissingNode()) {
                book.setPublishYear(publishDate.asText());
            } else {
                book.setPublishYear("Año desconocido");
            }
    
            // Manejar portada del libro
            JsonNode covers = bookData.path("cover");
            if (!covers.isMissingNode()) {
                String coverUrl = covers.path("large").asText();
                if (!coverUrl.isEmpty()) {
                    book.setCoverUrl(coverUrl);
                } else {
                    // URL alternativa para la portada
                    book.setCoverUrl("https://covers.openlibrary.org/b/isbn/" + isbn + "-L.jpg");
                }
            }
    
            // Manejar descripción
            JsonNode description = bookData.path("description");
            if (!description.isMissingNode()) {
                if (description.isObject()) {
                    book.setDescription(description.path("value").asText("Sin descripción disponible"));
                } else {
                    book.setDescription(description.asText("Sin descripción disponible"));
                }
            } else {
                book.setDescription("Sin descripción disponible");
            }
    
            logger.info("Successfully retrieved book details for ISBN: {}", isbn);
            return book;
    
        } catch (Exception e) {
            logger.error("Error getting book details for ISBN {}: {}", isbn, e.getMessage());
            return null;
        }
    }

    // Nuevo método para obtener libros similares
    public List<Book> getSimilarBooks(String genre, String author) {
        try {
            String query = String.format("subject:%s OR author:%s", 
                URLEncoder.encode(genre, StandardCharsets.UTF_8.toString()),
                URLEncoder.encode(author, StandardCharsets.UTF_8.toString()));
            
            String url = BASE_URL + "?q=" + query + "&limit=5";
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                return parseBookResults(root.get("docs"));
            }
            
            return new ArrayList<>();
        } catch (Exception e) {
            logger.error("Error getting similar books: ", e);
            return new ArrayList<>();
        }
    }

    // Nuevo método para obtener recomendaciones personalizadas
    public List<Book> getPersonalizedRecommendations(User user, RecommendationCriteria criteria) {
        try {
            List<Book> recommendations = new ArrayList<>();
            Set<String> addedBooks = new HashSet<>();

            // Obtener libros basados en géneros preferidos
            for (Map.Entry<String, Double> entry : criteria.getGenrePreferences().entrySet()) {
                if (entry.getValue() >= criteria.getMinimumRelevanceScore()) {
                    List<Book> genreBooks = searchBooks("subject:" + entry.getKey());
                    for (Book book : genreBooks) {
                        if (addedBooks.add(book.getIsbn())) {
                            book.setRating(entry.getValue().toString());
                            recommendations.add(book);
                        }
                    }
                }
            }

            return recommendations.stream()
                .sorted((b1, b2) -> Double.compare(
                    Double.parseDouble(b2.getRating()), 
                    Double.parseDouble(b1.getRating())))
                .limit(criteria.getMaxRecommendations())
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            logger.error("Error getting personalized recommendations: ", e);
            return new ArrayList<>();
        }
    }

    private List<Book> parseBookResults(JsonNode docs) {
        List<Book> books = new ArrayList<>();
        if (docs != null && docs.isArray()) {
            for (JsonNode doc : docs) {
                Book book = new Book();
                book.setTitle(doc.path("title").asText("Unknown Title"));
                book.setAuthor(doc.has("author_name") ? 
                    doc.get("author_name").get(0).asText() : "Unknown Author");
                book.setIsbn(doc.has("isbn") ? 
                    doc.get("isbn").get(0).asText() : "");
                
                if (doc.has("cover_i")) {
                    String coverId = doc.get("cover_i").asText();
                    book.setCoverUrl("https://covers.openlibrary.org/b/id/" + coverId + "-M.jpg");
                }
                
                books.add(book);
            }
        }
        return books;
    }
}