package com.version.nutrition.model;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Node
public class ComidaDiaria {
    @Id
    @GeneratedValue
    private Long id;

    private String tipoComida; // DESAYUNO, ALMUERZO, CENA, MERIENDA
    private LocalTime horaRecomendada;
    private int caloriasObjetivo;
    private double proteinasObjetivo;
    private double carbohidratosObjetivo;
    private double grasasObjetivo;

    @Relationship(type = "CONTIENE_ALIMENTO")
    private List<AlimentoPorcion> alimentos = new ArrayList<>();

    // Constructores
    public ComidaDiaria() {}

    public ComidaDiaria(String tipoComida, LocalTime horaRecomendada, int caloriasObjetivo) {
        this.tipoComida = tipoComida;
        this.horaRecomendada = horaRecomendada;
        this.caloriasObjetivo = caloriasObjetivo;
    }

    // Métodos de utilidad
    public void agregarAlimento(Alimento alimento, double cantidad, String unidadMedida) {
        AlimentoPorcion porcion = new AlimentoPorcion(alimento, cantidad, unidadMedida);
        alimentos.add(porcion);
    }

    public double calcularCaloriasTotales() {
        return alimentos.stream()
                .mapToDouble(ap -> ap.getAlimento().getCalorias() * ap.getCantidad())
                .sum();
    }

    public boolean cumpleObjetivosCaloricos() {
        double totalCalorias = calcularCaloriasTotales();
        return Math.abs(totalCalorias - caloriasObjetivo) <= 50; // Margen de 50 calorías
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTipoComida() { return tipoComida; }
    public void setTipoComida(String tipoComida) { this.tipoComida = tipoComida; }
    public LocalTime getHoraRecomendada() { return horaRecomendada; }
    public void setHoraRecomendada(LocalTime horaRecomendada) { this.horaRecomendada = horaRecomendada; }
    public int getCaloriasObjetivo() { return caloriasObjetivo; }
    public void setCaloriasObjetivo(int caloriasObjetivo) { this.caloriasObjetivo = caloriasObjetivo; }
    public double getProteinasObjetivo() { return proteinasObjetivo; }
    public void setProteinasObjetivo(double proteinasObjetivo) { this.proteinasObjetivo = proteinasObjetivo; }
    public double getCarbohidratosObjetivo() { return carbohidratosObjetivo; }
    public void setCarbohidratosObjetivo(double carbohidratosObjetivo) { this.carbohidratosObjetivo = carbohidratosObjetivo; }
    public double getGrasasObjetivo() { return grasasObjetivo; }
    public void setGrasasObjetivo(double grasasObjetivo) { this.grasasObjetivo = grasasObjetivo; }
    public List<AlimentoPorcion> getAlimentos() { return alimentos; }
    public void setAlimentos(List<AlimentoPorcion> alimentos) { this.alimentos = alimentos; }
}
