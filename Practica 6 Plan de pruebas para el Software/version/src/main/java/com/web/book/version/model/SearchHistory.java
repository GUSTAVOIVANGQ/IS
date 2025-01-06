package com.web.book.version.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "historial_busquedas")
public class SearchHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_busqueda")
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private User user;
    
    @Column(name = "fecha_busqueda")
    private LocalDateTime fechaBusqueda = LocalDateTime.now();
    
    @Column(name = "terminos_busqueda", nullable = false)
    private String terminosBusqueda;
    
    @Column(name = "libro")
    private String libro = "";
    
    @Column(name = "autor")
    private String autor = "";
    
    @Column(name = "serie")
    private String serie = "";
    
    @Column(name = "pelicula")
    private String pelicula = "";

    // Constructor para búsqueda de libros
    public static SearchHistory createForBook(User user, String searchTerm, String bookTitle, String author) {
        SearchHistory history = new SearchHistory();
        history.setUser(user);
        history.setTerminosBusqueda(searchTerm);
        history.setLibro(bookTitle);
        history.setAutor(author);
        return history;
    }

    // Constructor para búsqueda de series/películas
    public static SearchHistory createForShow(User user, String searchTerm, String title, boolean isSeries) {
        SearchHistory history = new SearchHistory();
        history.setUser(user);
        history.setTerminosBusqueda(searchTerm);
        if (isSeries) {
            history.setSerie(title);
        } else {
            history.setPelicula(title);
        }
        return history;
    }

    // Asegúrate de que estos getters y setters existan
    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getFechaBusqueda() {
        return fechaBusqueda;
    }

    public String getTerminosBusqueda() {
        return terminosBusqueda == null ? "" : terminosBusqueda;
    }

    public String getLibro() {
        return libro == null ? "" : libro;
    }

    public String getAutor() {
        return autor == null ? "" : autor;
    }
}