package com.web.book.version.service;

import com.web.book.version.model.Favorite;
import com.web.book.version.model.User;
import com.web.book.version.model.Book;
import com.web.book.version.model.Show;
import com.web.book.version.repository.FavoriteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

@Service
public class FavoriteService {
    
    private static final Logger logger = LoggerFactory.getLogger(FavoriteService.class);
    
    @Autowired
    private FavoriteRepository favoriteRepository;

    // Métodos para agregar favoritos
    @Transactional
    public Favorite addBookToFavorites(User user, Book book) {
        try {
            if (favoriteRepository.existsByUserAndContenidoId(user, book.getIsbn())) {
                logger.warn("Book already in favorites for user: {}", user.getUsername());
                throw new RuntimeException("Book is already in favorites");
            }

            Favorite favorite = Favorite.createBookFavorite(
                user,
                book.getTitle(),
                book.getAuthor(),
                book.getDescription(),
                book.getCoverUrl(),
                book.getIsbn()
            );

            logger.info("Adding book to favorites for user: {}", user.getUsername());
            return favoriteRepository.save(favorite);
        } catch (Exception e) {
            logger.error("Error adding book to favorites: {}", e.getMessage());
            throw new RuntimeException("Error adding book to favorites", e);
        }
    }

    @Transactional
    public Favorite addShowToFavorites(User user, Show show) {
        try {
            if (favoriteRepository.existsByUserAndContenidoId(user, show.getId().toString())) {
                logger.warn("Show already in favorites for user: {}", user.getUsername());
                throw new RuntimeException("Show is already in favorites");
            }

            Favorite favorite = Favorite.createShowFavorite(
                user,
                show.getName(),
                "series".equalsIgnoreCase(show.getType()),
                show.getSummary(),
                show.getImageUrl(),
                show.getId().toString()
            );

            logger.info("Adding show to favorites for user: {}", user.getUsername());
            return favoriteRepository.save(favorite);
        } catch (Exception e) {
            logger.error("Error adding show to favorites: {}", e.getMessage());
            throw new RuntimeException("Error adding show to favorites", e);
        }
    }
    public List<Book> getUserFavoriteBooks(User user) {
        // Implement the logic to get the user's favorite books
        List<Favorite> favorites = favoriteRepository.findBookFavoritesByUser(user);
        List<Book> favoriteBooks = new ArrayList<>();
        for (Favorite favorite : favorites) {
            Book book = new Book();
            book.setTitle(favorite.getLibro());
            book.setAuthor(favorite.getAutor());
            book.setDescription(favorite.getDescripcion());
            book.setCoverUrl(favorite.getImagenUrl());
            book.setIsbn(favorite.getContenidoId());
            favoriteBooks.add(book);
        }
        return favoriteBooks;
    }
    public List<Show> getUserFavoriteShows(User user) {
        // Implement the logic to get the user's favorite shows
        List<Favorite> favorites = favoriteRepository.findSeriesFavoritesByUser(user);
        List<Show> favoriteShows = new ArrayList<>();
        for (Favorite favorite : favorites) {
            Show show = new Show();
            show.setName(favorite.getLibro());
            show.setSummary(favorite.getDescripcion());
            show.setId(Long.parseLong(favorite.getContenidoId()));
            favoriteShows.add(show);
        }
        return favoriteShows;
    }
    public long getUserFavoritesCount(User user) {
        try {
            return favoriteRepository.countByUser(user);
        } catch (Exception e) {
            logger.error("Error getting user favorites count: {}", e.getMessage());
            return 0;
        }
    }
    // Métodos para obtener favoritos
    public List<Favorite> getAllFavorites(User user) {
        return favoriteRepository.findByUser(user);
    }

    @Transactional(readOnly = true)
    public List<Favorite> getBookFavorites(User user) {
        try {
            return favoriteRepository.findBookFavoritesByUser(user);
        } catch (Exception e) {
            logger.error("Error getting book favorites: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    @Transactional(readOnly = true)
    public List<Favorite> getShowFavorites(User user) {
        try {
            return favoriteRepository.findSeriesFavoritesByUser(user);
        } catch (Exception e) {
            logger.error("Error getting show favorites: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    @Transactional(readOnly = true)
    public List<Favorite> getMovieFavorites(User user) {
        try {
            return favoriteRepository.findMovieFavoritesByUser(user);
        } catch (Exception e) {
            logger.error("Error getting movie favorites: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    // Método para verificar si un contenido está en favoritos
    public boolean isFavorite(User user, String contenidoId) {
        return favoriteRepository.existsByUserAndContenidoId(user, contenidoId);
    }

    // Método para eliminar un favorito
    @Transactional
    public void removeFavorite(User user, String contenidoId) {
        try {
            logger.info("Removing favorite with content ID {} for user: {}", 
                       contenidoId, user.getUsername());
            favoriteRepository.deleteByUserAndContenidoId(user, contenidoId);
        } catch (Exception e) {
            logger.error("Error removing favorite: {}", e.getMessage());
            throw new RuntimeException("Error removing favorite", e);
        }
    }

    // Métodos para obtener estadísticas
    public long getTotalFavorites(User user) {
        return favoriteRepository.countByUser(user);
    }

    public long getBookFavoritesCount(User user) {
        return favoriteRepository.countBookFavoritesByUser(user);
    }

    public long getShowFavoritesCount(User user) {
        return favoriteRepository.countSeriesFavoritesByUser(user);
    }

    public long getMovieFavoritesCount(User user) {
        return favoriteRepository.countMovieFavoritesByUser(user);
    }
    public List<Favorite> getUserFavorites(User user) {
        return favoriteRepository.findByUser(user);
    }
}
