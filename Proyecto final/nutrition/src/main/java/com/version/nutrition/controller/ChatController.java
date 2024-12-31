package com.version.nutrition.controller;

import com.version.nutrition.model.ChatMessage;
import com.version.nutrition.model.Usuario;
import com.version.nutrition.model.Paciente;
import com.version.nutrition.service.ChatService;
import com.version.nutrition.service.PacienteService;
import com.version.nutrition.service.NutricionistaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Controller
public class ChatController {
    
    @Autowired
    private ChatService chatService;
    
    @Autowired
    private PacienteService pacienteService;
    
    @Autowired
    private NutricionistaService nutricionistaService; // Agregar servicio

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @GetMapping("/chat")
    public String chatPage(Model model, Authentication auth) {
        String userRole = auth.getAuthorities().iterator().next().getAuthority();
        Usuario currentUser;
        
        if ("ROLE_NUTRICIONISTA".equals(userRole)) {
            currentUser = nutricionistaService.buscarPorEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Nutricionista no encontrado"));
            model.addAttribute("nutricionista", currentUser);
        } else {
            currentUser = pacienteService.buscarPorEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));
            // Obtener el nutricionista asignado
            model.addAttribute("nutricionista", nutricionistaService.listarTodos().stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No hay nutricionistas disponibles")));
        }
        
        // No cargar historial inicial, se cargará cuando se seleccione un contacto
        model.addAttribute("messages", new ArrayList<>());
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("userRole", userRole);
        
        return "chat/chat";
    }

    // Manejar mensajes enviados
    @MessageMapping("/chat.send")
    public void processMessage(@Payload ChatMessage chatMessage) {
        if (chatMessage.getContent() == null || chatMessage.getContent().trim().isEmpty()) {
            return;
        }
        
        chatMessage.setTimestamp(LocalDateTime.now());
        chatMessage.setType(ChatMessage.MessageType.CHAT);
        ChatMessage savedMessage = chatService.sendMessage(chatMessage);
        
        // Enviar al remitente
        messagingTemplate.convertAndSendToUser(
            chatMessage.getSender(),
            "/queue/messages",
            savedMessage
        );
        
        // Enviar al destinatario
        messagingTemplate.convertAndSendToUser(
            chatMessage.getRecipient(),
            "/queue/messages",
            savedMessage
        );
    }

    // Manejar notificación de "escribiendo..."
    @MessageMapping("/chat.typing")
    public void processTypingEvent(@Payload ChatMessage message) {
        if (message.getSender() != null && message.getRecipient() != null) {
            message.setType(ChatMessage.MessageType.TYPING);
            chatService.sendTypingNotification(message.getSender(), message.getRecipient());
        }
    }

    // Manejar eventos de conexión
    @MessageMapping("/chat.join")
    public void processJoinEvent(@Payload ChatMessage message, SimpMessageHeaderAccessor headerAccessor) {
        // Agregar usuario a la sesión
        headerAccessor.getSessionAttributes().put("username", message.getSender());
        message.setType(ChatMessage.MessageType.JOIN);
        chatService.sendJoinMessage(message.getSender());
    }

    // Manejar eventos de desconexión
    @MessageMapping("/chat.leave")
    public void processLeaveEvent(@Payload ChatMessage message) {
        message.setType(ChatMessage.MessageType.LEAVE);
        chatService.sendLeaveMessage(message.getSender());
    }

    // Obtener historial de chat
    @GetMapping("/api/chat/history")
    @ResponseBody
    public List<ChatMessage> getChatHistory(
            @RequestParam String otherUser, 
            Authentication auth) {
        String currentUser = auth.getName();
        // Obtener historial de ambas direcciones
        List<ChatMessage> messages = new ArrayList<>();
        messages.addAll(chatService.getChatHistory(currentUser, otherUser));
        messages.addAll(chatService.getChatHistory(otherUser, currentUser));
        // Ordenar por timestamp
        messages.sort((m1, m2) -> m1.getTimestamp().compareTo(m2.getTimestamp()));
        return messages;
    }

    // Marcar mensajes como leídos
    @PostMapping("/api/chat/read")
    @ResponseBody
    public void markMessagesAsRead(@RequestParam String sender, Authentication auth) {
        String recipient = auth.getName();
        chatService.markMessagesAsRead(sender, recipient);
    }

    // Obtener conteo de mensajes no leídos
    @GetMapping("/api/chat/unread")
    @ResponseBody
    public int getUnreadMessageCount(Authentication auth) {
        String username = auth.getName();
        return chatService.getUnreadMessageCount(username);
    }

    // Eliminar mensaje
    @DeleteMapping("/api/chat/messages/{messageId}")
    @ResponseBody
    public void deleteMessage(@PathVariable Long messageId, Authentication auth) {
        // Aquí deberías verificar que el usuario tenga permiso para eliminar el mensaje
        chatService.deleteMessage(messageId);
    }
}
