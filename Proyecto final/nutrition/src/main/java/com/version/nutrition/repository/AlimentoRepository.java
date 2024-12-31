package com.version.nutrition.repository;

import com.version.nutrition.model.Alimento;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface AlimentoRepository extends Neo4jRepository<Alimento, Long> {
    
    // Búsquedas básicas
    List<Alimento> findByNombreContainingIgnoreCase(String nombre);
    
    List<Alimento> findByCategoria(String categoria);
    
    // Búsquedas por valores nutricionales
    @Query("MATCH (a:Alimento) WHERE a.calorias <= $maxCalorias RETURN a")
    List<Alimento> findByCaloriasLessThan(@Param("maxCalorias") double maxCalorias);
    
    @Query("MATCH (a:Alimento) " +
           "WHERE a.proteinas >= $minProteinas " +
           "AND a.carbohidratos <= $maxCarbs " +
           "RETURN a")
    List<Alimento> findByMacronutrientes(@Param("minProteinas") double minProteinas,
                                        @Param("maxCarbs") double maxCarbs);
    
    // Búsquedas por restricciones dietéticas
    @Query("MATCH (a:Alimento) " +
           "WHERE (NOT $esVegetariano OR a.esVegetariano = true) " +
           "AND (NOT $esVegano OR a.esVegano = true) " +
           "AND (NOT $libreGluten OR a.libre_gluten = true) " +
           "RETURN a")
    List<Alimento> findByRestricciones(@Param("esVegetariano") boolean esVegetariano,
                                      @Param("esVegano") boolean esVegano,
                                      @Param("libreGluten") boolean libreGluten);
    
    // Búsqueda de alimentos compatibles
    @Query("MATCH (a:Alimento)-[:COMPATIBLE_CON]->(b:Alimento) " +
           "WHERE ID(a) = $alimentoId " +
           "RETURN b")
    List<Alimento> findAlimentosCompatibles(@Param("alimentoId") Long alimentoId);
    
    // Búsqueda por temporada
    List<Alimento> findByTemporada(String temporada);
    
    // Búsqueda de alimentos sin alérgenos específicos
    @Query("MATCH (a:Alimento) " +
           "WHERE NOT a.alergenos CONTAINS $alergeno " +
           "RETURN a")
    List<Alimento> findAlimentosSinAlergeno(@Param("alergeno") String alergeno);
    
    // Estadísticas y análisis
    @Query("MATCH (a:Alimento) " +
           "RETURN a.categoria as categoria, COUNT(a) as total, " +
           "AVG(a.calorias) as promedioCalorias " +
           "ORDER BY total DESC")
    List<Map<String, Object>> obtenerEstadisticasPorCategoria();
    
    // Búsqueda avanzada con múltiples criterios
    @Query("MATCH (a:Alimento) " +
           "WHERE ($categoria IS NULL OR a.categoria = $categoria) " +
           "AND ($maxCalorias IS NULL OR a.calorias <= $maxCalorias) " +
           "AND ($esVegetariano IS NULL OR a.esVegetariano = $esVegetariano) " +
           "RETURN a")
    List<Alimento> busquedaAvanzada(@Param("categoria") String categoria,
                                   @Param("maxCalorias") Double maxCalorias,
                                   @Param("esVegetariano") Boolean esVegetariano);
    
    // Recomendaciones basadas en objetivos nutricionales
    @Query("MATCH (a:Alimento) " +
           "WHERE a.categoria = $categoria " +
           "AND a.proteinas >= $minProteinas " +
           "AND a.calorias <= $maxCalorias " +
           "AND ($considerarVegetariano = false OR a.esVegetariano = true) " +
           "RETURN a " +
           "ORDER BY a.proteinas DESC " +
           "LIMIT 5")
    List<Alimento> recomendarAlimentosParaObjetivo(@Param("categoria") String categoria,
                                                  @Param("minProteinas") double minProteinas,
                                                  @Param("maxCalorias") double maxCalorias,
                                                  @Param("considerarVegetariano") boolean considerarVegetariano);
    
    @Query("MATCH (a:Alimento) RETURN a")
    List<Alimento> findAll();
}
