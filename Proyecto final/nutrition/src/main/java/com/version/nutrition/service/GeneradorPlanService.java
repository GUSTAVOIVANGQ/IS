package com.version.nutrition.service;

import com.version.nutrition.model.*;
import com.version.nutrition.repository.AlimentoRepository;
import com.version.nutrition.repository.PlanDietaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@Service
@Transactional
public class GeneradorPlanService {

    @Autowired
    private AlimentoRepository alimentoRepository;

    @Autowired
    private PlanDietaRepository planDietaRepository;

    public PlanDieta generarPlanPersonalizado(Paciente paciente, Map<String, Object> criterios) {
        // Calcular requerimientos calóricos
        double caloriasObjetivo = calcularCaloriasObjetivo(paciente);
        
        // Crear plan base
        PlanDieta plan = new PlanDieta(
            "Plan personalizado para " + paciente.getNombre(),
            "Plan adaptado a objetivos y preferencias del paciente",
            LocalDate.now(),
            LocalDate.now().plusMonths(1),
            paciente.getObjetivoNutricional(),
            (int) caloriasObjetivo
        );

        // Distribuir calorías por comida
        Map<String, Double> distribucionCalorias = distribuirCaloriasPorComida(caloriasObjetivo);
        
        // Generar comidas diarias
        distribucionCalorias.forEach((tipoComida, calorias) -> {
            ComidaDiaria comida = generarComida(
                tipoComida,
                calorias.intValue(),
                paciente,
                criterios
            );
            plan.getComidas().add(comida);
        });

        return planDietaRepository.save(plan);
    }

    private ComidaDiaria generarComida(String tipoComida, int calorias, 
                                      Paciente paciente, Map<String, Object> criterios) {
        ComidaDiaria comida = new ComidaDiaria();
        comida.setTipoComida(tipoComida);
        comida.setCaloriasObjetivo(calorias);
        comida.setHoraRecomendada(obtenerHoraRecomendada(tipoComida));

        // Obtener alimentos compatibles
        List<Alimento> alimentosCompatibles = buscarAlimentosCompatibles(
            paciente,
            calorias,
            criterios
        );

        // Distribuir alimentos para cumplir objetivos nutricionales
        distribuirAlimentos(comida, alimentosCompatibles, calorias);

        return comida;
    }

    private double calcularCaloriasObjetivo(Paciente paciente) {
        double tmb = paciente.getTasaMetabolicaBasal();
        double factorActividad = obtenerFactorActividad(paciente.getActividadFisica());
        
        // Ajustar según objetivo
        double caloriasObjetivo = tmb * factorActividad;
        switch (paciente.getObjetivoNutricional().toUpperCase()) {
            case "PERDIDA_PESO" -> caloriasObjetivo *= 0.85; // Déficit del 15%
            case "GANANCIA_PESO" -> caloriasObjetivo *= 1.15; // Superávit del 15%
            default -> {} // Mantener peso actual
        };
        
        return caloriasObjetivo;
    }

    private Map<String, Double> distribuirCaloriasPorComida(double caloriasTotal) {
        Map<String, Double> distribucion = new HashMap<>();
        distribucion.put("DESAYUNO", caloriasTotal * 0.25);    // 25%
        distribucion.put("ALMUERZO", caloriasTotal * 0.35);    // 35%
        distribucion.put("MERIENDA", caloriasTotal * 0.15);    // 15%
        distribucion.put("CENA", caloriasTotal * 0.25);        // 25%
        return distribucion;
    }

    private List<Alimento> buscarAlimentosCompatibles(Paciente paciente, 
                                                     int caloriasObjetivo,
                                                     Map<String, Object> criterios) {
        boolean esVegetariano = criterios.getOrDefault("vegetariano", false).toString().equals("true");
        boolean esVegano = criterios.getOrDefault("vegano", false).toString().equals("true");
        boolean sinGluten = criterios.getOrDefault("sinGluten", false).toString().equals("true");

        return alimentoRepository.findByRestricciones(
            esVegetariano,
            esVegano,
            sinGluten
        );
    }

    private void distribuirAlimentos(ComidaDiaria comida, 
                                   List<Alimento> alimentosDisponibles,
                                   int caloriasObjetivo) {
        double caloriasActuales = 0;
        for (Alimento alimento : alimentosDisponibles) {
            if (caloriasActuales >= caloriasObjetivo) break;

            // Calcular porción para no exceder calorías objetivo
            double caloriasFaltantes = caloriasObjetivo - caloriasActuales;
            double porcion = calcularPorcionOptima(alimento, caloriasFaltantes);

            if (porcion > 0) {
                comida.agregarAlimento(alimento, porcion, alimento.getUnidadMedida());
                caloriasActuales += alimento.getCalorias() * porcion;
            }
        }
    }

    private LocalTime obtenerHoraRecomendada(String tipoComida) {
        return switch (tipoComida.toUpperCase()) {
            case "DESAYUNO" -> LocalTime.of(8, 0);
            case "ALMUERZO" -> LocalTime.of(13, 0);
            case "MERIENDA" -> LocalTime.of(16, 30);
            case "CENA" -> LocalTime.of(20, 0);
            default -> LocalTime.NOON;
        };
    }

    private double obtenerFactorActividad(String nivelActividad) {
        return switch (nivelActividad.toUpperCase()) {
            case "SEDENTARIO" -> 1.2;
            case "LIGERO" -> 1.375;
            case "MODERADO" -> 1.55;
            case "INTENSO" -> 1.725;
            case "MUY_INTENSO" -> 1.9;
            default -> 1.2;
        };
    }

    private double calcularPorcionOptima(Alimento alimento, double caloriasFaltantes) {
        if (alimento.getCalorias() <= 0) return 0;
        return Math.min(
            caloriasFaltantes / alimento.getCalorias(),
            alimento.getPorcion() * 2  // Máximo el doble de la porción estándar
        );
    }
}
