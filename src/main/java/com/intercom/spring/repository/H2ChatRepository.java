package com.intercom.spring.repository;

import com.intercom.spring.domain.models.Conversation;
import com.intercom.spring.domain.models.Message;
import com.intercom.spring.domain.exception.ChatRepositoryException;
import com.intercom.spring.ports.outbound.ChatRepository;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

/**
 * Driven (outbound) adapter — H2 implementation of the ChatRepository.
 * Pure data-access only; no business logic belongs here.
 */
@Repository
public class H2ChatRepository implements ChatRepository {

  private static final RowMapper<Conversation> conversationRowMapper = (rs, rowNum) -> new Conversation(
      rs.getLong("id"),
      rs.getTimestamp("created_at"),
      rs.getTimestamp("updated_at")
  );

  private static final RowMapper<Message> messageRowMapper = (rs, rowNum) -> new Message(
      rs.getLong("id"),
      rs.getLong("conversation_id"),
      rs.getLong("sender_id"),
      rs.getString("content"),
      rs.getTimestamp("created_at")
  );

  private final JdbcTemplate jdbcTemplate;

  public H2ChatRepository(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  // ── Conversation operations ──

  @Override
  public List<Conversation> findConversationsByUserId(long userId) throws ChatRepositoryException {
    String sql = "SELECT c.* FROM conversations c "
        + "JOIN conversation_participants cp ON c.id = cp.conversation_id "
        + "WHERE cp.user_id = ? ORDER BY c.updated_at DESC";
    return jdbcTemplate.query(sql, conversationRowMapper, userId);
  }

  @Override
  public boolean isParticipant(long conversationId, long userId) throws ChatRepositoryException {
    String sql = "SELECT COUNT(*) FROM conversation_participants WHERE conversation_id = ? AND user_id = ?";
    Integer count = jdbcTemplate.queryForObject(sql, Integer.class, conversationId, userId);
    return count != null && count > 0;
  }

  // ── Message operations ──

  @Override
  public Message saveMessage(long conversationId, long senderId, String content) throws ChatRepositoryException {
    String sql = "INSERT INTO messages (conversation_id, sender_id, content, created_at) VALUES (?, ?, ?, CURRENT_TIMESTAMP)";
    jdbcTemplate.update(sql, conversationId, senderId, content);

    long messageId = getLastInsertedMessageId();

    // Create receipts for the other participant(s)
    List<Long> recipients = getOtherParticipants(conversationId, senderId);
    for (Long recipientId : recipients) {
      createReceipt(messageId, recipientId);
    }

    updateConversationTimestamp(conversationId);

    return new Message(messageId, conversationId, senderId, content, null);
  }

  @Override
  public List<Message> findMessages(long conversationId, long offsetId, int limit) throws ChatRepositoryException {
    String sql = "SELECT * FROM messages WHERE conversation_id = ? AND id > ? ORDER BY created_at ASC LIMIT ?";
    return jdbcTemplate.query(sql, messageRowMapper, conversationId, offsetId, limit);
  }

  @Override
  public void markMessagesAsRead(long conversationId, long recipientId) throws ChatRepositoryException {
    String sql = "UPDATE message_receipts SET is_read = TRUE, read_at = CURRENT_TIMESTAMP "
        + "WHERE recipient_id = ? AND is_read = FALSE AND message_id IN "
        + "(SELECT id FROM messages WHERE conversation_id = ?)";
    jdbcTemplate.update(sql, recipientId, conversationId);
  }

  // ── Private helpers (adapter implementation details) ──

  private long getLastInsertedMessageId() throws ChatRepositoryException {
    Long id = jdbcTemplate.queryForObject("SELECT MAX(id) FROM messages", Long.class);
    if (id == null) {
      throw new ChatRepositoryException("Failed to retrieve last inserted message ID");
    }
    return id;
  }

  private List<Long> getOtherParticipants(long conversationId, long excludeUserId) {
    String sql = "SELECT user_id FROM conversation_participants WHERE conversation_id = ? AND user_id != ?";
    return jdbcTemplate.queryForList(sql, Long.class, conversationId, excludeUserId);
  }

  private void createReceipt(long messageId, long recipientId) {
    String sql = "INSERT INTO message_receipts (message_id, recipient_id, delivered, is_read) VALUES (?, ?, FALSE, FALSE)";
    jdbcTemplate.update(sql, messageId, recipientId);
  }

  private void updateConversationTimestamp(long conversationId) {
    jdbcTemplate.update("UPDATE conversations SET updated_at = CURRENT_TIMESTAMP WHERE id = ?", conversationId);
  }
}
