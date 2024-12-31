package com.version.nutrition.model;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.time.LocalTime;

@Node
public class PlanDieta {
    @Id
    @GeneratedValue
    private Long id;

    private String nombre;
    private String descripcion;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String objetivo; // PERDIDA_PESO, GANANCIA_MASA, MANTENIMIENTO, etc.
    
    // Valores nutricionales objetivo diarios
    private int caloriasObjetivo;
    private double proteinasObjetivo;
    private double carbohidratosObjetivo;
    private double grasasObjetivo;

    // Restricciones y preferencias
    private boolean esVegetariano;
    private boolean esVegano;
    private boolean libreGluten;
    private String restriccionesAdicionales;

    // Comidas del día
    @Relationship(type = "TIENE_COMIDA")
    private List<Comida> comida = new ArrayList<>();

    @Relationship(type = "TIENE_COMIDA")
    private List<ComidaDiaria> comidas = new ArrayList<>();

    // Relación con alimentos y sus cantidades
    @Relationship(type = "INCLUDES")
    private List<AlimentoPorcion> alimentosPorciones;

    // Relación con el nutricionista que lo creó
    @Relationship(type = "CREATED_BY", direction = Relationship.Direction.INCOMING)
    private Nutricionista nutricionista;

    // Relación con el paciente asignado
    @Relationship(type = "ASSIGNED_TO")
    private Paciente paciente;

    // Estados de progreso
    private boolean activo;
    private String estado; // EN_CURSO, COMPLETADO, SUSPENDIDO
    private String observaciones;

    @Node
    public static class Comida {
        @Id @GeneratedValue
        private Long id;
        private String tipo; // DESAYUNO, ALMUERZO, CENA, MERIENDA
        private LocalTime hora;
        private int caloriasTotales;

        @Relationship(type = "INCLUYE")
        private List<AlimentoPorcion> alimentos = new ArrayList<>();
    }

    // Constructor básico
    public PlanDieta() {
    }

    // Constructor con parámetros esenciales
    public PlanDieta(String nombre, String descripcion, LocalDate fechaInicio, LocalDate fechaFin, 
                     String objetivo, int caloriasObjetivo) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.objetivo = objetivo;
        this.caloriasObjetivo = caloriasObjetivo;
        this.activo = true;
        this.estado = "EN_CURSO";
    }

    // Métodos de utilidad
    public double calcularCaloriasTotales() {
        double totalCalorias = 0;
        
        // Sumar calorías de alimentosPorciones
        if (alimentosPorciones != null) {
            totalCalorias += alimentosPorciones.stream()
                .filter(ap -> ap != null && ap.getAlimento() != null)
                .mapToDouble(ap -> ap.getAlimento().getCalorias() * ap.getCantidad())
                .sum();
        }
        
        // Sumar calorías de comidas
        if (comidas != null) {
            totalCalorias += comidas.stream()
                .filter(c -> c != null)
                .mapToDouble(ComidaDiaria::calcularCaloriasTotales)
                .sum();
        }
        
        return totalCalorias;
    }

    public boolean validarObjetivoCalorico() {
        return Math.abs(calcularCaloriasTotales() - caloriasObjetivo) <= 100;
    }

    // Getters y Setters
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }

    public String getObjetivo() {
        return objetivo;
    }

    public void setObjetivo(String objetivo) {
        this.objetivo = objetivo;
    }

    public int getCaloriasObjetivo() {
        return caloriasObjetivo;
    }

    public void setCaloriasObjetivo(int caloriasObjetivo) {
        this.caloriasObjetivo = caloriasObjetivo;
    }

    public double getProteinasObjetivo() {
        return proteinasObjetivo;
    }

    public void setProteinasObjetivo(double proteinasObjetivo) {
        this.proteinasObjetivo = proteinasObjetivo;
    }

    public double getCarbohidratosObjetivo() {
        return carbohidratosObjetivo;
    }

    public void setCarbohidratosObjetivo(double carbohidratosObjetivo) {
        this.carbohidratosObjetivo = carbohidratosObjetivo;
    }

    public double getGrasasObjetivo() {
        return grasasObjetivo;
    }

    public void setGrasasObjetivo(double grasasObjetivo) {
        this.grasasObjetivo = grasasObjetivo;
    }

    public boolean isEsVegetariano() {
        return esVegetariano;
    }

    public void setEsVegetariano(boolean esVegetariano) {
        this.esVegetariano = esVegetariano;
    }

    public boolean isEsVegano() {
        return esVegano;
    }

    public void setEsVegano(boolean esVegano) {
        this.esVegano = esVegano;
    }

    public boolean isLibreGluten() {
        return libreGluten;
    }

    public void setLibreGluten(boolean libreGluten) {
        this.libreGluten = libreGluten;
    }

    public String getRestriccionesAdicionales() {
        return restriccionesAdicionales;
    }

    public void setRestriccionesAdicionales(String restriccionesAdicionales) {
        this.restriccionesAdicionales = restriccionesAdicionales;
    }

    public List<Comida> getComida() {
        return comida;
    }

    public void setComida(List<Comida> comida) {
        this.comida = comida;
    }

    public List<AlimentoPorcion> getAlimentosPorciones() {
        return alimentosPorciones;
    }

    public void setAlimentosPorciones(List<AlimentoPorcion> alimentosPorciones) {
        this.alimentosPorciones = alimentosPorciones;
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

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
    public Long getId() {
        return id;
    }
    public List<ComidaDiaria> getComidas() {
        return comidas;
    }
    public void setComidas(List<ComidaDiaria> comidas) {
        this.comidas = comidas;
    }
}