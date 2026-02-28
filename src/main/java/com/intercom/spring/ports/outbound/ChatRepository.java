package com.intercom.spring.ports.outbound;

import com.intercom.spring.domain.models.Conversation;
import com.intercom.spring.domain.models.Message;
import com.intercom.spring.domain.exception.ChatRepositoryException;
import java.util.List;

/**
 * Outbound (driven) port — defines what the domain needs from persistence.
 * Implemented by infrastructure adapters (e.g. H2Repository).
 */
public interface ChatRepository {

  long createConversation(String type, String name, List<Long> participantIds) throws ChatRepositoryException;
  Long findExistingPrivateConversation(long userId1, long userId2) throws ChatRepositoryException;

  List<Conversation> findConversationsByUserId(long userId) throws ChatRepositoryException;
  List<String> getParticipantUsernames(long conversationId) throws ChatRepositoryException;
  int getUnreadCount(long conversationId, long userId) throws ChatRepositoryException;
  boolean isParticipant(long conversationId, long userId) throws ChatRepositoryException;

  Message saveMessage(long conversationId, long senderId, String content) throws ChatRepositoryException;
  List<Message> findMessages(long conversationId, long offsetId, int limit) throws ChatRepositoryException;

  void markMessagesAsRead(long conversationId, long recipientId) throws ChatRepositoryException;
}

