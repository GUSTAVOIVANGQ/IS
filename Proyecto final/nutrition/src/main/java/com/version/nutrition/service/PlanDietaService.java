package com.version.nutrition.service;

import com.version.nutrition.model.*;
import com.version.nutrition.repository.PlanDietaRepository;
import com.version.nutrition.repository.PacienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
@Transactional
public class PlanDietaService {
    
    @Autowired
    private Neo4jClient neo4jClient;
    
    @Autowired
    private PlanDietaRepository planDietaRepository;
    
    @Autowired
    private PacienteRepository pacienteRepository;
    
    @Autowired
    private GeneradorPlanService generadorPlanService;

    // Operaciones CRUD básicas
    public PlanDieta crearPlan(PlanDieta plan) {
        return planDietaRepository.save(plan);
    }

    public Optional<PlanDieta> buscarPorId(Long id) {
        return planDietaRepository.findById(id);
    }

    public List<PlanDieta> buscarPorObjetivo(String objetivo) {
        return planDietaRepository.findByObjetivo(objetivo);
    }

    // Generación de planes personalizados
    public PlanDieta generarPlanPersonalizado(Long pacienteId, Map<String, Object> criterios) {
        Paciente paciente = pacienteRepository.findById(pacienteId)
            .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));
        
        PlanDieta plan = generadorPlanService.generarPlanPersonalizado(paciente, criterios);
        vincularEntidades(plan.getId());
        
        return plan;
    }

    // Gestión de planes y alimentos
    public void agregarAlimentoAPlan(Long planId, Long alimentoId, double cantidad) {
        String query = """
            MATCH (p:PlanDieta), (a:Alimento)
            WHERE ID(p) = $planId AND ID(a) = $alimentoId
            CREATE (p)-[:INCLUDES {cantidad: $cantidad}]->(a)
        """;
        
        Map<String, Object> params = new HashMap<>();
        params.put("planId", planId);
        params.put("alimentoId", alimentoId);
        params.put("cantidad", cantidad);
        
        neo4jClient.query(query).bindAll(params).run();
    }

    // Vinculación de entidades y relaciones
    public void vincularEntidades(Long planId) {
        String query = """
            MATCH (p:PlanDieta) WHERE ID(p) = $planId
            OPTIONAL MATCH (p)-[:INCLUDES]->(a:Alimento)
            OPTIONAL MATCH (a)-[:COMPATIBLE_CON]->(a2:Alimento)
            OPTIONAL MATCH (a)-[:CONTIENE]->(n:Nutriente)
            WITH p, collect(DISTINCT a) as alimentos, collect(DISTINCT n) as nutrientes
            FOREACH (alimento IN alimentos |
                MERGE (alimento)-[:INCLUDED_IN]->(p)
            )
            FOREACH (nutriente IN nutrientes |
                MERGE (p)-[:CONTAINS]->(nutriente)
            )
        """;
        
        Map<String, Object> params = new HashMap<>();
        params.put("planId", planId);
        
        neo4jClient.query(query).bindAll(params).run();
    }

    // Validaciones y cálculos
    public boolean validarPlan(PlanDieta plan) {
        return plan.validarObjetivoCalorico() && 
               validarCompatibilidadAlimentos(plan) &&
               validarRestriccionesDieteticas(plan);
    }

    private boolean validarCompatibilidadAlimentos(PlanDieta plan) {
        // Implementar lógica de validación de compatibilidad
        return true;
    }

    private boolean validarRestriccionesDieteticas(PlanDieta plan) {
        // Implementar lógica de validación de restricciones
        return true;
    }

    // Métodos de consulta y estadísticas
    public List<PlanDieta> buscarPlanesActivos() {
        return planDietaRepository.findPlanesActivos();
    }

    public List<Map<String, Object>> obtenerEstadisticasObjetivos() {
        return planDietaRepository.obtenerEstadisticasObjetivos();
    }

    public List<PlanDieta> buscarPlanesPorRangoCalorias(int minCalorias, int maxCalorias) {
        return planDietaRepository.findByRangoCalorias(minCalorias, maxCalorias);
    }
    
    public List<PlanDieta> buscarPlanesPorRestricciones(boolean esVegetariano, 
                                                       boolean esVegano, 
                                                       boolean libreGluten) {
        return planDietaRepository.findByRestricciones(esVegetariano, esVegano, libreGluten);
    }

    // Actualización y mantenimiento de planes
    public void actualizarEstadoPlan(Long planId, String nuevoEstado) {
        planDietaRepository.findById(planId).ifPresent(plan -> {
            plan.setEstado(nuevoEstado);
            planDietaRepository.save(plan);
        });
    }

    public void desactivarPlanesExpirados() {
        List<PlanDieta> planesExpirados = planDietaRepository.findPlanesExpirados(LocalDate.now());
        planesExpirados.forEach(plan -> {
            plan.setActivo(false);
            plan.setEstado("COMPLETADO");
            planDietaRepository.save(plan);
        });
    }
    
    @Transactional
    public PlanDieta actualizarPlan(PlanDieta plan) {
        // Validar fechas
        if (plan.getFechaFin().isBefore(plan.getFechaInicio())) {
            throw new IllegalArgumentException("La fecha de fin no puede ser anterior a la fecha de inicio");
        }
        
        // Validar campos requeridos
        if (plan.getNombre() == null || plan.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del plan es requerido");
        }
        
        // Guardar cambios
        return planDietaRepository.save(plan);
    }
}
