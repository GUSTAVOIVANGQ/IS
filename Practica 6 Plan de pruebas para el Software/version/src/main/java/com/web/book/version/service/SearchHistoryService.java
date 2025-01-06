package com.web.book.version.service;

import com.web.book.version.dto.SearchHistoryDTO;
import com.web.book.version.model.SearchHistory;
import com.web.book.version.model.User;
import com.web.book.version.repository.SearchHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class SearchHistoryService {
    private static final Logger logger = LoggerFactory.getLogger(SearchHistoryService.class);
    
    @Autowired
    private SearchHistoryRepository searchHistoryRepository;
    
    public SearchHistory saveBookSearch(User user, String searchTerm, String bookTitle, String author) {
        try {
            SearchHistory history = SearchHistory.createForBook(user, searchTerm, bookTitle, author);
            return searchHistoryRepository.save(history);
        } catch (Exception e) {
            logger.error("Error saving book search history: {}", e.getMessage());
            throw new RuntimeException("Error saving search history", e);
        }
    }
    
    public SearchHistory saveShowSearch(User user, String searchTerm, String title, boolean isSeries) {
        try {
            SearchHistory history = SearchHistory.createForShow(user, searchTerm, title, isSeries);
            return searchHistoryRepository.save(history);
        } catch (Exception e) {
            logger.error("Error saving show search history: {}", e.getMessage());
            throw new RuntimeException("Error saving search history", e);
        }
    }
    
    public List<SearchHistory> getUserHistory(User user) {
        try {
            logger.info("Fetching search history for user: {}", user.getUsername());
            List<SearchHistory> history = searchHistoryRepository.findByUserOrderByFechaBusquedaDesc(user);
            logger.info("Found {} history entries", history.size());
            return history;
        } catch (Exception e) {
            logger.error("Error fetching user history: {}", e.getMessage());
            throw new RuntimeException("Error fetching search history", e);
        }
    }

    public List<SearchHistory> getUserHistoryWithFilters(User user, String search, String type, String date) {
        try {
            if (search == null && type == null && date == null) {
                return getUserHistory(user);
            }

            LocalDateTime dateFilter = date != null ? LocalDate.parse(date).atStartOfDay() : null;
            return searchHistoryRepository.findByFilters(user, search, type, dateFilter);
        } catch (Exception e) {
            logger.error("Error filtering user history: {}", e.getMessage());
            throw new RuntimeException("Error filtering search history", e);
        }
    }

    public List<SearchHistory> getAllHistoryWithFilters(String search, String type, String date) {
        try {
            if (search == null && type == null && date == null) {
                return getAllHistory();
            }

            LocalDateTime dateFilter = date != null ? LocalDate.parse(date).atStartOfDay() : null;
            return searchHistoryRepository.findByFiltersAdmin(search, type, dateFilter);
        } catch (Exception e) {
            logger.error("Error filtering all history: {}", e.getMessage());
            throw new RuntimeException("Error filtering search history", e);
        }
    }    
    
    public List<SearchHistory> getUserBookSearches(User user) {
        try {
            logger.info("Fetching book search history for user: {}", user.getUsername());
            List<SearchHistory> bookHistory = searchHistoryRepository.findBookSearchesByUser(user);
            logger.info("Found {} book searches", bookHistory.size());
            return bookHistory;
        } catch (Exception e) {
            logger.error("Error fetching book search history: {}", e.getMessage());
            throw new RuntimeException("Error fetching book search history", e);
        }
    }
    
    public List<SearchHistory> getUserShowSearches(User user) {
        return searchHistoryRepository.findShowSearchesByUser(user);
    }
    
    public List<SearchHistory> getAllHistory() {
        return searchHistoryRepository.findAllByOrderByFechaBusquedaDesc();
    }
    
    @Transactional
    public void deleteUserHistory(User user) {
        try {
            logger.info("Deleting search history for user: {}", user.getUsername());
            searchHistoryRepository.deleteByUser(user);
            logger.info("Successfully deleted search history for user: {}", user.getUsername());
        } catch (Exception e) {
            logger.error("Error deleting user history: {}", e.getMessage());
            throw new RuntimeException("Error deleting search history", e);
        }
    }

    @Transactional
    public void deleteUserBookHistory(User user) {
        try {
            logger.info("Deleting book search history for user: {}", user.getUsername());
            searchHistoryRepository.deleteBookSearchesByUser(user);
            logger.info("Successfully deleted book search history for user: {}", user.getUsername());
        } catch (Exception e) {
            logger.error("Error deleting book history: {}", e.getMessage());
            throw new RuntimeException("Error deleting book search history", e);
        }
    }       

    @Transactional
    public void clearUserShowHistory(User user) {
        try {
            List<SearchHistory> showHistories = searchHistoryRepository.findShowSearchesByUser(user);
            searchHistoryRepository.deleteAll(showHistories);
            logger.info("Successfully cleared show history for user: {}", user.getUsername());
        } catch (Exception e) {
            logger.error("Error clearing show history: {}", e.getMessage());
            throw new RuntimeException("Error clearing show history", e);
        }
    }
    public long getUserSearchCount(User user) {
        try {
            return searchHistoryRepository.countByUser(user);
        } catch (Exception e) {
            logger.error("Error getting user search count: {}", e.getMessage());
            return 0;
        }
    }

        // Agregar en SearchHistoryService.java
    public SearchHistoryDTO convertToDTO(SearchHistory searchHistory) {
        SearchHistoryDTO dto = new SearchHistoryDTO();
        dto.setId(searchHistory.getId());
        dto.setUsername(searchHistory.getUser().getUsername());
        dto.setFechaBusqueda(searchHistory.getFechaBusqueda());
        dto.setTerminosBusqueda(searchHistory.getTerminosBusqueda());
        dto.setLibro(searchHistory.getLibro());
        dto.setAutor(searchHistory.getAutor());
        dto.setSerie(searchHistory.getSerie());
        dto.setPelicula(searchHistory.getPelicula());
        return dto;
    }

    public void deleteSearchRecord(Long id) {
        searchHistoryRepository.deleteById(id);
    }

    public void deleteAllHistory() {
        searchHistoryRepository.deleteAll();
    }
}
