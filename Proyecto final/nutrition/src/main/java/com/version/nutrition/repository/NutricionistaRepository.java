package com.version.nutrition.repository;

import com.version.nutrition.model.Nutricionista;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NutricionistaRepository extends Neo4jRepository<Nutricionista, Long> {
    
    Optional<Nutricionista> findByEmail(String email);
    
    List<Nutricionista> findByEspecialidad(String especialidad);
    
    @Query("MATCH (n:Nutricionista) WHERE n.disponibleConsultaOnline = true RETURN n")
    List<Nutricionista> findAllAvailableOnline();
    
    @Query("MATCH (n:Nutricionista)-[r:ATIENDE]->(p:Paciente) " +
           "WHERE ID(n) = $nutricionistaId " +
           "RETURN count(p)")
    int countPacientesActivos(@Param("nutricionistaId") Long nutricionistaId);
    
    @Query("MATCH (n:Nutricionista) " +
           "WHERE n.calificacionPromedio >= $minCalificacion " +
           "RETURN n ORDER BY n.calificacionPromedio DESC")
    List<Nutricionista> findByMinCalificacion(@Param("minCalificacion") double minCalificacion);
    
    @Query("MATCH (n:Nutricionista)-[r:CREATES_PLAN]->(p:PlanDieta) " +
           "WHERE ID(n) = $nutricionistaId " +
           "RETURN count(p)")
    int countPlanesCreados(@Param("nutricionistaId") Long nutricionistaId);
    
    @Query("MATCH (n:Nutricionista)-[r:TIENE_CITA]->(c:Cita) " +
           "WHERE ID(n) = $nutricionistaId AND c.fecha >= $fechaInicio AND c.fecha <= $fechaFin " +
           "RETURN count(c)")
    int countCitasEnPeriodo(@Param("nutricionistaId") Long nutricionistaId, 
                           @Param("fechaInicio") String fechaInicio, 
                           @Param("fechaFin") String fechaFin);
    
    @Query("MATCH (n:Nutricionista) " +
           "WHERE toLower(n.especialidades) CONTAINS toLower($especialidad) " +
           "RETURN n")
    List<Nutricionista> findByEspecialidadContaining(@Param("especialidad") String especialidad);
    
    @Query("MATCH (n:Nutricionista) " +
           "WHERE n.tarifaConsulta <= $maxTarifa " +
           "RETURN n ORDER BY n.tarifaConsulta ASC")
    List<Nutricionista> findByTarifaMaxima(@Param("maxTarifa") double maxTarifa);
    
    @Query("MATCH (n:Nutricionista)-[r:ATIENDE]->(p:Paciente) " +
           "WITH n, count(p) as pacientesCount " +
           "WHERE pacientesCount < $maxPacientes " +
           "RETURN n")
    List<Nutricionista> findDisponiblesParaNuevosPacientes(@Param("maxPacientes") int maxPacientes);

    Optional<Nutricionista> findByNombre(String nombre);

    @Query("MATCH (n:Nutricionista), (p:Paciente) " +
           "WHERE n.nombre = 'admin' " +
           "CREATE (n)-[:ATIENDE]->(p)")
    void vincularPacientesConAdmin();

    @Query("MATCH (n:Nutricionista), (p:Paciente) " +
           "WHERE n.nombre = 'admin' AND ID(p) = $pacienteId " +
           "CREATE (n)-[:ATIENDE]->(p)")
    void vincularPacienteConAdmin(@Param("pacienteId") Long pacienteId);
}
