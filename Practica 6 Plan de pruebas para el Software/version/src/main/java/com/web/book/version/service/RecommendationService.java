package com.web.book.version.service;

import com.web.book.version.model.*;
import com.web.book.version.repository.RecommendationRepository;
import com.web.book.version.repository.SearchHistoryRepository;

import lombok.extern.slf4j.Slf4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j  // Use Lombok's @Slf4j annotation
public class RecommendationService {
    
    private static final Logger logger = LoggerFactory.getLogger(RecommendationService.class);
    
    @Autowired
    private RecommendationRepository recommendationRepository;
    
    @Autowired
    private SearchHistoryRepository searchHistoryRepository;
    
    @Autowired
    private OpenLibraryService openLibraryService;
    
    @Autowired
    private TVMazeService tvMazeService;
    
    @Autowired
    private FavoriteService favoriteService;

    // Obtener recomendaciones personalizadas para un usuario
    @Transactional
    public List<Recommendation> getPersonalizedRecommendations(User user) {
        try {
            // 1. Verificar si ya existen recomendaciones recientes
            LocalDateTime oneDayAgo = LocalDateTime.now().minusHours(24);
            List<Recommendation> existingRecommendations = 
                recommendationRepository.findByUserAndGeneratedDateAfter(user, oneDayAgo);
            
            if (!existingRecommendations.isEmpty()) {
                logger.info("Returning {} existing recommendations for user {}", 
                    existingRecommendations.size(), user.getUsername());
                return existingRecommendations;
            }

            // 2. Si no hay recomendaciones recientes, generar nuevas
            logger.info("Generating new recommendations for user {}", user.getUsername());
            
            // Limpiar recomendaciones antiguas
            cleanupOldRecommendations(user);

            List<Recommendation> recommendations = new ArrayList<>();

            // 3. Obtener datos del usuario
            List<SearchHistory> bookSearchHistory = searchHistoryRepository.findBookSearchesByUser(user);
            List<SearchHistory> showSearchHistory = searchHistoryRepository.findShowSearchesByUser(user);
            List<Favorite> favorites = favoriteService.getUserFavorites(user);

            // 4. Generar recomendaciones de libros
            if (!bookSearchHistory.isEmpty() || !favorites.isEmpty()) {
                List<Book> bookRecommendations = openLibraryService.getPersonalizedRecommendations(user, 
                    new RecommendationCriteria());
                
                for (Book book : bookRecommendations) {
                    Recommendation rec = Recommendation.createBookRecommendation(
                        user,
                        book.getIsbn(),
                        book.getTitle(),
                        book.getAuthor(),
                        book.getCoverUrl(),
                        0.8 // relevancia por defecto
                    );
                    recommendations.add(recommendationRepository.save(rec));
                }
            }

            // 5. Generar recomendaciones de shows
            if (!showSearchHistory.isEmpty() || !favorites.isEmpty()) {
                List<Show> showRecommendations = tvMazeService.getPersonalizedRecommendations(user, 
                    new RecommendationCriteria());
                
                for (Show show : showRecommendations) {
                    Recommendation rec = Recommendation.createShowRecommendation(
                        user,
                        show.getId().toString(),
                        show.getName(),
                        true,
                        show.getImage() != null ? show.getImage().getMedium() : "",
                        0.8 // relevancia por defecto
                    );
                    recommendations.add(recommendationRepository.save(rec));
                }
            }

            logger.info("Generated and saved {} new recommendations for user {}", 
                recommendations.size(), user.getUsername());
            
            return recommendations;

        } catch (Exception e) {
            logger.error("Error generating recommendations: ", e);
            return new ArrayList<>();
        }
    }

