package com.version.nutrition.dto;

import java.time.LocalDate;

public class PacienteFiltro {
    private String estadoNutricional;
    private String estadoSeguimiento;
    private Double imcMin;
    private Double imcMax;
    private String objetivo;
    private String nombre;
    private String apellido;
    private LocalDate fechaUltimaConsultaDesde;
    private LocalDate fechaUltimaConsultaHasta;
    private Boolean activo;

    // Constructor por defecto
    public PacienteFiltro() {}

    // Constructor con parámetros principales
    public PacienteFiltro(String estadoNutricional, String estadoSeguimiento, Double imcMin, Double imcMax) {
        this.estadoNutricional = estadoNutricional;
        this.estadoSeguimiento = estadoSeguimiento;
        this.imcMin = imcMin;
        this.imcMax = imcMax;
    }

    // Getters y Setters
    public String getEstadoNutricional() {
        return estadoNutricional;
    }

    public void setEstadoNutricional(String estadoNutricional) {
        this.estadoNutricional = estadoNutricional;
    }

    public String getEstadoSeguimiento() {
        return estadoSeguimiento;
    }

    public void setEstadoSeguimiento(String estadoSeguimiento) {
        this.estadoSeguimiento = estadoSeguimiento;
    }

    public Double getImcMin() {
        return imcMin;
    }

    public void setImcMin(Double imcMin) {
        this.imcMin = imcMin;
    }

    public Double getImcMax() {
        return imcMax;
    }

    public void setImcMax(Double imcMax) {
        this.imcMax = imcMax;
    }

    public String getObjetivo() {
        return objetivo;
    }

    public void setObjetivo(String objetivo) {
        this.objetivo = objetivo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public LocalDate getFechaUltimaConsultaDesde() {
        return fechaUltimaConsultaDesde;
    }

    public void setFechaUltimaConsultaDesde(LocalDate fechaUltimaConsultaDesde) {
        this.fechaUltimaConsultaDesde = fechaUltimaConsultaDesde;
    }

    public LocalDate getFechaUltimaConsultaHasta() {
        return fechaUltimaConsultaHasta;
    }

    public void setFechaUltimaConsultaHasta(LocalDate fechaUltimaConsultaHasta) {
        this.fechaUltimaConsultaHasta = fechaUltimaConsultaHasta;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    // Métodos de utilidad
    public boolean tieneValores() {
        return estadoNutricional != null || estadoSeguimiento != null || 
               imcMin != null || imcMax != null || objetivo != null ||
               nombre != null || apellido != null ||
               fechaUltimaConsultaDesde != null || fechaUltimaConsultaHasta != null ||
               activo != null;
    }

    public void limpiar() {
        this.estadoNutricional = null;
        this.estadoSeguimiento = null;
        this.imcMin = null;
        this.imcMax = null;
        this.objetivo = null;
        this.nombre = null;
        this.apellido = null;
        this.fechaUltimaConsultaDesde = null;
        this.fechaUltimaConsultaHasta = null;
        this.activo = null;
    }
}
