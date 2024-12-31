package com.version.nutrition.repository;

import com.version.nutrition.model.Cita;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface CitaRepository extends Neo4jRepository<Cita, Long> {
    
    // Crear nueva cita con todas las relaciones
    @Query("MATCH (n:Nutricionista), (p:Paciente) " +
           "WHERE ID(n) = $nutricionistaId AND ID(p) = $pacienteId " +
           "CREATE (c:Cita) " +
           "SET c = $citaData " +
           "CREATE (c)-[:AGENDA_CON]->(n) " +
           "CREATE (c)-[:PARA_PACIENTE]->(p) " +
           "CREATE (n)-[:ATIENDE]->(p) " +
           "RETURN c")
    Cita crearCitaConRelaciones(
        @Param("nutricionistaId") Long nutricionistaId,
        @Param("pacienteId") Long pacienteId,
        @Param("citaData") Map<String, Object> citaData
    );

    // Buscar citas por nutricionista con relaciones completas
    @Query("MATCH (c:Cita)-[r1:AGENDA_CON]->(n:Nutricionista) " +
           "MATCH (c)-[r2:PARA_PACIENTE]->(p:Paciente) " +
           "WHERE ID(n) = $nutricionistaId " +
           "RETURN c, r1, r2, n, p " +
           "ORDER BY c.fecha")
    List<Cita> findByNutricionista(@Param("nutricionistaId") Long nutricionistaId);
    
    // Buscar citas por paciente con relaciones completas
    @Query("MATCH (c:Cita)-[r1:PARA_PACIENTE]->(p:Paciente) " +
           "MATCH (c)-[r2:AGENDA_CON]->(n:Nutricionista) " +
           "WHERE ID(p) = $pacienteId " +
           "RETURN c, r1, r2, n, p " +
           "ORDER BY c.fecha")
    List<Cita> findByPaciente(@Param("pacienteId") Long pacienteId);

    // Verificar si existe relación entre nutricionista y paciente
    @Query("MATCH (n:Nutricionista)-[r:ATIENDE]->(p:Paciente) " +
           "WHERE ID(n) = $nutricionistaId AND ID(p) = $pacienteId " +
           "RETURN COUNT(r) > 0")
    boolean existeRelacionNutricionistaPaciente(
        @Param("nutricionistaId") Long nutricionistaId,
        @Param("pacienteId") Long pacienteId
    );

    // Buscar citas futuras de un nutricionista
    @Query("MATCH (c:Cita)-[r:AGENDA_CON]->(n:Nutricionista) " +
           "WHERE ID(n) = $nutricionistaId AND c.fecha >= $fecha " +
           "RETURN c ORDER BY c.fecha")
    List<Cita> findFuturasByNutricionista(
        @Param("nutricionistaId") Long nutricionistaId,
        @Param("fecha") LocalDateTime fecha
    );
    
    // Buscar citas por estado
    @Query("MATCH (c:Cita) WHERE c.estado = $estado RETURN c")
    List<Cita> findByEstado(@Param("estado") String estado);
    
    // Verificar disponibilidad de horario
    @Query("MATCH (c:Cita)-[r:AGENDA_CON]->(n:Nutricionista) " +
           "WHERE ID(n) = $nutricionistaId AND c.fecha = $fecha " +
           "RETURN COUNT(c) > 0")
    boolean existsCitaInHorario(
        @Param("nutricionistaId") Long nutricionistaId,
        @Param("fecha") LocalDateTime fecha
    );
    
    // Buscar citas por rango de fechas
    @Query("MATCH (c:Cita) " +
           "WHERE c.fecha >= $fechaInicio AND c.fecha <= $fechaFin " +
           "RETURN c ORDER BY c.fecha")
    List<Cita> findByRangoFechas(
        @Param("fechaInicio") LocalDateTime fechaInicio,
        @Param("fechaFin") LocalDateTime fechaFin
    );
    
    // Contar citas pendientes por nutricionista
    @Query("MATCH (c:Cita)-[r:AGENDA_CON]->(n:Nutricionista) " +
           "WHERE ID(n) = $nutricionistaId AND c.estado = 'PENDIENTE' " +
           "RETURN COUNT(c)")
    int countCitasPendientes(@Param("nutricionistaId") Long nutricionistaId);
    
    // Buscar citas virtuales
    @Query("MATCH (c:Cita) WHERE c.esVirtual = true RETURN c")
    List<Cita> findCitasVirtuales();
    
    // Buscar citas por tipo
    @Query("MATCH (c:Cita) WHERE c.tipoCita = $tipoCita RETURN c")
    List<Cita> findByTipoCita(@Param("tipoCita") String tipoCita);
    
    // Buscar próxima cita de un paciente
    @Query("MATCH (c:Cita)-[r:PARA_PACIENTE]->(p:Paciente) " +
           "WHERE ID(p) = $pacienteId AND c.fecha >= $fecha " +
           "RETURN c ORDER BY c.fecha LIMIT 1")
    Cita findProximaCitaPaciente(
        @Param("pacienteId") Long pacienteId,
        @Param("fecha") LocalDateTime fecha
    );

    // Contar citas completadas por nutricionista en un período
    @Query("MATCH (c:Cita)-[r:AGENDA_CON]->(n:Nutricionista) " +
           "WHERE ID(n) = $nutricionistaId AND c.estado = 'COMPLETADA' " +
           "AND c.fecha >= $fechaInicio AND c.fecha <= $fechaFin " +
           "RETURN COUNT(c)")
    int countCitasCompletadas(
        @Param("nutricionistaId") Long nutricionistaId,
        @Param("fechaInicio") LocalDateTime fechaInicio,
        @Param("fechaFin") LocalDateTime fechaFin
    );

    @Query("MATCH (c:Cita)-[r:PARA_PACIENTE]->(p:Paciente) " +
           "MATCH (c)-[r2:AGENDA_CON]->(n:Nutricionista) " +
           "WHERE ID(p) = $pacienteId AND c.fecha >= $fechaActual " +
           "AND c.estado IN ['PENDIENTE', 'CONFIRMADA'] " +
           "RETURN c, n, r2 " +
           "ORDER BY c.fecha ASC LIMIT 1")
    Optional<Cita> findByPacienteAndFechaAfter(
        @Param("pacienteId") Long pacienteId, 
        @Param("fechaActual") LocalDateTime fechaActual
    );

    // Encontrar todas las citas futuras con relaciones completas
    @Query("MATCH (c:Cita)-[r1:AGENDA_CON]->(n:Nutricionista) " +
           "MATCH (c)-[r2:PARA_PACIENTE]->(p:Paciente) " +
           "WHERE c.fecha >= $fecha " +
           "RETURN c, r1, r2, n, p " +
           "ORDER BY c.fecha")
    List<Cita> findCitasFuturas(@Param("fecha") LocalDateTime fecha);

    // Cancelar cita manteniendo relaciones históricas
    @Query("MATCH (c:Cita)-[r1:AGENDA_CON]->(n:Nutricionista) " +
           "MATCH (c)-[r2:PARA_PACIENTE]->(p:Paciente) " +
           "WHERE ID(c) = $citaId " +
           "SET c.estado = 'CANCELADA', c.fechaModificacion = $fechaModificacion " +
           "RETURN c, r1, r2, n, p")
    Cita cancelarCita(
        @Param("citaId") Long citaId,
        @Param("fechaModificacion") LocalDateTime fechaModificacion
    );
}
