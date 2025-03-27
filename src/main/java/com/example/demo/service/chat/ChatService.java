package com.example.demo.service.chat;

import com.example.demo.data.chat.ChatMessage;
import com.example.demo.data.chat.ChatMessage.MessageType;
import com.example.demo.data.chat.Conversation;
import com.example.demo.data.chat.Conversation.ConversationType;
import com.example.demo.repository.mongoDB.chat.ChatMessageRepository;
import com.example.demo.repository.mongoDB.chat.ConversationRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final ConversationRepository conversationRepository;

    public ChatService(ChatMessageRepository chatMessageRepository, ConversationRepository conversationRepository) {
        this.chatMessageRepository = chatMessageRepository;
        this.conversationRepository = conversationRepository;
    }

    // Send a private message from sender to receiver
    public ChatMessage sendPrivateMessage(String senderId, String receiverId, String content) {
        // TODO: Add logic to check if the receiver has blocked the sender.
        Conversation conversation = getOrCreatePrivateConversation(senderId, receiverId);
        ChatMessage message = new ChatMessage();
        message.setConversationId(conversation.getId());
        message.setSenderId(senderId);
        message.setContent(content);
        message.setTimestamp(LocalDateTime.now());
        message.setMessageType(MessageType.PRIVATE);
        return chatMessageRepository.save(message);
    }

    private Conversation getOrCreatePrivateConversation(String senderId, String receiverId) {
        // In a full implementation, search for an existing conversation with these two participants.
        // Here, we simply create a new conversation.
        Conversation conversation = new Conversation();
        conversation.setType(ConversationType.PRIVATE);
        conversation.setParticipantIds(List.of(senderId, receiverId));
        conversation.setName(null);
        return conversationRepository.save(conversation);
    }

    // Send a group message in an existing conversation (group chat)
    public ChatMessage sendGroupMessage(String senderId, String conversationId, String content) {
        ChatMessage message = new ChatMessage();
        message.setConversationId(conversationId);
        message.setSenderId(senderId);
        message.setContent(content);
        message.setTimestamp(LocalDateTime.now());
        message.setMessageType(MessageType.GROUP);
        return chatMessageRepository.save(message);
    }

    // Send a global message to the global chat room
    public ChatMessage sendGlobalMessage(String senderId, String content) {
        Conversation conversation = getGlobalConversation();
        ChatMessage message = new ChatMessage();
        message.setConversationId(conversation.getId());
        message.setSenderId(senderId);
        message.setContent(content);
        message.setTimestamp(LocalDateTime.now());
        message.setMessageType(MessageType.GLOBAL);
        return chatMessageRepository.save(message);
    }

    private Conversation getGlobalConversation() {
        // For simplicity, assume there is only one global conversation.
        Optional<Conversation> optional = conversationRepository.findAll().stream()
                .filter(conv -> conv.getType() == ConversationType.GLOBAL)
                .findFirst();
        if(optional.isPresent()){
            return optional.get();
        }
        Conversation conversation = new Conversation();
        conversation.setType(ConversationType.GLOBAL);
        conversation.setParticipantIds(null); // Not needed for global chat
        conversation.setName("Global Chat");
        return conversationRepository.save(conversation);
    }

    // Send a notification message to specific users (e.g., for suggestions or tournaments)
    public ChatMessage sendNotificationMessage(String senderId, List<String> targetUserIds, String content) {
        ChatMessage lastMessage = null;
        for(String targetUserId: targetUserIds) {
            Conversation conversation = getOrCreatePrivateConversation(senderId, targetUserId);
            ChatMessage message = new ChatMessage();
            message.setConversationId(conversation.getId());
            message.setSenderId(senderId);
            message.setContent(content);
            message.setTimestamp(LocalDateTime.now());
            message.setMessageType(MessageType.NOTIFICATION);
            lastMessage = chatMessageRepository.save(message);
        }
        return lastMessage;
    }

    // Retrieve messages for a given conversation
    public List<ChatMessage> getMessagesForConversation(String conversationId) {
        return chatMessageRepository.findByConversationIdOrderByTimestampAsc(conversationId);
    }
}