    // Agregar método para generar recomendaciones iniciales
    @Transactional
    public List<Recommendation> generateInitialRecommendations(User user) {
        try {
            // Limpiar recomendaciones antiguas
            cleanupOldRecommendations(user);
            
            List<Recommendation> recommendations = new ArrayList<>();
            
            // Generar recomendaciones basadas en historial
            List<SearchHistory> bookSearches = searchHistoryRepository.findBookSearchesByUser(user);
            for (SearchHistory search : bookSearches) {
                List<Book> similarBooks = openLibraryService.getSimilarBooks(
                    search.getLibro(), 
                    search.getAutor()
                );
                
                for (Book book : similarBooks) {
                    double relevanceScore = calculateRelevanceScore(search.getFechaBusqueda());
                    
                    Recommendation rec = new Recommendation();
                    rec.setUser(user);
                    rec.setContentType(Recommendation.ContentType.BOOK);
                    rec.setContentId(book.getIsbn());
                    rec.setTitle(book.getTitle());
                    rec.setAuthor(book.getAuthor());
                    rec.setImageUrl(book.getCoverUrl());
                    rec.setRelevanceScore(relevanceScore);
                    rec.setGeneratedDate(LocalDateTime.now());
                    
                    // Guardar la recomendación
                    recommendations.add(recommendationRepository.save(rec));
                }
            }
            
            // Generar recomendaciones de shows
            List<SearchHistory> showSearches = searchHistoryRepository.findShowSearchesByUser(user);
            for (SearchHistory search : showSearches) {
                List<Show> similarShows = tvMazeService.getSimilarShows(search.getTerminosBusqueda());
                
                for (Show show : similarShows) {
                    double relevanceScore = calculateRelevanceScore(search.getFechaBusqueda());
                    
                    Recommendation rec = new Recommendation();
                    rec.setUser(user);
                    rec.setContentType(Recommendation.ContentType.SERIES);
                    rec.setContentId(show.getId().toString());
                    rec.setTitle(show.getName());
                    rec.setImageUrl(show.getImageUrl());
                    rec.setRelevanceScore(relevanceScore);
                    rec.setGeneratedDate(LocalDateTime.now());
                    
                    // Guardar la recomendación
                    recommendations.add(recommendationRepository.save(rec));
                }
            }
            
            logger.info("Successfully generated and saved {} recommendations for user {}", 
                       recommendations.size(), user.getUsername());
            return recommendations;
            
        } catch (Exception e) {
            logger.error("Error generating recommendations: {}", e.getMessage(), e);
            throw new RuntimeException("Error generating recommendations", e);
        }
    }

    private double calculateRelevanceScore(LocalDateTime activityDate) {
        long daysAgo = java.time.Duration.between(activityDate, LocalDateTime.now()).toDays();
        return Math.max(0.1, 1.0 - (daysAgo / 30.0)); // Más reciente = mayor relevancia
    }

    private RecommendationCriteria buildCriteriaFromUserActivity(
            List<SearchHistory> searchHistory,
            List<Favorite> favorites) {
        RecommendationCriteria criteria = new RecommendationCriteria();
        
        // Analizar historial de búsquedas
        for (SearchHistory search : searchHistory) {
            if (!search.getLibro().isEmpty()) {
                criteria.incrementGenreWeight("book", 0.3);
            }
            if (!search.getSerie().isEmpty() || !search.getPelicula().isEmpty()) {
                criteria.incrementGenreWeight("show", 0.3);
            }
        }

        // Analizar favoritos
        for (Favorite favorite : favorites) {
            if (!favorite.getLibro().isEmpty()) {
                criteria.incrementGenreWeight("book", 0.5);
            }
            if (!favorite.getSerie().isEmpty() || !favorite.getPelicula().isEmpty()) {
                criteria.incrementGenreWeight("show", 0.5);
            }
        }

        return criteria;
    }

    private double calculateRelevanceScore(Object content, RecommendationCriteria criteria) {
        // Implementar lógica de puntuación basada en el contenido y criterios
        return 0.8; // Valor de ejemplo
    }

    // Construir criterios basados en el historial del usuario
    private RecommendationCriteria buildUserCriteria(User user) {
        RecommendationCriteria criteria = RecommendationCriteria.createDefault();
        
        // Analizar historial de búsqueda
        List<SearchHistory> searchHistory = searchHistoryRepository.findByUser(user);
        updateCriteriaFromHistory(criteria, searchHistory);
        
        return criteria;
    }

