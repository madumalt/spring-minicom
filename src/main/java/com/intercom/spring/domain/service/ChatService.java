package com.intercom.spring.domain.service;

import com.intercom.spring.domain.exception.InvalidInputException;
import com.intercom.spring.domain.models.Conversation;
import com.intercom.spring.domain.models.Message;
import com.intercom.spring.domain.exception.ChatRepositoryException;
import com.intercom.spring.ports.inbound.ChatAPI;
import com.intercom.spring.ports.outbound.ChatRepository;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Domain service — implements the inbound ChatAPI port.
 * Contains business rules and orchestrates calls to the outbound port.
 * This is the core of the hexagon.
 */
@Service
public class ChatService implements ChatAPI {

    private static final int MAX_MESSAGE_LENGTH = 5000;
    private static final int MAX_PAGE_SIZE = 100;

    private final ChatRepository chatRepository;

    public ChatService(ChatRepository chatRepository) {
        this.chatRepository = chatRepository;
    }

    @Override
    public Message sendMessage(long conversationId, long senderId, String content)
        throws ChatRepositoryException, InvalidInputException {
        validateMessageContent(content);
        validateParticipant(conversationId, senderId);

        return chatRepository.saveMessage(conversationId, senderId, content);
    }

    @Override
    public List<Conversation> getConversationsForUser(long userId) throws ChatRepositoryException {
        return chatRepository.findConversationsByUserId(userId);
    }

    @Override
    public List<Message> getMessages(long conversationId, long offsetId, int limit) throws ChatRepositoryException {
        int safeLimit = Math.max(1, Math.min(limit, MAX_PAGE_SIZE));
        return chatRepository.findMessages(conversationId, offsetId, safeLimit);
    }

    @Override
    public void markAsRead(long conversationId, long userId) throws ChatRepositoryException, InvalidInputException {
        validateParticipant(conversationId, userId);
        chatRepository.markMessagesAsRead(conversationId, userId);
    }

    private void validateMessageContent(String content) throws InvalidInputException {
        if (content == null || content.isBlank()) {
            throw new InvalidInputException("Message content cannot be empty");
        }
        if (content.length() > MAX_MESSAGE_LENGTH) {
            throw new InvalidInputException("Message content exceeds maximum length of " + MAX_MESSAGE_LENGTH);
        }
    }

    private void validateParticipant(long conversationId, long userId) throws InvalidInputException, ChatRepositoryException {
        if (!chatRepository.isParticipant(conversationId, userId)) {
            throw new InvalidInputException("User " + userId + " is not a participant of conversation " + conversationId);
        }
    }
}


