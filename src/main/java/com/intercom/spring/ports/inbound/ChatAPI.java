package com.intercom.spring.ports.inbound;

import com.intercom.spring.domain.exception.InvalidInputException;
import com.intercom.spring.domain.models.Conversation;
import com.intercom.spring.domain.models.Message;
import com.intercom.spring.domain.exception.ChatRepositoryException;
import java.util.List;

/**
 * Inbound (driving) port — defines all use cases for the chat application.
 * Called by driving adapters (e.g. REST controller).
 * Implemented by the domain service.
 */
public interface ChatAPI {

  Conversation createConversation(String type, String name, List<Long> participantIds)
      throws ChatRepositoryException, InvalidInputException;

  Message sendMessage(long conversationId, long senderId, String content)
      throws ChatRepositoryException, InvalidInputException;

  List<Conversation> getConversationsForUser(long userId) throws ChatRepositoryException;

  List<Message> getMessages(long conversationId, long offsetId, int limit) throws ChatRepositoryException;

  void markAsRead(long conversationId, long userId) throws ChatRepositoryException, InvalidInputException;
}

