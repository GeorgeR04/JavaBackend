package com.example.demo.data.chat;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@Document(collection = "chatMessages")
public class ChatMessage {
    @Id
    private String id;
    private String conversationId; // Identifier of the conversation (private, group, or global)
    private String senderId;       // Sender's user ID
    private String content;
    private LocalDateTime timestamp;
    private MessageType messageType; // Type of message: PRIVATE, GROUP, GLOBAL, or NOTIFICATION

    public enum MessageType {
        PRIVATE, GROUP, GLOBAL, NOTIFICATION
    }
}
