package com.version.nutrition.model;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

@Node
public class AlimentoPorcion {
    @Id @GeneratedValue
    private Long id;
    
    @Relationship(type = "ES_ALIMENTO")
    private Alimento alimento;
    private double cantidad;
    private String unidadMedida;
    private String comidaDelDia; // DESAYUNO, ALMUERZO, CENA, MERIENDA
    private String notas;

    // Constructor vacío requerido por Spring Data
    public AlimentoPorcion() {}

    // Constructor con parámetros
    public AlimentoPorcion(Alimento alimento, double cantidad, String unidadMedida) {
        this.alimento = alimento;
        this.cantidad = cantidad;
        this.unidadMedida = unidadMedida;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Alimento getAlimento() { return alimento; }
    public void setAlimento(Alimento alimento) { this.alimento = alimento; }
    public double getCantidad() { return cantidad; }
    public void setCantidad(double cantidad) { this.cantidad = cantidad; }
    public String getUnidadMedida() { return unidadMedida; }
    public void setUnidadMedida(String unidadMedida) { this.unidadMedida = unidadMedida; }
    public String getComidaDelDia() { return comidaDelDia; }
    public void setComidaDelDia(String comidaDelDia) { this.comidaDelDia = comidaDelDia; }
    public String getNotas() { return notas; }
    public void setNotas(String notas) { this.notas = notas; }
}
