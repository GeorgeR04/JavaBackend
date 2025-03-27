package com.example.demo.data.chat;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Data
@Document(collection = "conversations")
public class Conversation {
    @Id
    private String id;
    private ConversationType type; // PRIVATE, GROUP, or GLOBAL
    private List<String> participantIds; // List of participant user IDs (for private/group)
    private String name; // Optional, for example, group name or "Global Chat"

    public enum ConversationType {
        PRIVATE, GROUP, GLOBAL
    }
}
