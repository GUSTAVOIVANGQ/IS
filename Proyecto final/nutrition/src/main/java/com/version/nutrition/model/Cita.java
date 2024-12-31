package com.version.nutrition.model;

import org.springframework.data.neo4j.core.schema.*;
import java.time.LocalDateTime;

@Node
public class Cita {
    @Id
    @GeneratedValue
    private Long id;

    private LocalDateTime fecha;
    private String estado;
    private String notas;

    @Relationship(type = "AGENDA_CON")
    private Nutricionista nutricionista;

    @Relationship(type = "PARA_PACIENTE")
    private Paciente paciente;

    // Nuevos atributos
    private int duracionMinutos = 30;
    private String tipoCita; // PRIMERA_VEZ, SEGUIMIENTO, CONTROL, EMERGENCIA
    private String motivoConsulta;
    private boolean esVirtual;
    private String enlaceReunion;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaModificacion;
    private double costo;
    private String estadoPago; // PENDIENTE, PAGADO, CANCELADO

    // Constructor por defecto
    public Cita() {
        this.fechaCreacion = LocalDateTime.now();
        this.estado = "PENDIENTE";
    }

    // Constructor con parámetros principales
    public Cita(LocalDateTime fecha, Nutricionista nutricionista, Paciente paciente, String tipoCita) {
        this();
        this.fecha = fecha;
        this.nutricionista = nutricionista;
        this.paciente = paciente;
        this.tipoCita = tipoCita;
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

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
        this.fechaModificacion = LocalDateTime.now();
    }

    public String getNotas() {
        return notas;
    }

    public void setNotas(String notas) {
        this.notas = notas;
    }

    public Nutricionista getNutricionista() {
        return nutricionista;
    }

    public void setNutricionista(Nutricionista nutricionista) {
        this.nutricionista = nutricionista;
    }

    public Paciente getPaciente() {
        return paciente;
    }

    public void setPaciente(Paciente paciente) {
        this.paciente = paciente;
    }

    public int getDuracionMinutos() {
        return duracionMinutos;
    }

    public void setDuracionMinutos(int duracionMinutos) {
        this.duracionMinutos = duracionMinutos;
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

    public String getEnlaceReunion() {
        return enlaceReunion;
    }

    public void setEnlaceReunion(String enlaceReunion) {
        this.enlaceReunion = enlaceReunion;
    }

    public double getCosto() {
        return costo;
    }

    public void setCosto(double costo) {
        this.costo = costo;
    }

    public String getEstadoPago() {
        return estadoPago;
    }

    public void setEstadoPago(String estadoPago) {
        this.estadoPago = estadoPago;
    }
    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }
    public void setFechaModificacion(LocalDateTime fechaModificacion) {
        this.fechaModificacion = fechaModificacion;
    }
    public LocalDateTime getFechaModificacion() {
        return fechaModificacion;
    }
    // Métodos de utilidad
    public boolean estaProxima() {
        return LocalDateTime.now().plusHours(24).isAfter(fecha);
    }

    public boolean puedeSerCancelada() {
        return LocalDateTime.now().plusHours(2).isBefore(fecha) 
            && !estado.equals("CANCELADA") 
            && !estado.equals("COMPLETADA");
    }

    public void confirmar() {
        this.estado = "CONFIRMADA";
        this.fechaModificacion = LocalDateTime.now();
    }

    public void cancelar() {
        if (puedeSerCancelada()) {
            this.estado = "CANCELADA";
            this.fechaModificacion = LocalDateTime.now();
        } else {
            throw new IllegalStateException("La cita no puede ser cancelada");
        }
    }

    public void completar() {
        if ("CONFIRMADA".equals(this.estado)) {
            this.estado = "COMPLETADA";
            this.fechaModificacion = LocalDateTime.now();
        } else {
            throw new IllegalStateException("La cita debe estar confirmada para ser completada");
        }
    }
}