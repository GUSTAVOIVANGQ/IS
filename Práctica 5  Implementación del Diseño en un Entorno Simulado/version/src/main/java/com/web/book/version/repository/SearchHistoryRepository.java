package com.web.book.version.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.web.book.version.model.SearchHistory;
import com.web.book.version.model.User;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SearchHistoryRepository extends JpaRepository<SearchHistory, Long> {
    // Búsquedas básicas
    List<SearchHistory> findByUserOrderByFechaBusquedaDesc(User user);
    List<SearchHistory> findAllByOrderByFechaBusquedaDesc();
    List<SearchHistory> findByUser(User user);
    List<SearchHistory> findByUserAndFechaBusquedaAfter(User user, LocalDateTime fechaBusqueda);

    // Búsquedas específicas por tipo de contenido
    @Query("SELECT sh FROM SearchHistory sh WHERE sh.user = :user AND sh.libro IS NOT NULL AND sh.libro != '' ORDER BY sh.fechaBusqueda DESC")
    List<SearchHistory> findBookSearchesByUser(@Param("user") User user);
    
    @Query("SELECT sh FROM SearchHistory sh WHERE sh.user = :user AND (sh.serie != '' OR sh.pelicula != '')")
    List<SearchHistory> findShowSearchesByUser(@Param("user") User user);

    @Query("SELECT sh FROM SearchHistory sh WHERE sh.user = :user " +
    "AND (:search IS NULL OR sh.terminosBusqueda LIKE %:search%) " +
    "AND (:type IS NULL OR " +
    "     (sh.libro != '' AND :type = 'LIBRO') OR " +
    "     (sh.serie != '' AND :type = 'SERIE') OR " +
    "     (sh.pelicula != '' AND :type = 'PELÍCULA')) " +
    "AND (:date IS NULL OR DATE(sh.fechaBusqueda) = DATE(:date)) " +
    "ORDER BY sh.fechaBusqueda DESC")
    
    List<SearchHistory> findByFilters(
    @Param("user") User user,
    @Param("search") String search,
    @Param("type") String type,
    @Param("date") LocalDateTime date); 

    @Query("SELECT sh FROM SearchHistory sh WHERE " +
        "(:search IS NULL OR sh.terminosBusqueda LIKE %:search%) " +
        "AND (:type IS NULL OR " +
        "     (sh.libro != '' AND :type = 'LIBRO') OR " +
        "     (sh.serie != '' AND :type = 'SERIE') OR " +
        "     (sh.pelicula != '' AND :type = 'PELÍCULA')) " +
        "AND (:date IS NULL OR DATE(sh.fechaBusqueda) = DATE(:date)) " +
        "ORDER BY sh.fechaBusqueda DESC")
    List<SearchHistory> findByFiltersAdmin(
    @Param("search") String search,
    @Param("type") String type,
    @Param("date") LocalDateTime date);    
    
    // Búsquedas por período de tiempo
    List<SearchHistory> findByUserAndFechaBusquedaBetween(
        User user, 
        LocalDateTime startDate, 
        LocalDateTime endDate
    );
    
    // Búsquedas por término
    List<SearchHistory> findByUserAndTerminosBusquedaContaining(
        User user, 
        String searchTerm
    );
    
    // Conteo de búsquedas
    Long countByUser(User user);
    
    // Eliminar historial
    void deleteByUser(User user);

    @Modifying
    @Query("DELETE FROM SearchHistory sh WHERE sh.user = :user AND sh.libro IS NOT NULL AND sh.libro != ''")
    void deleteBookSearchesByUser(@Param("user") User user);    
}