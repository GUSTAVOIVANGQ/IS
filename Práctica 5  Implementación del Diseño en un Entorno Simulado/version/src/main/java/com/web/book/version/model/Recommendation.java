package com.web.book.version.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "recomendaciones")
public class Recommendation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_recomendacion")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_contenido", nullable = false)
    private ContentType contentType;

    @Column(name = "id_contenido", nullable = false)
    private String contentId;

    @Column(name = "titulo", nullable = false)
    private String title;

    @Column(name = "descripcion")
    private String description;

    @Column(name = "imagen_url")
    private String imageUrl;

    @Column(name = "fecha_generacion")
    private LocalDateTime generatedDate = LocalDateTime.now();

    @Column(name = "relevancia")
    private Double relevanceScore = 0.0;

    // Campos específicos para libros
    @Column(name = "libro")
    private String book = "";

    @Column(name = "autor")
    private String author = "";

    // Campos específicos para contenido audiovisual
    @Column(name = "serie")
    private String series = "";

    @Column(name = "pelicula")
    private String movie = "";

    public enum ContentType {
        BOOK,
        SERIES,
        MOVIE
    }

    // Constructor para recomendaciones de libros
    public static Recommendation createBookRecommendation(User user, String contentId, 
            String title, String author, String imageUrl, Double relevance) {
        Recommendation rec = new Recommendation();
        rec.setUser(user);
        rec.setContentType(ContentType.BOOK);
        rec.setContentId(contentId);
        rec.setTitle(title);
        rec.setBook(title);
        rec.setAuthor(author);
        rec.setImageUrl(imageUrl);
        rec.setRelevanceScore(relevance);
        return rec;
    }

    // Constructor para recomendaciones de series/películas
    public static Recommendation createShowRecommendation(User user, String contentId,
            String title, boolean isSeries, String imageUrl, Double relevance) {
        Recommendation rec = new Recommendation();
        rec.setUser(user);
        rec.setContentType(isSeries ? ContentType.SERIES : ContentType.MOVIE);
        rec.setContentId(contentId);
        rec.setTitle(title);
        if (isSeries) {
            rec.setSeries(title);
        } else {
            rec.setMovie(title);
        }
        rec.setImageUrl(imageUrl);
        rec.setRelevanceScore(relevance);
        return rec;
    }

    // Getters y setters específicos
    public void setSeries(String series) {
        this.series = series;
        this.movie = ""; // Si es serie, no es película
    }

    public void setMovie(String movie) {
        this.movie = movie;
        this.series = ""; // Si es película, no es serie
    }

    public void setBook(String book) {
        this.book = book;
        this.series = "";
        this.movie = ""; // Si es libro, no es contenido audiovisual
    }
}
