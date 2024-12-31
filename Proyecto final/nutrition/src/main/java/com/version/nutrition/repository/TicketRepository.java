package com.version.nutrition.repository;

import com.version.nutrition.model.Ticket;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Repository
public interface TicketRepository extends Neo4jRepository<Ticket, Long> {
    
    // Consultas básicas
    List<Ticket> findByEstado(String estado);
    List<Ticket> findByCategoria(String categoria);
    List<Ticket> findByPrioridad(String prioridad);
    
    // Consultas por relaciones
    @Query("MATCH (t:Ticket)-[:CREADO_POR]->(p:Paciente) " +
           "WHERE ID(p) = $pacienteId " +
           "RETURN t ORDER BY t.fechaCreacion DESC")
    List<Ticket> findByPacienteId(@Param("pacienteId") Long pacienteId);
    
    @Query("MATCH (t:Ticket)-[:ASIGNADO_A]->(n:Nutricionista) " +
           "WHERE ID(n) = $nutricionistaId " +
           "RETURN t ORDER BY t.fechaCreacion DESC")
    List<Ticket> findByNutricionistaId(@Param("nutricionistaId") Long nutricionistaId);
    
    // Consultas para estadísticas
    @Query("MATCH (t:Ticket) WHERE t.estado = $estado RETURN COUNT(t)")
    long countByEstado(@Param("estado") String estado);
    
    @Query("MATCH (t:Ticket) " +
           "WHERE t.fechaCreacion >= $fechaInicio " +
           "AND t.fechaCreacion <= $fechaFin " +
           "RETURN t")
    List<Ticket> findByRangoFechas(@Param("fechaInicio") LocalDateTime fechaInicio,
                                  @Param("fechaFin") LocalDateTime fechaFin);
    
    // Búsqueda por etiquetas
    @Query("MATCH (t:Ticket) " +
           "WHERE ANY(etiqueta IN t.etiquetas WHERE etiqueta IN $etiquetas) " +
           "RETURN t")
    List<Ticket> findByEtiquetasContaining(@Param("etiquetas") List<String> etiquetas);
    
    // Búsqueda avanzada
    @Query("MATCH (t:Ticket) " +
           "WHERE ($estado IS NULL OR t.estado = $estado) " +
           "AND ($categoria IS NULL OR t.categoria = $categoria) " +
           "AND ($prioridad IS NULL OR t.prioridad = $prioridad) " +
           "RETURN t")
    List<Ticket> findByFiltros(@Param("estado") String estado,
                              @Param("categoria") String categoria,
                              @Param("prioridad") String prioridad);
    
    // Estadísticas y reportes
    @Query("MATCH (t:Ticket) " +
           "RETURN t.categoria as categoria, " +
           "COUNT(t) as total, " +
           "AVG(duration.between(t.fechaCreacion, t.fechaCierre).hours) as tiempoPromedioResolucion " +
           "ORDER BY total DESC")
    List<Map<String, Object>> obtenerEstadisticasPorCategoria();
    
    // Tickets sin asignar
    @Query("MATCH (t:Ticket) " +
           "WHERE NOT EXISTS((t)-[:ASIGNADO_A]->()) " +
           "AND t.estado = 'ABIERTO' " +
           "RETURN t ORDER BY t.fechaCreacion ASC")
    List<Ticket> findTicketsSinAsignar();
    
    // Tickets urgentes
    @Query("MATCH (t:Ticket) " +
           "WHERE t.prioridad = 'ALTA' " +
           "AND t.estado IN ['ABIERTO', 'EN_PROGRESO'] " +
           "RETURN t ORDER BY t.fechaCreacion ASC")
    List<Ticket> findTicketsUrgentes();
    
    // Tickets resueltos recientemente
    @Query("MATCH (t:Ticket) " +
           "WHERE t.estado = 'RESUELTO' " +
           "AND t.fechaCierre >= $fecha " +
           "RETURN t ORDER BY t.fechaCierre DESC")
    List<Ticket> findTicketsResueltosRecientes(@Param("fecha") LocalDateTime fecha);
}
