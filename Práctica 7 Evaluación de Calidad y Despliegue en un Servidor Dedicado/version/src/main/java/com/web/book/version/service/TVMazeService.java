package com.web.book.version.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

import com.web.book.version.model.RecommendationCriteria;
import com.web.book.version.model.Show;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import com.web.book.version.model.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;

import org.springframework.cache.annotation.Cacheable;

@Service
public class TVMazeService {
    private static final Logger logger = LoggerFactory.getLogger(OpenLibraryService.class);
    private final String BASE_URL = "https://api.tvmaze.com";
    private final RestTemplate restTemplate;

    @Autowired
    private SearchHistoryService searchHistoryService;

    public TVMazeService() {
        this.restTemplate = new RestTemplate();
    }

    @Cacheable(value = "featuredShowsCache", key = "'featured'")
    public List<Show> getFeaturedShows() {
        try {
            // Obtener shows populares más actuales
            List<Long> featuredIds = Arrays.asList(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L);
            List<Show> shows = new ArrayList<>();
            
            for (Long id : featuredIds) {
                try {
                    String url = BASE_URL + "/shows/" + id;
                    ResponseEntity<Show> response = restTemplate.getForEntity(url, Show.class);
                    if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                        shows.add(response.getBody());
                    }
                    logger.info("Fetching show from: {}", url); // Añadir log
                    
                    Show show = restTemplate.getForObject(url, Show.class);
                    if (show != null) {
                        shows.add(show);
                    }
                } catch (Exception e) {
                    logger.error("Error fetching show with ID {}: {}", id, e.getMessage());
                    continue; // Continuar con el siguiente show si hay error
                }
            }
            if (shows.isEmpty()) {
                logger.warn("No shows were retrieved from TVMaze API {}");
            }
            return shows.stream()
                       .filter(show -> show.getImage() != null && show.getImage().getMedium() != null)
                       .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error fetching featured shows: ", e);
            return new ArrayList<>();
        }
    }

    @Cacheable(value = "showDetailsCache", key = "#id", unless = "#result == null")
    public Show getShowById(Long id) {
        try {
            String url = BASE_URL + "/shows/" + id;
            logger.info("Fetching show from URL: {}", url);
            
            ResponseEntity<Show> response = restTemplate.getForEntity(url, Show.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                logger.info("Successfully retrieved show with ID: {}", id);
                return response.getBody();
            } else {
                logger.warn("No show found with ID: {}", id);
                return null;
            }
        } catch (RestClientException e) {
            logger.error("Error getting show by ID {}: {}", id, e.getMessage());
            throw new RuntimeException("Error fetching show details", e);
        }
    }

    @Cacheable(value = "showSearchCache", key = "#query")
    public List<Show> searchShows(String query) {
        try {
            String encodedQuery = UriComponentsBuilder.fromHttpUrl(BASE_URL + "/search/shows")
                .queryParam("q", query)
                .build()
                .encode()
                .toUriString();
            
            logger.info("Searching shows with URL: {}", encodedQuery);
            
            ResponseEntity<String> response = restTemplate.getForEntity(encodedQuery, String.class);
            ObjectMapper mapper = new ObjectMapper();
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = mapper.readTree(response.getBody());
                List<Show> shows = new ArrayList<>();
                
                for (JsonNode node : root) {
                    if (node.has("show")) {
                        Show show = mapper.treeToValue(node.get("show"), Show.class);
                        shows.add(show);
                    }
                }
                
                logger.info("Found {} shows", shows.size());
                return shows;
            }
            
            return new ArrayList<>();
        } catch (Exception e) {
            logger.error("Error searching shows: {}", e.getMessage());
            throw new RuntimeException("Error searching shows", e);
        }
    }

    // No cachear búsquedas asociadas a usuarios para mantener el historial actualizado
    public List<Show> searchShows(String query, User user) {
        try {
            List<Show> shows = searchShows(query);
            
            // Registrar la búsqueda solo si se encontraron resultados
            if (!shows.isEmpty()) {
                Show firstShow = shows.get(0);
                boolean isSeries = checkIfSeriesType(firstShow); // Helper method
                searchHistoryService.saveShowSearch(
                    user,
                    query,
                    firstShow.getName(),
                    isSeries
                );
            }
            
            return shows;
        } catch (Exception e) {
            logger.error("Error searching shows: ", e);
            return new ArrayList<>();
        }
    }

    private boolean checkIfSeriesType(Show show) {
        // Lógica para determinar si es serie o película
        // Por defecto asumimos que es una serie si no hay información específica
        return show.getType() == null || "series".equalsIgnoreCase(show.getType());
    }

    @Cacheable(value = "similarShowsCache", key = "#genre", unless = "#result.empty")
    public List<Show> getSimilarShows(String genre) {
        try {
            // TVMaze no tiene endpoint directo para búsqueda por género,
            // así que buscaremos shows y filtraremos por género
            String url = BASE_URL + "/shows";
            ResponseEntity<Show[]> response = restTemplate.getForEntity(url, Show[].class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return Arrays.stream(response.getBody())
                    .filter(show -> Arrays.asList(show.getGenres()).contains(genre))
                    .limit(5)
                    .collect(Collectors.toList());
            }
            
            return new ArrayList<>();
        } catch (Exception e) {
            logger.error("Error getting similar shows: ", e);
            return new ArrayList<>();
        }
    }

    @Cacheable(value = "showRecommendationsCache", key = "#user.id + '-' + #criteria.hashCode()")
    public List<Show> getPersonalizedRecommendations(User user, RecommendationCriteria criteria) {
        try {
            List<Show> recommendations = new ArrayList<>();
            Set<Long> addedShows = new HashSet<>();

            // Obtener shows basados en géneros preferidos
            for (Map.Entry<String, Double> entry : criteria.getGenrePreferences().entrySet()) {
                if (entry.getValue() >= criteria.getMinimumRelevanceScore()) {
                    List<Show> genreShows = getSimilarShows(entry.getKey());
                    for (Show show : genreShows) {
                        if (addedShows.add(show.getId())) {
                            if (show.getRating() == null) {
                                show.setRating(new Show.Rating());
                            }
                            show.getRating().setAverage(entry.getValue());
                            recommendations.add(show);
                        }
                    }
                }
            }

            return recommendations.stream()
                .sorted((s1, s2) -> Double.compare(
                    s2.getRating().getAverage(),
                    s1.getRating().getAverage()))
                .limit(criteria.getMaxRecommendations())
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            logger.error("Error getting personalized recommendations: ", e);
            return new ArrayList<>();
        }
    }

    // Método auxiliar para procesar resultados de búsqueda
    private List<Show> processSearchResults(ResponseEntity<Show[]> response) {
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            return Arrays.stream(response.getBody())
                .filter(show -> show.getImage() != null && show.getImage().getMedium() != null)
                .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class SearchResult {
        private Show show;
        public Show getShow() { return show; }
        public void setShow(Show show) { this.show = show; }
    }
}