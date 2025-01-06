package com.web.book.version.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationCriteria {
    
    // Pesos para diferentes factores de recomendación
    private double favoriteWeight = 0.4;
    private double searchHistoryWeight = 0.3;
    private double recentActivityWeight = 0.3;
    
    // Límites de tiempo para considerar actividad reciente
    private int recentDaysThreshold = 30;
    
    // Número máximo de recomendaciones a generar
    private int maxRecommendations = 10;
    
    // Preferencias de género/categoría
    private Map<String, Double> genrePreferences = new HashMap<>();
    private Set<String> excludedGenres = new HashSet<>();
    
    // Historial de búsqueda y favoritos
    private LocalDateTime lastSearchDate;
    private int searchCount = 0;
    private int favoriteCount = 0;

    // Factores de contenido
    private ContentType preferredType = ContentType.ALL;
    private double minimumRelevanceScore = 0.5;

    public enum ContentType {
        ALL,
        BOOKS_ONLY,
        SHOWS_ONLY
    }

    // Métodos de utilidad para calcular relevancia
    public double calculateRelevanceScore(String genre, LocalDateTime activityDate) {
        double score = 0.0;
        
        // Calcular peso por género
        score += genrePreferences.getOrDefault(genre, 0.0) * favoriteWeight;
        
        // Calcular peso por actividad reciente
        if (activityDate != null) {
            long daysSinceActivity = java.time.Duration.between(
                activityDate, LocalDateTime.now()).toDays();
            if (daysSinceActivity <= recentDaysThreshold) {
                score += recentActivityWeight * 
                    (1.0 - ((double) daysSinceActivity / recentDaysThreshold));
            }
        }

        return score;
    }

    // Métodos para actualizar preferencias
    public void updateGenrePreference(String genre, double weight) {
        genrePreferences.put(genre, Math.min(1.0, Math.max(0.0, weight)));
    }

    public void addExcludedGenre(String genre) {
        excludedGenres.add(genre);
    }

    public void incrementSearchCount() {
        this.searchCount++;
        this.lastSearchDate = LocalDateTime.now();
    }

    public void incrementFavoriteCount() {
        this.favoriteCount++;
    }

    // Método para verificar si un contenido cumple con los criterios mínimos
    public boolean meetsMinimumCriteria(double relevanceScore, String genre) {
        return relevanceScore >= minimumRelevanceScore && 
               !excludedGenres.contains(genre);
    }

    // Constructor con valores predeterminados
    public static RecommendationCriteria createDefault() {
        RecommendationCriteria criteria = new RecommendationCriteria();
        // Configurar algunos géneros populares por defecto
        criteria.genrePreferences.put("Fiction", 0.5);
        criteria.genrePreferences.put("Drama", 0.5);
        criteria.genrePreferences.put("Comedy", 0.5);
        return criteria;
    }
    public void incrementGenreWeight(String genre, double weight) {
        genrePreferences.merge(genre, weight, Double::sum);
    }
}
