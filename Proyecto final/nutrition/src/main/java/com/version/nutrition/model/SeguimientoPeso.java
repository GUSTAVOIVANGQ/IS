package com.version.nutrition.model;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.time.LocalDateTime;

@Node
public class SeguimientoPeso {
    @Id
    @GeneratedValue
    private Long id;

    private LocalDateTime fechaRegistro;
    private double peso;
    private double imc;
    private double porcentajeGrasaCorporal;
    private double circunferenciaCintura;
    private double circunferenciaCadera;
    private double masaMuscular;
    private String observaciones;

    // Métricas adicionales
    private double presionArterialSistolica;
    private double presionArterialDiastolica;
    private double frecuenciaCardiaca;
    private int nivelActividad; // 1-5, donde 5 es muy activo
    private int calidadSueño; // 1-5, donde 5 es excelente
    private String estadoAnimo;

    @Relationship(type = "PERTENECE_A")
    private Paciente paciente;

    // Constructores
    public SeguimientoPeso() {
        this.fechaRegistro = LocalDateTime.now();
    }

    public SeguimientoPeso(Paciente paciente, double peso) {
        this();
        this.paciente = paciente;
        this.peso = peso;
    }

    // Métodos de utilidad
    public double calcularIMC(double altura) {
        this.imc = peso / (altura * altura);
        return this.imc;
    }

    public double calcularIndiceCircunferencia() {
        return circunferenciaCintura / circunferenciaCadera;
    }

    public boolean esRegistroSaludable() {
        return imc >= 18.5 && imc <= 24.9 &&
               calcularIndiceCircunferencia() <= 0.85 &&
               presionArterialSistolica <= 120 &&
               presionArterialDiastolica <= 80;
    }

    public String obtenerClasificacionIMC() {
        if (imc < 18.5) return "Bajo peso";
        if (imc < 25) return "Normal";
        if (imc < 30) return "Sobrepeso";
        return "Obesidad";
    }

    // Getters y Setters
    public double getImc() {
        return imc;
    }

    public void setImc(double imc) {
        this.imc = imc;
    }

    public double getPorcentajeGrasaCorporal() {
        return porcentajeGrasaCorporal;
    }

    public void setPorcentajeGrasaCorporal(double porcentajeGrasaCorporal) {
        this.porcentajeGrasaCorporal = porcentajeGrasaCorporal;
    }

    public double getCircunferenciaCintura() {
        return circunferenciaCintura;
    }

    public void setCircunferenciaCintura(double circunferenciaCintura) {
        this.circunferenciaCintura = circunferenciaCintura;
    }

    public double getCircunferenciaCadera() {
        return circunferenciaCadera;
    }

    public void setCircunferenciaCadera(double circunferenciaCadera) {
        this.circunferenciaCadera = circunferenciaCadera;
    }

    public double getMasaMuscular() {
        return masaMuscular;
    }

    public void setMasaMuscular(double masaMuscular) {
        this.masaMuscular = masaMuscular;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public double getPresionArterialSistolica() {
        return presionArterialSistolica;
    }

    public void setPresionArterialSistolica(double presionArterialSistolica) {
        this.presionArterialSistolica = presionArterialSistolica;
    }

    public double getPresionArterialDiastolica() {
        return presionArterialDiastolica;
    }

    public void setPresionArterialDiastolica(double presionArterialDiastolica) {
        this.presionArterialDiastolica = presionArterialDiastolica;
    }

    public double getFrecuenciaCardiaca() {
        return frecuenciaCardiaca;
    }

    public void setFrecuenciaCardiaca(double frecuenciaCardiaca) {
        this.frecuenciaCardiaca = frecuenciaCardiaca;
    }

    public int getNivelActividad() {
        return nivelActividad;
    }

    public void setNivelActividad(int nivelActividad) {
        this.nivelActividad = nivelActividad;
    }

    public int getCalidadSueño() {
        return calidadSueño;
    }

    public void setCalidadSueño(int calidadSueño) {
        this.calidadSueño = calidadSueño;
    }

    public String getEstadoAnimo() {
        return estadoAnimo;
    }

    public void setEstadoAnimo(String estadoAnimo) {
        this.estadoAnimo = estadoAnimo;
    }

    public Paciente getPaciente() {
        return paciente;
    }

    public void setPaciente(Paciente paciente) {
        this.paciente = paciente;
    }
    public Long getId() {
        return id;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }
    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }
    public double getPeso() {
        return peso;
    }
    public void setPeso(double peso) {
        this.peso = peso;
    }
    // ... otros getters y setters
}
