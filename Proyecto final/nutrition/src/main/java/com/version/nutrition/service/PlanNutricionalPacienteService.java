package com.version.nutrition.service;

import com.version.nutrition.model.*;
import com.version.nutrition.repository.PlanDietaRepository;
import com.version.nutrition.repository.AlimentoRepository;
import com.version.nutrition.repository.PacienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@Service
@Transactional
public class PlanNutricionalPacienteService {

    private static final Logger logger = LoggerFactory.getLogger(PlanNutricionalPacienteService.class);

    @Autowired
    private PlanDietaRepository planDietaRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private AlimentoRepository alimentoRepository;

    // Obtener el plan actual del paciente
    public PlanDieta obtenerPlanActual(Long pacienteId) {
        if (pacienteId == null) {
            logger.warn("ID de paciente nulo al intentar obtener plan actual");
            return null;
        }

        try {
            return planDietaRepository.findPlanesPacienteActivo(pacienteId)
                .stream()
                .filter(PlanDieta::isActivo)
                .findFirst()
                .orElse(null);
        } catch (Exception e) {
            logger.error("Error al obtener plan actual para paciente {}: {}", pacienteId, e.getMessage());
            return null;
        }
    }

    // Obtener comidas de un día específico
    public List<ComidaDiaria> obtenerComidasDelDia(Long planId, LocalDate fecha) {
        PlanDieta plan = planDietaRepository.findById(planId)
            .orElseThrow(() -> new RuntimeException("Plan no encontrado"));
            
        return plan.getComidas().stream()
            .filter(comida -> !comida.getAlimentos().isEmpty())
            .collect(Collectors.toList());
    }

    // Obtener alimentos recomendados según el perfil del paciente
    public List<Alimento> obtenerAlimentosRecomendados(Long pacienteId) {
        Paciente paciente = pacienteRepository.findById(pacienteId)
            .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));
            
        // Obtener restricciones y objetivos del paciente
        String objetivo = paciente.getObjetivoNutricional();
        double imc = paciente.getIndiceMasaCorporal();
        
        // Definir criterios de búsqueda según el objetivo y el IMC
        double minProteinas = 0.0;
        double maxCalorias = Double.MAX_VALUE;
        boolean considerarVegetariano = false;
        String categoria = null;

        if (objetivo != null) {
            switch (objetivo.toUpperCase()) {
                case "PERDIDA_PESO":
                    maxCalorias = 300; // Por porción
                    break;
                case "GANANCIA_PESO":
                    minProteinas = 15; // Mínimo 15g de proteína por porción
                    break;
                case "MANTENIMIENTO":
                    maxCalorias = 500;
                    break;
            }
        }

        if (imc > 25) { // Sobrepeso
            maxCalorias = Math.min(maxCalorias, 250);
            categoria = "VERDURAS";
        } else if (imc < 18.5) { // Bajo peso
            minProteinas = Math.max(minProteinas, 10);
            categoria = "PROTEINAS";
        }

        return alimentoRepository.recomendarAlimentosParaObjetivo(
            categoria,
            minProteinas,
            maxCalorias,
            considerarVegetariano
        );
    }

    // Registrar consumo de alimentos
    public void registrarConsumoAlimento(Long pacienteId, Long alimentoId, 
                                       double cantidad, LocalDate fecha) {
        PlanDieta planActual = obtenerPlanActual(pacienteId);
        if (planActual == null) {
            throw new RuntimeException("No hay un plan activo");
        }

        Alimento alimento = alimentoRepository.findById(alimentoId)
            .orElseThrow(() -> new RuntimeException("Alimento no encontrado"));

        // Crear el registro de consumo
        AlimentoPorcion porcion = new AlimentoPorcion(alimento, cantidad, alimento.getUnidadMedida());

        // Asignar a la comida correspondiente según la hora del día
        asignarAComidaCorrespondiente(planActual, porcion);

        // Guardar cambios
        planDietaRepository.save(planActual);
    }

    // Obtener estadísticas del plan
    public Map<String, Object> obtenerEstadisticasPlan(Long planId) {
        PlanDieta plan = planDietaRepository.findById(planId)
            .orElseThrow(() -> new RuntimeException("Plan no encontrado"));

        Map<String, Object> estadisticas = new HashMap<>();
        
        // Calcular totales y promedios
        double caloriasActuales = plan.calcularCaloriasTotales();
        double porcentajeCompletado = (caloriasActuales / plan.getCaloriasObjetivo()) * 100;
        
        // Calcular distribución de macronutrientes
        double proteinasTotal = plan.getComidas().stream()
            .flatMap(c -> c.getAlimentos().stream())
            .mapToDouble(a -> a.getAlimento().getProteinas() * a.getCantidad())
            .sum();
            
        double carbohidratosTotal = plan.getComidas().stream()
            .flatMap(c -> c.getAlimentos().stream())
            .mapToDouble(a -> a.getAlimento().getCarbohidratos() * a.getCantidad())
            .sum();
            
        double grasasTotal = plan.getComidas().stream()
            .flatMap(c -> c.getAlimentos().stream())
            .mapToDouble(a -> a.getAlimento().getGrasas() * a.getCantidad())
            .sum();

        // Agregar datos al mapa de estadísticas
        estadisticas.put("caloriasActuales", caloriasActuales);
        estadisticas.put("caloriasObjetivo", plan.getCaloriasObjetivo());
        estadisticas.put("porcentajeCompletado", porcentajeCompletado);
        estadisticas.put("proteinasTotal", proteinasTotal);
        estadisticas.put("carbohidratosTotal", carbohidratosTotal);
        estadisticas.put("grasasTotal", grasasTotal);
        estadisticas.put("cumpleObjetivo", plan.validarObjetivoCalorico());

        return estadisticas;
    }

    // Método auxiliar para asignar alimentos a comidas
    private void asignarAComidaCorrespondiente(PlanDieta plan, AlimentoPorcion porcion) {
        int hora = java.time.LocalTime.now().getHour();
        String tipoComida;

        if (hora >= 6 && hora < 11) {
            tipoComida = "DESAYUNO";
        } else if (hora >= 11 && hora < 15) {
            tipoComida = "ALMUERZO";
        } else if (hora >= 15 && hora < 18) {
            tipoComida = "MERIENDA";
        } else {
            tipoComida = "CENA";
        }

        // Buscar la comida correspondiente o crear una nueva
        ComidaDiaria comida = plan.getComidas().stream()
            .filter(c -> c.getTipoComida().equals(tipoComida))
            .findFirst()
            .orElseGet(() -> {
                ComidaDiaria nuevaComida = new ComidaDiaria();
                nuevaComida.setTipoComida(tipoComida);
                plan.getComidas().add(nuevaComida);
                return nuevaComida;
            });

        comida.getAlimentos().add(porcion);
    }
}
