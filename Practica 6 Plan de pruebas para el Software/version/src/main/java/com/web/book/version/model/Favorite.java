package com.web.book.version.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "favoritos")
public class Favorite {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_favorito")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private User user;

    @Column(name = "libro")
    private String libro;

    @Column(name = "autor")
    private String autor;

    @Column(name = "serie")
    private String serie;

    @Column(name = "pelicula")
    private String pelicula;

    @Column(name = "id_contenido")
    private String contenidoId;

    @Column(name = "titulo", nullable = false)
    private String titulo;

    @Column(name = "fecha_agregado")
    private LocalDateTime fechaAgregado;

    @Column(name = "descripcion")
    private String descripcion;

    @Column(name = "imagen_url")
    private String imagenUrl;

    // Constructor por defecto
    public Favorite() {
        this.fechaAgregado = LocalDateTime.now();
    }

    // Constructores para diferentes tipos de favoritos
    public static Favorite createBookFavorite(User user, String titulo, String autor, String descripcion, String imagenUrl, String contenidoId) {
        Favorite favorite = new Favorite();
        favorite.setUser(user);
        favorite.setTitulo(titulo);
        favorite.setLibro(titulo);
        favorite.setAutor(autor);
        favorite.setSerie("");
        favorite.setPelicula("");
        favorite.setDescripcion(descripcion);
        favorite.setImagenUrl(imagenUrl);
        favorite.setContenidoId(contenidoId);
        return favorite;
    }

    public static Favorite createShowFavorite(User user, String titulo, boolean isSeries, String descripcion, String imagenUrl, String contenidoId) {
        Favorite favorite = new Favorite();
        favorite.setUser(user);
        favorite.setTitulo(titulo);
        favorite.setLibro("");
        favorite.setAutor("");
        if (isSeries) {
            favorite.setSerie(titulo);
            favorite.setPelicula("");
        } else {
            favorite.setSerie("");
            favorite.setPelicula(titulo);
        }
        favorite.setDescripcion(descripcion);
        favorite.setImagenUrl(imagenUrl);
        favorite.setContenidoId(contenidoId);
        return favorite;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getLibro() {
        return libro;
    }

    public void setLibro(String libro) {
        this.libro = libro;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public String getSerie() {
        return serie;
    }

    public void setSerie(String serie) {
        this.serie = serie;
    }

    public String getPelicula() {
        return pelicula;
    }

    public void setPelicula(String pelicula) {
        this.pelicula = pelicula;
    }

    public String getContenidoId() {
        return contenidoId;
    }

    public void setContenidoId(String contenidoId) {
        this.contenidoId = contenidoId;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public LocalDateTime getFechaAgregado() {
        return fechaAgregado;
    }

    public void setFechaAgregado(LocalDateTime fechaAgregado) {
        this.fechaAgregado = fechaAgregado;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }

    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }

}
