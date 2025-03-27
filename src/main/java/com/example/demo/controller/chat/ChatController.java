package com.example.demo.controller.chat;

import com.example.demo.data.chat.ChatMessage;
import com.example.demo.service.chat.ChatService;
import com.example.demo.security.request.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;
    private final JwtUtil jwtUtil;

    public ChatController(ChatService chatService, JwtUtil jwtUtil) {
        this.chatService = chatService;
        this.jwtUtil = jwtUtil;
    }

    // Helper method to extract username from the request header
    private Optional<String> extractUsername(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if(authHeader != null && authHeader.startsWith("Bearer ")) {
            return Optional.of(jwtUtil.extractUsername(authHeader.substring(7)));
        }
        return Optional.empty();
    }

    // Endpoint for sending a private message
    @PostMapping("/private")
    public ResponseEntity<?> sendPrivateMessage(@RequestParam String receiverId,
                                                @RequestParam String content,
                                                HttpServletRequest request) {
        Optional<String> senderOpt = extractUsername(request);
        if(senderOpt.isEmpty()){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
        // TODO: Verify that the receiver has not blocked the sender.
        ChatMessage message = chatService.sendPrivateMessage(senderOpt.get(), receiverId, content);
        return ResponseEntity.ok(message);
    }

    // Endpoint for sending a group message
    @PostMapping("/group/{conversationId}")
    public ResponseEntity<?> sendGroupMessage(@PathVariable String conversationId,
                                              @RequestParam String content,
                                              HttpServletRequest request) {
        Optional<String> senderOpt = extractUsername(request);
        if(senderOpt.isEmpty()){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
        ChatMessage message = chatService.sendGroupMessage(senderOpt.get(), conversationId, content);
        return ResponseEntity.ok(message);
    }

    // Endpoint for sending a global message
    @PostMapping("/global")
    public ResponseEntity<?> sendGlobalMessage(@RequestParam String content, HttpServletRequest request) {
        Optional<String> senderOpt = extractUsername(request);
        if(senderOpt.isEmpty()){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
        ChatMessage message = chatService.sendGlobalMessage(senderOpt.get(), content);
        return ResponseEntity.ok(message);
    }

    // Endpoint for sending notification messages to a list of target users
    @PostMapping("/notify")
    public ResponseEntity<?> sendNotification(@RequestBody NotificationRequest notificationRequest,
                                              HttpServletRequest request) {
        Optional<String> senderOpt = extractUsername(request);
        if(senderOpt.isEmpty()){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
        ChatMessage message = chatService.sendNotificationMessage(senderOpt.get(),
                notificationRequest.getTargetUserIds(),
                notificationRequest.getContent());
        return ResponseEntity.ok(message);
    }

    // Endpoint for retrieving messages of a conversation
    @GetMapping("/conversation/{conversationId}")
    public ResponseEntity<?> getConversationMessages(@PathVariable String conversationId,
                                                     HttpServletRequest request) {
        // Optionally, verify that the requester is part of the conversation.
        List<ChatMessage> messages = chatService.getMessagesForConversation(conversationId);
        return ResponseEntity.ok(messages);
    }

    // DTO for notification requests
    public static class NotificationRequest {
        private List<String> targetUserIds;
        private String content;

        public List<String> getTargetUserIds() {
            return targetUserIds;
        }
        public void setTargetUserIds(List<String> targetUserIds) {
            this.targetUserIds = targetUserIds;
        }
        public String getContent() {
            return content;
        }
        public void setContent(String content) {
            this.content = content;
        }
    }
}
