package com.version.nutrition.model;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.List;

@Node
public class Alimento {
    @Id
    @GeneratedValue
    private Long id;

    private String nombre;
    private String categoria; // FRUTAS, VERDURAS, PROTEINAS, CEREALES, etc.
    private String descripcion;
    private String unidadMedida; // gramos, mililitros, unidad, etc.
    private double porcion; // cantidad por porción en la unidad de medida especificada

    // Información nutricional por porción
    private double calorias;
    private double proteinas;
    private double carbohidratos;
    private double grasas;
    private double fibra;
    private double azucares;
    private double sodio;
    private double colesterol;
    private double caloriasPorUnidad;
    // Información adicional
    private boolean esVegetariano;
    private boolean esVegano;
    private boolean libre_gluten;
    private String temporada; // En qué época del año está disponible
    private String alergenos; // Lista de alérgenos comunes

    @Relationship(type = "COMPATIBLE_CON", direction = Relationship.Direction.OUTGOING)
    private List<Alimento> alimentosCompatibles;

    // Constructor
    public Alimento() {
    }

    // Constructor con parámetros básicos
    public Alimento(String nombre, String categoria, double calorias) {
        this.nombre = nombre;
        this.categoria = categoria;
        this.calorias = calorias;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public double getCalorias() {
        return calorias;
    }

    public void setCalorias(double calorias) {
        this.calorias = calorias;
    }

    public double getProteinas() {
        return proteinas;
    }

    public void setProteinas(double proteinas) {
        this.proteinas = proteinas;
    }

    public double getCarbohidratos() {
        return carbohidratos;
    }

    public void setCarbohidratos(double carbohidratos) {
        this.carbohidratos = carbohidratos;
    }

    public double getGrasas() {
        return grasas;
    }

    public void setGrasas(double grasas) {
        this.grasas = grasas;
    }

    public double getFibra() {
        return fibra;
    }

    public void setFibra(double fibra) {
        this.fibra = fibra;
    }

    public double getAzucares() {
        return azucares;
    }

    public void setAzucares(double azucares) {
        this.azucares = azucares;
    }

    public double getSodio() {
        return sodio;
    }

    public void setSodio(double sodio) {
        this.sodio = sodio;
    }

    public double getColesterol() {
        return colesterol;
    }

    public void setColesterol(double colesterol) {
        this.colesterol = colesterol;
    }

    public double getCaloriasPorUnidad() {
        return caloriasPorUnidad;
    }

    public void setCaloriasPorUnidad(double caloriasPorUnidad) {
        this.caloriasPorUnidad = caloriasPorUnidad;
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

    public boolean isLibre_gluten() {
        return libre_gluten;
    }

    public void setLibre_gluten(boolean libre_gluten) {
        this.libre_gluten = libre_gluten;
    }

    public String getTemporada() {
        return temporada;
    }

    public void setTemporada(String temporada) {
        this.temporada = temporada;
    }

    public String getAlergenos() {
        return alergenos;
    }

    public void setAlergenos(String alergenos) {
        this.alergenos = alergenos;
    }

    public List<Alimento> getAlimentosCompatibles() {
        return alimentosCompatibles;
    }

    public void setAlimentosCompatibles(List<Alimento> alimentosCompatibles) {
        this.alimentosCompatibles = alimentosCompatibles;
    }
    public String getUnidadMedida() {
        return unidadMedida;
    }
    public void setUnidadMedida(String unidadMedida) {
        this.unidadMedida = unidadMedida;
    }
    public double getPorcion() {
        // Return a default portion size or implement the logic to get the portion size
        return 100.0; // Example default portion size
    }
}