package com.version.nutrition.repository;

import com.version.nutrition.model.ChatMessage;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends Neo4jRepository<ChatMessage, Long> {
    
    // Buscar mensajes entre dos usuarios ordenados por fecha
    List<ChatMessage> findBySenderAndRecipientOrderByTimestampDesc(String sender, String recipient);
    
    // Encontrar el último mensaje entre dos usuarios
    ChatMessage findFirstBySenderAndRecipientOrderByTimestampDesc(String sender, String recipient);
    
    // Buscar mensajes no leídos
    @Query("MATCH (m:ChatMessage) WHERE m.recipient = $recipient AND m.sender = $sender AND m.read = false RETURN m ORDER BY m.timestamp")
    List<ChatMessage> findUnreadMessages(@Param("recipient") String recipient, @Param("sender") String sender);
    
    // Contar mensajes no leídos para un usuario
    @Query("MATCH (m:ChatMessage) WHERE m.recipient = $username AND m.read = false RETURN count(m)")
    int countUnreadMessages(@Param("username") String username);
    
    // Buscar mensajes por tipo
    List<ChatMessage> findByType(String type);
    
    // Buscar mensajes por sala de chat
    List<ChatMessage> findByRoomOrderByTimestampAsc(String room);
    
    // Buscar mensajes entre dos usuarios en un rango de tiempo
    @Query("MATCH (m:ChatMessage) WHERE m.sender IN [$user1, $user2] AND m.recipient IN [$user1, $user2] " +
           "AND m.timestamp >= $startTime AND m.timestamp <= $endTime RETURN m ORDER BY m.timestamp")
    List<ChatMessage> findMessagesBetweenUsers(
        @Param("user1") String user1, 
        @Param("user2") String user2, 
        @Param("startTime") String startTime, 
        @Param("endTime") String endTime
    );
    
    // Buscar últimos mensajes de un usuario con todos sus contactos
    @Query("MATCH (m:ChatMessage) WHERE m.sender = $username OR m.recipient = $username " +
           "WITH m.sender AS contact, m.recipient AS contact2, m " +
           "ORDER BY m.timestamp DESC " +
           "RETURN DISTINCT m LIMIT 10")
    List<ChatMessage> findRecentContactMessages(@Param("username") String username);
    
    // Eliminar todos los mensajes entre dos usuarios
    @Query("MATCH (m:ChatMessage) WHERE m.sender IN [$user1, $user2] AND m.recipient IN [$user1, $user2] DELETE m")
    void deleteConversation(@Param("user1") String user1, @Param("user2") String user2);

    // Buscar mensajes recibidos o enviados por un nutricionista
    @Query("MATCH (m:ChatMessage) WHERE m.recipient = $email OR m.sender = $email " +
           "RETURN m ORDER BY m.timestamp DESC")
    List<ChatMessage> findByRecipientOrSenderOrderByTimestampDesc(
        @Param("email") String email
    );

    // Buscar mensajes entre nutricionista y paciente específico
    @Query("MATCH (m:ChatMessage) WHERE " +
           "(m.sender = $nutricionistaEmail AND m.recipient = $pacienteEmail) OR " +
           "(m.sender = $pacienteEmail AND m.recipient = $nutricionistaEmail) " +
           "RETURN m ORDER BY m.timestamp DESC")
    List<ChatMessage> findMessagesBetweenNutricionistaAndPaciente(
        @Param("nutricionistaEmail") String nutricionistaEmail,
        @Param("pacienteEmail") String pacienteEmail
    );

    // Obtener últimos mensajes del nutricionista con cada paciente
    @Query("MATCH (m:ChatMessage) WHERE m.sender = $nutricionistaEmail OR m.recipient = $nutricionistaEmail " +
           "WITH m.sender AS sender, m.recipient AS recipient, collect(m)[0] AS lastMessage " +
           "WHERE sender <> $nutricionistaEmail OR recipient <> $nutricionistaEmail " +
           "RETURN lastMessage ORDER BY lastMessage.timestamp DESC")
    List<ChatMessage> findLastMessagesWithPacientes(@Param("nutricionistaEmail") String nutricionistaEmail);

    // Contar mensajes no leídos agrupados por paciente
    @Query("MATCH (m:ChatMessage) WHERE m.recipient = $nutricionistaEmail AND m.read = false " +
           "WITH m.sender AS paciente, count(m) AS unreadCount " +
           "RETURN paciente, unreadCount")
    List<Object[]> countUnreadMessagesPerPaciente(@Param("nutricionistaEmail") String nutricionistaEmail);
}
