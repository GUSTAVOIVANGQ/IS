package com.version.nutrition.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Future;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

public class CitaDTO {
    private Long id;
    
    @NotNull(message = "La fecha es requerida")
    @Future(message = "La fecha debe ser futura")
    private LocalDateTime fecha;
    
    @NotNull(message = "El tipo de cita es requerido")
    private String tipoCita;
    
    @NotNull(message = "El motivo de consulta es requerido")
    @Size(min = 10, max = 500, message = "El motivo debe tener entre 10 y 500 caracteres")
    private String motivoConsulta;
    
    private boolean esVirtual;
    
    @NotNull(message = "El ID del nutricionista es requerido")
    private Long nutricionistaId;
    
    @NotNull(message = "El ID del paciente es requerido")
    private Long pacienteId;
    
    private String estado;
    private String notas;
    private Integer duracionMinutos;
    private Double costo;
    private String enlaceReunion;

    // Constructor por defecto
    public CitaDTO() {
    }

    // Constructor con campos principales
    public CitaDTO(LocalDateTime fecha, String tipoCita, String motivoConsulta, 
                  Long nutricionistaId, Long pacienteId) {
        this.fecha = fecha;
        this.tipoCita = tipoCita;
        this.motivoConsulta = motivoConsulta;
        this.nutricionistaId = nutricionistaId;
        this.pacienteId = pacienteId;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public String getTipoCita() {
        return tipoCita;
    }

    public void setTipoCita(String tipoCita) {
        this.tipoCita = tipoCita;
    }

    public String getMotivoConsulta() {
        return motivoConsulta;
    }

    public void setMotivoConsulta(String motivoConsulta) {
        this.motivoConsulta = motivoConsulta;
    }

    public boolean isEsVirtual() {
        return esVirtual;
    }

    public void setEsVirtual(boolean esVirtual) {
        this.esVirtual = esVirtual;
    }

    public Long getNutricionistaId() {
        return nutricionistaId;
    }

    public void setNutricionistaId(Long nutricionistaId) {
        this.nutricionistaId = nutricionistaId;
    }

    public Long getPacienteId() {
        return pacienteId;
    }

    public void setPacienteId(Long pacienteId) {
        this.pacienteId = pacienteId;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getNotas() {
        return notas;
    }

    public void setNotas(String notas) {
        this.notas = notas;
    }

    public Integer getDuracionMinutos() {
        return duracionMinutos;
    }

    public void setDuracionMinutos(Integer duracionMinutos) {
        this.duracionMinutos = duracionMinutos;
    }

    public Double getCosto() {
        return costo;
    }

    public void setCosto(Double costo) {
        this.costo = costo;
    }

    public String getEnlaceReunion() {
        return enlaceReunion;
    }

    public void setEnlaceReunion(String enlaceReunion) {
        this.enlaceReunion = enlaceReunion;
    }
}
