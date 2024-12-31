package com.version.nutrition.model;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.time.LocalDateTime;

@Node
public class RespuestaTicket {
    @Id @GeneratedValue
    private Long id;
    
    private String contenido;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaModificacion;
    private boolean esRespuestaAutomatica;
    private String tipo; // PUBLICA, INTERNA, AUTOMATICA
    private String estado; // PENDIENTE, LEIDA, RESPONDIDA
    private boolean destacada;

    // Adjuntos y archivos
    private String archivoAdjunto;
    private String tipoArchivo;

    // Relaciones
    @Relationship(type = "RESPONDIDO_POR")
    private Usuario autor;

    @Relationship(type = "PERTENECE_A")
    private Ticket ticket;

    // Constructor por defecto
    public RespuestaTicket() {
        this.fechaCreacion = LocalDateTime.now();
        this.fechaModificacion = LocalDateTime.now();
        this.estado = "PENDIENTE";
        this.esRespuestaAutomatica = false;
        this.destacada = false;
    }

    // Constructor con parámetros básicos
    public RespuestaTicket(String contenido, Usuario autor, Ticket ticket) {
        this();
        this.contenido = contenido;
        this.autor = autor;
        this.ticket = ticket;
    }

    // Métodos de utilidad
    public void marcarComoLeida() {
        this.estado = "LEIDA";
        this.fechaModificacion = LocalDateTime.now();
    }

    public void marcarComoRespondida() {
        this.estado = "RESPONDIDA";
        this.fechaModificacion = LocalDateTime.now();
    }

    public void toggleDestacada() {
        this.destacada = !this.destacada;
        this.fechaModificacion = LocalDateTime.now();
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getContenido() { return contenido; }
    public void setContenido(String contenido) { 
        this.contenido = contenido;
        this.fechaModificacion = LocalDateTime.now();
    }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public LocalDateTime getFechaModificacion() { return fechaModificacion; }
    public void setFechaModificacion(LocalDateTime fechaModificacion) { this.fechaModificacion = fechaModificacion; }

    public boolean isEsRespuestaAutomatica() { return esRespuestaAutomatica; }
    public void setEsRespuestaAutomatica(boolean esRespuestaAutomatica) { this.esRespuestaAutomatica = esRespuestaAutomatica; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { 
        this.estado = estado;
        this.fechaModificacion = LocalDateTime.now();
    }

    public boolean isDestacada() { return destacada; }
    public void setDestacada(boolean destacada) { this.destacada = destacada; }

    public String getArchivoAdjunto() { return archivoAdjunto; }
    public void setArchivoAdjunto(String archivoAdjunto) { this.archivoAdjunto = archivoAdjunto; }

    public String getTipoArchivo() { return tipoArchivo; }
    public void setTipoArchivo(String tipoArchivo) { this.tipoArchivo = tipoArchivo; }

    public Usuario getAutor() { return autor; }
    public void setAutor(Usuario autor) { this.autor = autor; }

    public Ticket getTicket() { return ticket; }
    public void setTicket(Ticket ticket) { this.ticket = ticket; }
}
