package com.version.nutrition.repository;

import com.version.nutrition.model.RespuestaTicket;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Repository
public interface RespuestaTicketRepository extends Neo4jRepository<RespuestaTicket, Long> {
    
    // Consultas básicas por ticket
    @Query("MATCH (r:RespuestaTicket)-[:PERTENECE_A]->(t:Ticket) " +
           "WHERE ID(t) = $ticketId " +
           "RETURN r ORDER BY r.fechaCreacion ASC")
    List<RespuestaTicket> findByTicketId(@Param("ticketId") Long ticketId);
    
    // Consultas por tipo de respuesta
    List<RespuestaTicket> findByTipo(String tipo);
    
    // Buscar respuestas automáticas
    @Query("MATCH (r:RespuestaTicket) " +
           "WHERE r.esRespuestaAutomatica = true " +
           "RETURN r ORDER BY r.fechaCreacion DESC")
    List<RespuestaTicket> findRespuestasAutomaticas();
    
    // Buscar por autor
    @Query("MATCH (r:RespuestaTicket)-[:RESPONDIDO_POR]->(u:Usuario) " +
           "WHERE ID(u) = $autorId " +
           "RETURN r ORDER BY r.fechaCreacion DESC")
    List<RespuestaTicket> findByAutorId(@Param("autorId") Long autorId);
    
    // Buscar respuestas destacadas
    @Query("MATCH (r:RespuestaTicket) " +
           "WHERE r.destacada = true " +
           "RETURN r ORDER BY r.fechaCreacion DESC")
    List<RespuestaTicket> findRespuestasDestacadas();
    
    // Estadísticas por tipo de respuesta
    @Query("MATCH (r:RespuestaTicket) " +
           "RETURN r.tipo as tipo, COUNT(r) as total, " +
           "AVG(duration.between(r.fechaCreacion, r.fechaModificacion).seconds) as tiempoPromedioRespuesta")
    List<Map<String, Object>> obtenerEstadisticasPorTipo();
    
    // Buscar respuestas por estado
    @Query("MATCH (r:RespuestaTicket) " +
           "WHERE r.estado = $estado " +
           "RETURN r ORDER BY r.fechaCreacion DESC")
    List<RespuestaTicket> findByEstado(@Param("estado") String estado);
    
    // Buscar respuestas recientes
    @Query("MATCH (r:RespuestaTicket) " +
           "WHERE r.fechaCreacion >= $fecha " +
           "RETURN r ORDER BY r.fechaCreacion DESC")
    List<RespuestaTicket> findRespuestasRecientes(@Param("fecha") LocalDateTime fecha);
    
    // Buscar respuestas con archivos adjuntos
    @Query("MATCH (r:RespuestaTicket) " +
           "WHERE r.archivoAdjunto IS NOT NULL " +
           "RETURN r ORDER BY r.fechaCreacion DESC")
    List<RespuestaTicket> findRespuestasConAdjuntos();
    
    // Contar respuestas por ticket
    @Query("MATCH (r:RespuestaTicket)-[:PERTENECE_A]->(t:Ticket) " +
           "WHERE ID(t) = $ticketId " +
           "RETURN COUNT(r)")
    long countRespuestasByTicket(@Param("ticketId") Long ticketId);
    
    // Búsqueda avanzada
    @Query("MATCH (r:RespuestaTicket) " +
           "WHERE ($tipo IS NULL OR r.tipo = $tipo) " +
           "AND ($estado IS NULL OR r.estado = $estado) " +
           "AND ($esAutomatica IS NULL OR r.esRespuestaAutomatica = $esAutomatica) " +
           "RETURN r ORDER BY r.fechaCreacion DESC")
    List<RespuestaTicket> busquedaAvanzada(@Param("tipo") String tipo,
                                          @Param("estado") String estado,
                                          @Param("esAutomatica") Boolean esAutomatica);
}
