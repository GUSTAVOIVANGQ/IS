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
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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

    @Cacheable(value = "featuredBooksCache", key = "'featured'")
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
        
    @Cacheable(value = "bookSearchCache", key = "#query + '-' + #page + '-' + #size")
    public List<Book> searchBooks(String query, int page, int size) {
        try {
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8.toString());
            String url = BASE_URL + "?q=" + encodedQuery + "&page=" + page + "&limit=" + size;
            
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            
            if (!response.getStatusCode().is2xxSuccessful()) {
                logger.error("Error from OpenLibrary API: {}", response.getStatusCode());
                throw new RuntimeException("Error calling OpenLibrary API");
            }

            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode docs = root.get("docs");
            
            return parseBookResults(docs);
        } catch (Exception e) {
            logger.error("Error searching books: {}", e.getMessage());
            throw new RuntimeException("Error searching books", e);
        }
    }

    @Cacheable(value = "bookSearchCache", key = "#query")
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

    // No cachear búsquedas asociadas a usuarios para mantener historial actualizado
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

    @Cacheable(value = "bookDetailsCache", key = "#isbn", unless = "#result == null")
    public Book getBookDetails(String isbn) {
        try {
            String url = "https://openlibrary.org/api/books?bibkeys=ISBN:" + isbn + "&format=json&jscmd=data";
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            
            if (!response.getStatusCode().is2xxSuccessful()) {
                logger.error("Error response from OpenLibrary API: {}", response.getStatusCode());
                return null;
            }

            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode bookData = root.get("ISBN:" + isbn);
            
            if (bookData == null || bookData.isEmpty()) {
                return null;
            }

            return mapJsonToBook(bookData, isbn);
        } catch (Exception e) {
            logger.error("Error getting book details: {}", e.getMessage());
            throw new RuntimeException("Error getting book details", e);
        }
    }

    private Book mapJsonToBook(JsonNode bookData, String isbn) {
        Book book = new Book();
        book.setIsbn(isbn);
        book.setTitle(bookData.path("title").asText("Sin título"));
        
        // Manejar autores
        JsonNode authors = bookData.path("authors");
        if (authors.isArray() && authors.size() > 0) {
            book.setAuthor(authors.get(0).path("name").asText("Autor desconocido"));
        }

        // Manejar géneros/subjects
        JsonNode subjects = bookData.path("subjects");
        if (subjects.isArray()) {
            String[] genres = new String[subjects.size()];
            for (int i = 0; i < subjects.size(); i++) {
                genres[i] = subjects.get(i).asText();
            }
            book.setGenres(genres);
        }

        // Manejar editorial
        JsonNode publishers = bookData.path("publishers");
        if (publishers.isArray() && publishers.size() > 0) {
            book.setPublisher(publishers.get(0).path("name").asText());
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

        return book;
    }

    @Cacheable(value = "similarBooksCache", key = "#genre + '-' + #author", unless = "#result.empty")
    public List<Book> getSimilarBooks(String genre, String author) {
        try {
            StringBuilder queryBuilder = new StringBuilder();
            if (genre != null && !genre.isEmpty()) {
                queryBuilder.append("subject:").append(URLEncoder.encode(genre, StandardCharsets.UTF_8));
            }
            if (author != null && !author.isEmpty()) {
                if (queryBuilder.length() > 0) queryBuilder.append("+OR+");
                queryBuilder.append("author:").append(URLEncoder.encode(author, StandardCharsets.UTF_8));
            }
            
            String url = BASE_URL + "?q=" + queryBuilder.toString() + "&limit=5";
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                return parseBookResults(root.get("docs"));
            }
            
            return new ArrayList<>();
        } catch (Exception e) {
            logger.error("Error getting similar books: {}", e.getMessage());
            throw new RuntimeException("Error getting similar books", e);
        }
    }

    @Cacheable(value = "recommendationsCache", key = "#user.id + '-' + #criteria.hashCode()")
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