package com.web.book.version.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchHistoryDTO {
    private Long id;
    private String username;
    private LocalDateTime fechaBusqueda;
    private String terminosBusqueda;
    private String libro;
    private String autor;
    private String serie;
    private String pelicula;
    private String tipo; // "LIBRO", "SERIE", "PELICULA"
    
    // Métodos helper para la vista
    public String getFechaBusquedaFormatted() {
        if (fechaBusqueda == null) return "";
        return fechaBusqueda.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }
    
    public String getResultadoBusqueda() {
        if (!libro.isEmpty()) {
            return libro + " por " + autor;
        } else if (!serie.isEmpty()) {
            return "Serie: " + serie;
        } else if (!pelicula.isEmpty()) {
            return "Película: " + pelicula;
        }
        return "Sin resultados";
    }
    
    public String getTipoContenido() {
        if (!libro.isEmpty()) return "LIBRO";
        if (!serie.isEmpty()) return "SERIE";
        if (!pelicula.isEmpty()) return "PELÍCULA";
        return "DESCONOCIDO";
    }
    
    public String getIconClass() {
        switch (getTipoContenido()) {
            case "LIBRO":
                return "bi bi-book";
            case "SERIE":
                return "bi bi-tv";
            case "PELÍCULA":
                return "bi bi-film";
            default:
                return "bi bi-question-circle";
        }
    }
    
    public String getStatusClass() {
        switch (getTipoContenido()) {
            case "LIBRO":
                return "badge bg-primary";
            case "SERIE":
                return "badge bg-success";
            case "PELÍCULA":
                return "badge bg-info";
            default:
                return "badge bg-secondary";
        }
    }
}
