package com.web.book.version.repository;

import com.web.book.version.model.Favorite;
import com.web.book.version.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    
    // Encontrar todos los favoritos de un usuario
    List<Favorite> findByUser(User user);
    
    // Encontrar favoritos por tipo (libros)
    @Query("SELECT f FROM Favorite f WHERE f.user = :user AND f.libro IS NOT NULL AND f.libro <> ''")
    List<Favorite> findBookFavoritesByUser(@Param("user") User user);
    
    // Encontrar favoritos por tipo (series)
    @Query("SELECT f FROM Favorite f WHERE f.user = :user AND f.serie IS NOT NULL AND f.serie <> ''")
    List<Favorite> findSeriesFavoritesByUser(@Param("user") User user);
    
    // Encontrar favoritos por tipo (películas)
    @Query("SELECT f FROM Favorite f WHERE f.user = :user AND f.pelicula IS NOT NULL AND f.pelicula <> ''")
    List<Favorite> findMovieFavoritesByUser(@Param("user") User user);
    
    // Verificar si un contenido ya está en favoritos
    boolean existsByUserAndContenidoId(User user, String contenidoId);
    
    // Buscar un favorito específico
    Optional<Favorite> findByUserAndContenidoId(User user, String contenidoId);
    
    // Eliminar un favorito específico
    void deleteByUserAndContenidoId(User user, String contenidoId);
    
    // Contar favoritos por usuario
    long countByUser(User user);
    
    // Contar favoritos por tipo
    @Query("SELECT COUNT(f) FROM Favorite f WHERE f.user = :user AND f.libro != ''")
    long countBookFavoritesByUser(@Param("user") User user);
    
    @Query("SELECT COUNT(f) FROM Favorite f WHERE f.user = :user AND f.serie != ''")
    long countSeriesFavoritesByUser(@Param("user") User user);
    
    @Query("SELECT COUNT(f) FROM Favorite f WHERE f.user = :user AND f.pelicula != ''")
    long countMovieFavoritesByUser(@Param("user") User user);
}
