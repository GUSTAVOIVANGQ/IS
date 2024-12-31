package com.version.nutrition.service;

import com.version.nutrition.model.ChatMessage;
import com.version.nutrition.model.ChatMessage.MessageType;
import com.version.nutrition.repository.ChatMessageRepository;
import com.version.nutrition.model.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import com.version.nutrition.service.NutricionistaService;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.ArrayList;
import java.time.LocalDateTime;
import java.util.Collections;

@Service
@Transactional
public class ChatService {
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    @Autowired
    private ChatMessageRepository chatMessageRepository;
    
    private UserService userService;
    
    @Autowired
    private PacienteService pacienteService;
    
    @Autowired
    private NutricionistaService nutricionistaService;

    // Enviar mensaje
    public ChatMessage sendMessage(ChatMessage message) {
        // Guardar el mensaje en la base de datos
        ChatMessage savedMessage = chatMessageRepository.save(message);
        
        // Enviar el mensaje al destinatario
        messagingTemplate.convertAndSendToUser(
            message.getRecipient(),
            "/queue/messages",
            savedMessage
        );
        
        return savedMessage;
    }

    // Obtener historial de chat entre dos usuarios
    public List<ChatMessage> getChatHistory(String sender, String recipient) {
        return chatMessageRepository.findMessagesBetweenUsers(
            sender, 
            recipient, 
            LocalDateTime.now().minusMonths(1).toString(), // último mes
            LocalDateTime.now().toString()
        );
    }

    // Marcar mensajes como leídos
    public void markMessagesAsRead(String sender, String recipient) {
        List<ChatMessage> unreadMessages = chatMessageRepository.findUnreadMessages(recipient, sender);
        unreadMessages.forEach(message -> {
            message.setRead(true);
            chatMessageRepository.save(message);
        });
    }

    // Enviar notificación de "escribiendo..."
    public void sendTypingNotification(String sender, String recipient) {
        ChatMessage typingMessage = new ChatMessage();
        typingMessage.setSender(sender);
        typingMessage.setRecipient(recipient);
        typingMessage.setType(MessageType.TYPING);
        
        messagingTemplate.convertAndSendToUser(
            recipient,
            "/queue/typing",
            typingMessage
        );
    }

    // Obtener usuarios conectados
    public List<String> getActiveUsers(String currentUser) {
        // Implementar lógica para obtener usuarios activos
        return new ArrayList<>(); // Placeholder
    }

    // Enviar mensaje de unión al chat
    public void sendJoinMessage(String username) {
        ChatMessage joinMessage = new ChatMessage();
        joinMessage.setType(MessageType.JOIN);
        joinMessage.setSender(username);
        joinMessage.setContent(username + " se ha unido al chat");
        
        messagingTemplate.convertAndSend("/topic/public", joinMessage);
    }

    // Enviar mensaje de abandono del chat
    public void sendLeaveMessage(String username) {
        ChatMessage leaveMessage = new ChatMessage();
        leaveMessage.setType(MessageType.LEAVE);
        leaveMessage.setSender(username);
        leaveMessage.setContent(username + " ha abandonado el chat");
        
        messagingTemplate.convertAndSend("/topic/public", leaveMessage);
    }

    // Obtener mensajes no leídos
    public int getUnreadMessageCount(String username) {
        return chatMessageRepository.countUnreadMessages(username);
    }

    // Eliminar mensajes
    public void deleteMessage(Long messageId) {
        chatMessageRepository.deleteById(messageId);
    }

    // Verificar si un usuario está en línea
    public boolean isUserOnline(String username) {
        // Implementar lógica para verificar estado del usuario
        return true; // Placeholder
    }

    // Obtener último mensaje entre dos usuarios
    public ChatMessage getLastMessage(String sender, String recipient) {
        return chatMessageRepository.findFirstBySenderAndRecipientOrderByTimestampDesc(
            sender, recipient);
    }

    // Obtener mensajes del nutricionista
    public List<ChatMessage> getChatMessagesForNutricionista(String nutricionistaEmail) {
        return chatMessageRepository.findByRecipientOrSenderOrderByTimestampDesc(
            nutricionistaEmail
        );
    }
    
    // Obtener contactos del usuario
    public List<Usuario> getContactsForUser(String userEmail, String role) {
        if ("ROLE_NUTRICIONISTA".equals(role)) {
            return nutricionistaService.buscarPorEmail(userEmail)
                .map(nutricionista -> new ArrayList<Usuario>(nutricionista.getPacientes()))
                .orElse(new ArrayList<>());
        } else {
            return pacienteService.buscarPorEmail(userEmail)
                .map(paciente -> Collections.singletonList((Usuario) paciente))
                .orElse(new ArrayList<>());
        }
    }
}
