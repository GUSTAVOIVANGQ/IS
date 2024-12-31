package com.version.nutrition.repository;

import com.version.nutrition.model.PlanDieta;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Repository
public interface PlanDietaRepository extends Neo4jRepository<PlanDieta, Long> {
    
    List<PlanDieta> findByNombreContaining(String nombre);
    
    List<PlanDieta> findByObjetivo(String objetivo);
    
    @Query("MATCH (p:PlanDieta) WHERE p.activo = true RETURN p")
    List<PlanDieta> findPlanesActivos();
    
    @Query("MATCH (p:PlanDieta)-[r:INCLUDES]->(a:Alimento) " +
           "WHERE ID(p) = $planId " +
           "RETURN a")
    List<Map<String, Object>> findAlimentosByPlanId(@Param("planId") Long planId);
    
    @Query("MATCH (p:PlanDieta) " +
           "WHERE p.caloriasObjetivo >= $minCalorias AND p.caloriasObjetivo <= $maxCalorias " +
           "RETURN p")
    List<PlanDieta> findByRangoCalorias(@Param("minCalorias") int minCalorias, 
                                       @Param("maxCalorias") int maxCalorias);
    
    @Query("MATCH (p:PlanDieta) " +
           "WHERE p.esVegetariano = $esVegetariano " +
           "AND p.esVegano = $esVegano " +
           "AND p.libreGluten = $libreGluten " +
           "RETURN p")
    List<PlanDieta> findByRestricciones(@Param("esVegetariano") boolean esVegetariano,
                                       @Param("esVegano") boolean esVegano,
                                       @Param("libreGluten") boolean libreGluten);
    
    @Query("MATCH (n:Nutricionista)-[c:CREATED_BY]->(p:PlanDieta) " +
           "WHERE ID(n) = $nutricionistaId " +
           "RETURN p")
    List<PlanDieta> findByNutricionista(@Param("nutricionistaId") Long nutricionistaId);
    
    @Query("MATCH (p:PlanDieta)-[r:ASSIGNED_TO]->(pac:Paciente) " +
           "WHERE ID(pac) = $pacienteId AND p.activo = true " +
           "RETURN p")
    List<PlanDieta> findPlanesPacienteActivo(@Param("pacienteId") Long pacienteId);
    
    @Query("MATCH (p:PlanDieta) " +
           "WHERE p.fechaFin < $fecha " +
           "RETURN p")
    List<PlanDieta> findPlanesExpirados(@Param("fecha") LocalDate fecha);
    
    @Query("MATCH (p:PlanDieta)-[r:INCLUDES]->(a:Alimento) " +
           "WITH p, count(a) as numAlimentos " +
           "WHERE numAlimentos >= $minAlimentos " +
           "RETURN p")
    List<PlanDieta> findPlanesCompletos(@Param("minAlimentos") int minAlimentos);

    // Estadísticas
    @Query("MATCH (p:PlanDieta) " +
           "RETURN p.objetivo as objetivo, count(p) as total " +
           "ORDER BY total DESC")
    List<Map<String, Object>> obtenerEstadisticasObjetivos();
}