    // Generar recomendaciones de libros
    private List<Recommendation> generateBookRecommendations(User user, RecommendationCriteria criteria) {
        List<Recommendation> bookRecommendations = new ArrayList<>();
        
        try {
            // Obtener libros basados en el historial
            List<SearchHistory> bookSearches = searchHistoryRepository.findBookSearchesByUser(user);
            
            // Generar recomendaciones por cada búsqueda reciente
            for (SearchHistory search : bookSearches) {
                List<Book> similarBooks = openLibraryService.searchBooks(search.getTerminosBusqueda());
                
                for (Book book : similarBooks) {
                    double relevanceScore = calculateBookRelevanceScore(book, criteria, search.getFechaBusqueda());
                    
                    if (criteria.meetsMinimumCriteria(relevanceScore, "")) {
                        Recommendation rec = Recommendation.createBookRecommendation(
                            user,
                            book.getIsbn(),
                            book.getTitle(),
                            book.getAuthor(),
                            book.getCoverUrl(),
                            relevanceScore
                        );
                        // Guardar la recomendación en la base de datos
                        rec = recommendationRepository.save(rec);
                        bookRecommendations.add(rec);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error generating book recommendations: {}", e.getMessage());
        }
        
        return bookRecommendations;
    }

    // Generar recomendaciones de series y películas
    private List<Recommendation> generateShowRecommendations(User user, RecommendationCriteria criteria) {
        List<Recommendation> showRecommendations = new ArrayList<>();
        
        try {
            // Obtener búsquedas de shows
            List<SearchHistory> showSearches = searchHistoryRepository.findShowSearchesByUser(user);
            
            // Generar recomendaciones por cada búsqueda reciente
            for (SearchHistory search : showSearches) {
                List<Show> similarShows = tvMazeService.searchShows(search.getTerminosBusqueda());
                
                for (Show show : similarShows) {
                    double relevanceScore = calculateShowRelevanceScore(show, criteria, search.getFechaBusqueda());
                    
                    if (criteria.meetsMinimumCriteria(relevanceScore, show.getGenres()[0])) {
                        Recommendation rec = Recommendation.createShowRecommendation(
                            user,
                            show.getId().toString(),
                            show.getName(),
                            true,
                            show.getImage().getMedium(),
                            relevanceScore
                        );
                        // Guardar la recomendación en la base de datos
                        rec = recommendationRepository.save(rec);
                        showRecommendations.add(rec);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error generating show recommendations: {}", e.getMessage());
        }
        
        return showRecommendations;
    }

    // Calcular relevancia para libros
    private double calculateBookRelevanceScore(Book book, RecommendationCriteria criteria, LocalDateTime activityDate) {
        return criteria.calculateRelevanceScore("", activityDate);
    }

    // Calcular relevancia para shows
    private double calculateShowRelevanceScore(Show show, RecommendationCriteria criteria, LocalDateTime activityDate) {
        return criteria.calculateRelevanceScore(
            show.getGenres().length > 0 ? show.getGenres()[0] : "",
            activityDate
        );
    }

    // Actualizar criterios basados en historial
    private void updateCriteriaFromHistory(RecommendationCriteria criteria, List<SearchHistory> history) {
        Map<String, Integer> genreCounts = new HashMap<>();
        
        for (SearchHistory search : history) {
            if (!search.getLibro().isEmpty()) {
                genreCounts.merge("Fiction", 1, Integer::sum); // Ejemplo simple
            }
            criteria.incrementSearchCount();
        }
        
        // Actualizar preferencias de género basadas en conteos
        genreCounts.forEach((genre, count) -> {
            double weight = Math.min(1.0, count / 10.0);
            criteria.updateGenrePreference(genre, weight);
        });
    }

    // Limpiar recomendaciones antiguas
    private void cleanupOldRecommendations(User user) {
        LocalDateTime threshold = LocalDateTime.now().minusDays(30);
        recommendationRepository.deleteByUserAndGeneratedDateBefore(user, threshold);
    }

    public List<Recommendation> getUserRecommendations(User user) {
        try {
            // Obtener recomendaciones existentes no más antiguas que 24 horas
            LocalDateTime oneDayAgo = LocalDateTime.now().minusHours(24);
            List<Recommendation> recommendations = 
                recommendationRepository.findByUserAndGeneratedDateAfter(user, oneDayAgo);
            
            if (recommendations.isEmpty()) {
                // Si no hay recomendaciones recientes, generar nuevas
                recommendations = generateInitialRecommendations(user);
            }
            
            return recommendations;
        } catch (Exception e) {
            logger.error("Error getting user recommendations: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
}
