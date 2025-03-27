package com.example.demo.repository.mongoDB.chat;
import com.example.demo.data.chat.Conversation;
import com.example.demo.data.chat.Conversation.ConversationType;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends MongoRepository<Conversation, String> {
    // Custom method could be added here to find a private conversation for two specific participants.
    // For simplicity, we assume a new conversation is created if none exists.
    Optional<Conversation> findById(String id);
}
