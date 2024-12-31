package com.version.nutrition.model;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Node
public class Ticket {
    @Id @GeneratedValue
    private Long id;

    private String titulo;
    private String descripcion;
    private String estado; // ABIERTO, EN_PROGRESO, RESUELTO, CERRADO
    private String prioridad; // ALTA, MEDIA, BAJA
    private String categoria; // CONSULTA_DIETA, PROBLEMA_TECNICO, AJUSTE_PLAN, CONSULTA_GENERAL
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    private LocalDateTime fechaCierre;
    private String numeroReferencia; // Para seguimiento, ej: TKT-2024-0001

    // Etiquetas para categorización y búsqueda
    private List<String> etiquetas;

    // Relaciones
    @Relationship(type = "CREADO_POR")
    private Paciente paciente;

    @Relationship(type = "ASIGNADO_A")
    private Nutricionista nutricionista;

    @Relationship(type = "TIENE_RESPUESTA")
    private List<RespuestaTicket> respuestas = new ArrayList<>();

    @Relationship(type = "RELACIONADO_CON")
    private Cita citaRelacionada;

    // Constructor
    public Ticket() {
        this.fechaCreacion = LocalDateTime.now();
        this.fechaActualizacion = LocalDateTime.now();
        this.estado = "ABIERTO";
        this.etiquetas = new ArrayList<>();
    }

    // Métodos de negocio
    public void agregarRespuesta(RespuestaTicket respuesta) {
        if (this.respuestas == null) {
            this.respuestas = new ArrayList<>();
        }
        this.respuestas.add(respuesta);
        this.fechaActualizacion = LocalDateTime.now();
    }

    public void cambiarEstado(String nuevoEstado) {
        this.estado = nuevoEstado;
        this.fechaActualizacion = LocalDateTime.now();
        
        if ("RESUELTO".equals(nuevoEstado) || "CERRADO".equals(nuevoEstado)) {
            this.fechaCierre = LocalDateTime.now();
        }
    }

    public void asignarNutricionista(Nutricionista nutricionista) {
        this.nutricionista = nutricionista;
        this.fechaActualizacion = LocalDateTime.now();
        if ("ABIERTO".equals(this.estado)) {
            this.estado = "EN_PROGRESO";
        }
    }

    public boolean estaResuelto() {
        return "RESUELTO".equals(this.estado) || "CERRADO".equals(this.estado);
    }

    public long getTiempoResolucion() {
        if (fechaCierre == null || fechaCreacion == null) return 0;
        return java.time.Duration.between(fechaCreacion, fechaCierre).toHours();
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { 
        this.estado = estado;
        this.fechaActualizacion = LocalDateTime.now();
    }

    public String getPrioridad() { return prioridad; }
    public void setPrioridad(String prioridad) { this.prioridad = prioridad; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }

    public LocalDateTime getFechaCierre() { return fechaCierre; }
    public void setFechaCierre(LocalDateTime fechaCierre) { this.fechaCierre = fechaCierre; }

    public String getNumeroReferencia() { return numeroReferencia; }
    public void setNumeroReferencia(String numeroReferencia) { this.numeroReferencia = numeroReferencia; }

    public List<String> getEtiquetas() { return etiquetas; }
    public void setEtiquetas(List<String> etiquetas) { this.etiquetas = etiquetas; }

    public Paciente getPaciente() { return paciente; }
    public void setPaciente(Paciente paciente) { this.paciente = paciente; }

    public Nutricionista getNutricionista() { return nutricionista; }
    public void setNutricionista(Nutricionista nutricionista) { this.nutricionista = nutricionista; }

    public List<RespuestaTicket> getRespuestas() { return respuestas; }
    public void setRespuestas(List<RespuestaTicket> respuestas) { this.respuestas = respuestas; }

    public Cita getCitaRelacionada() { return citaRelacionada; }
    public void setCitaRelacionada(Cita citaRelacionada) { this.citaRelacionada = citaRelacionada; }

    public void generarDesdeCita(Cita cita) {
        this.citaRelacionada = cita;
        this.titulo = "Consulta - " + cita.getTipoCita();
        this.categoria = "CONSULTA_CITA";
        this.descripcion = "Cita programada para: " + cita.getFecha() + 
                         "\nMotivo: " + cita.getMotivoConsulta();
        this.prioridad = "MEDIA";
        this.setNutricionista(cita.getNutricionista());
    }
    private String respuestaAutomatica;
    public void setRespuestaAutomatica(String respuestaAutomatica) {
        this.respuestaAutomatica = respuestaAutomatica;
    }
    public String getRespuestaAutomatica() {
        return respuestaAutomatica;
    }
}
