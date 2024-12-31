package com.version.nutrition.model;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.time.LocalDateTime;

@Node
public class ChatMessage {
    @Id
    @GeneratedValue
    private Long id;
    
    private String content;
    private String sender;
    private String recipient;
    private LocalDateTime timestamp;
    private boolean read;
    private MessageType type; // Tipo de mensaje: CHAT, JOIN, LEAVE
    private String room; // Para chat grupal si se implementa en el futuro
    
    @Relationship(type = "SENT_BY")
    private Usuario senderUser;
    
    @Relationship(type = "SENT_TO")
    private Usuario recipientUser;

    // Enum para tipos de mensaje
    public enum MessageType {
        CHAT,        // Mensaje normal
        JOIN,        // Usuario se une al chat
        LEAVE,       // Usuario abandona el chat
        TYPING,      // Usuario está escribiendo
        READ        // Mensaje leído
    }

    // Constructor por defecto
    public ChatMessage() {
        this.timestamp = LocalDateTime.now();
        this.read = false;
    }

    // Constructor con parámetros principales
    public ChatMessage(String content, String sender, String recipient, MessageType type) {
        this();
        this.content = content;
        this.sender = sender;
        this.recipient = recipient;
        this.type = type;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public Usuario getSenderUser() {
        return senderUser;
    }

    public void setSenderUser(Usuario senderUser) {
        this.senderUser = senderUser;
    }

    public Usuario getRecipientUser() {
        return recipientUser;
    }

    public void setRecipientUser(Usuario recipientUser) {
        this.recipientUser = recipientUser;
    }

    // Métodos de utilidad
    public boolean isFromUser(String userId) {
        return this.sender.equals(userId);
    }

    public boolean isToUser(String userId) {
        return this.recipient.equals(userId);
    }

    public String getChatRoom() {
        String[] participants = {sender, recipient};
        java.util.Arrays.sort(participants);
        return String.join("-", participants);
    }
}
