package com.version.nutrition.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

public class TicketDTO {
    private Long id;
    
    @NotBlank(message = "El título es obligatorio")
    @Size(min = 5, max = 100, message = "El título debe tener entre 5 y 100 caracteres")
    private String titulo;
    
    @NotBlank(message = "La descripción es obligatoria")
    @Size(min = 10, max = 1000, message = "La descripción debe tener entre 10 y 1000 caracteres")
    private String descripcion;
    
    private String estado;
    private String prioridad;
    
    @NotBlank(message = "La categoría es obligatoria")
    private String categoria;
    
    private LocalDateTime fechaCreacion;
    private String numeroReferencia;
    private List<String> etiquetas;
    
    @NotNull(message = "El ID del paciente es obligatorio")
    private Long pacienteId;
    
    private Long nutricionistaId;
    private Long citaRelacionadaId;
    private String respuestaAutomatica;

    // Constructor por defecto
    public TicketDTO() {}

    // Constructor con campos principales
    public TicketDTO(String titulo, String descripcion, String categoria, Long pacienteId) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.categoria = categoria;
        this.pacienteId = pacienteId;
    }

    // Getters y setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getPrioridad() { return prioridad; }
    public void setPrioridad(String prioridad) { this.prioridad = prioridad; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public String getNumeroReferencia() { return numeroReferencia; }
    public void setNumeroReferencia(String numeroReferencia) { this.numeroReferencia = numeroReferencia; }

    public List<String> getEtiquetas() { return etiquetas; }
    public void setEtiquetas(List<String> etiquetas) { this.etiquetas = etiquetas; }

    public Long getPacienteId() { return pacienteId; }
    public void setPacienteId(Long pacienteId) { this.pacienteId = pacienteId; }

    public Long getNutricionistaId() { return nutricionistaId; }
    public void setNutricionistaId(Long nutricionistaId) { this.nutricionistaId = nutricionistaId; }

    public Long getCitaRelacionadaId() { return citaRelacionadaId; }
    public void setCitaRelacionadaId(Long citaRelacionadaId) { this.citaRelacionadaId = citaRelacionadaId; }

    public String getRespuestaAutomatica() { return respuestaAutomatica; }
    public void setRespuestaAutomatica(String respuestaAutomatica) { this.respuestaAutomatica = respuestaAutomatica; }
}
