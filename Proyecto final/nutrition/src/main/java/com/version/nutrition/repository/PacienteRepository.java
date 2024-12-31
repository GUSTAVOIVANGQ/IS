package com.version.nutrition.repository;

import com.version.nutrition.model.Paciente;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface PacienteRepository extends Neo4jRepository<Paciente, Long> {
    
    Optional<Paciente> findByEmail(String email);
    
    List<Paciente> findByNombreContainingOrApellidoContaining(String nombre, String apellido);
    
    @Query("MATCH (p:Paciente) WHERE p.pesoObjetivo > 0 RETURN p")
    List<Paciente> findPacientesConObjetivo();
    
    @Query("MATCH (p:Paciente)-[r:TIENE_MEDICION]->(m:Medicion) " +
           "WHERE ID(p) = $pacienteId " +
           "RETURN m ORDER BY m.fecha DESC LIMIT 1")
    Optional<Paciente.Medicion> findUltimaMedicion(@Param("pacienteId") Long pacienteId);
    
    @Query("MATCH (p:Paciente)-[r:HAS_PLAN]->(plan:PlanDieta) " +
           "WHERE plan.activo = true RETURN p")
    List<Paciente> findPacientesConPlanActivo();
    
    @Query("MATCH (p:Paciente)-[r:TIENE_CITA]->(c:Cita) " +
           "WHERE c.fecha >= $fecha " +
           "RETURN p")
    List<Paciente> findPacientesConCitaProxima(@Param("fecha") LocalDate fecha);
    
    @Query("MATCH (p:Paciente) " +
           "WHERE p.imc >= $imcMin AND p.imc <= $imcMax " +
           "RETURN p")
    List<Paciente> findByRangoIMC(@Param("imcMin") double imcMin, @Param("imcMax") double imcMax);
    
    @Query("MATCH (p:Paciente) " +
           "WHERE p.ultimaConsulta < $fecha " +
           "RETURN p")
    List<Paciente> findPacientesSinSeguimiento(@Param("fecha") LocalDate fecha);
    
    @Query("MATCH (p:Paciente) " +
           "WHERE EXISTS(p.alergias) AND p.alergias CONTAINS $alergia " +
           "RETURN p")
    List<Paciente> findByAlergia(@Param("alergia") String alergia);
    
    @Query("MATCH (p:Paciente)-[r:TIENE_SEGUIMIENTO]->(s:SeguimientoPeso) " +
           "WHERE ID(p) = $pacienteId " +
           "WITH p, collect(s) as seguimientos " +
           "WHERE size(seguimientos) >= 2 " +
           "RETURN p")
    List<Paciente> findPacientesConSeguimientoCompleto(@Param("pacienteId") Long pacienteId);
    
    @Query("MATCH (p:Paciente) " +
           "WHERE p.actividadFisica = $nivelActividad " +
           "RETURN p")
    List<Paciente> findByNivelActividad(@Param("nivelActividad") String nivelActividad);
    
    @Query("MATCH (p:Paciente)-[r:HAS_PLAN]->(plan:PlanDieta) " +
           "WHERE plan.objetivo = $objetivo " +
           "RETURN p")
    List<Paciente> findByObjetivoDieta(@Param("objetivo") String objetivo);

    // Estadísticas y reportes
    @Query("MATCH (p:Paciente) " +
           "WITH p.estadoNutricional as estado, count(p) as total " +
           "RETURN {estado: estado, cantidad: total}")
    List<Map<String, Object>> obtenerEstadisticasNutricionales();
}
