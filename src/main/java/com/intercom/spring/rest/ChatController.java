package com.intercom.spring.rest;

import com.intercom.spring.domain.models.Conversation;
import com.intercom.spring.domain.models.Message;
import com.intercom.spring.domain.exception.ChatRepositoryException;
import com.intercom.spring.domain.exception.InvalidInputException;
import com.intercom.spring.ports.inbound.ChatAPI;
import com.intercom.spring.rest.dto.MarkAsReadRequest;
import com.intercom.spring.rest.dto.SendMessageRequest;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Driving (inbound) adapter — REST API for the chat application.
 * Depends only on the inbound ChatAPI port; never touches the repository directly.
 */
@RestController
@RequestMapping("/api")
public class ChatController {

    private final ChatAPI chatAPI;

    public ChatController(ChatAPI chatAPI) {
        this.chatAPI = chatAPI;
    }

    /**
     * Send a message to a conversation.
     * POST /api/conversations/{conversationId}/messages
     */
    @PostMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<Message> sendMessage(
            @PathVariable long conversationId,
            @RequestBody SendMessageRequest request) throws ChatRepositoryException, InvalidInputException {
        Message message = chatAPI.sendMessage(conversationId, request.getSenderId(), request.getContent());
        return ResponseEntity.status(HttpStatus.CREATED).body(message);
    }

    /**
     * Retrieve all conversations for a user.
     * GET /api/users/{userId}/conversations
     */
    @GetMapping("/users/{userId}/conversations")
    public ResponseEntity<List<Conversation>> getConversations(
            @PathVariable long userId) throws ChatRepositoryException {
        List<Conversation> conversations = chatAPI.getConversationsForUser(userId);
        return ResponseEntity.ok(conversations);
    }

    /**
     * Retrieve messages for a conversation from a given offset.
     * GET /api/conversations/{conversationId}/messages?offsetId=0&limit=50
     */
    @GetMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<List<Message>> getMessages(
            @PathVariable long conversationId,
            @RequestParam(defaultValue = "0") long offsetId,
            @RequestParam(defaultValue = "50") int limit) throws ChatRepositoryException {
        List<Message> messages = chatAPI.getMessages(conversationId, offsetId, limit);
        return ResponseEntity.ok(messages);
    }

    /**
     * Mark all messages in a conversation as read for a given user.
     * PUT /api/conversations/{conversationId}/read
     */
    @PutMapping("/conversations/{conversationId}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable long conversationId,
            @RequestBody MarkAsReadRequest request) throws ChatRepositoryException, InvalidInputException {
        chatAPI.markAsRead(conversationId, request.getUserId());
        return ResponseEntity.noContent().build();
    }
}

