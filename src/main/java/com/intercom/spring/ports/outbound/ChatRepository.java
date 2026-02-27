package com.intercom.spring.ports.outbound;

import com.intercom.spring.domain.models.Conversation;
import com.intercom.spring.domain.models.Message;
import com.intercom.spring.domain.exception.ChatRepositoryException;
import java.util.List;

/**
 * Outbound (driven) port — defines what the domain needs from persistence.
 * Implemented by infrastructure adapters (e.g. H2ChatRepository).
 */
public interface ChatRepository {

  List<Conversation> findConversationsByUserId(long userId) throws ChatRepositoryException;
  boolean isParticipant(long conversationId, long userId) throws ChatRepositoryException;

  Message saveMessage(long conversationId, long senderId, String content) throws ChatRepositoryException;
  List<Message> findMessages(long conversationId, long offsetId, int limit) throws ChatRepositoryException;

  void markMessagesAsRead(long conversationId, long recipientId) throws ChatRepositoryException;
}

