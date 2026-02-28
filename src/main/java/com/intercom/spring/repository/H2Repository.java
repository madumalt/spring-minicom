package com.intercom.spring.repository;

import com.intercom.spring.domain.exception.UserRepositoryException;
import com.intercom.spring.domain.models.Conversation;
import com.intercom.spring.domain.models.Message;
import com.intercom.spring.domain.models.User;
import com.intercom.spring.domain.exception.ChatRepositoryException;
import com.intercom.spring.ports.outbound.ChatRepository;
import com.intercom.spring.ports.outbound.UserRepository;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

/**
 * Driven (outbound) adapter — single H2 implementation of both
 * ChatRepository and UserRepository outbound ports.
 */
@Repository
public class H2Repository implements ChatRepository, UserRepository {

  private static final RowMapper<User> userRowMapper = (rs, rowNum) -> new User(
      rs.getLong("id"),
      rs.getString("username"),
      rs.getString("email"),
      rs.getString("status"),
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

  public H2Repository(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  // ── User operations (UserRepository) ──

  @Override
  public User saveUser(String username, String email) throws UserRepositoryException {
    String sql = "INSERT INTO users (username, email) VALUES (?, ?)";
    jdbcTemplate.update(sql, username, email);
    Long id = jdbcTemplate.queryForObject("SELECT MAX(id) FROM users", Long.class);
    return jdbcTemplate.queryForObject("SELECT * FROM users WHERE id = ?", userRowMapper, id);
  }

  @Override
  public boolean existsByUsername(String username) throws UserRepositoryException {
    String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
    Integer count = jdbcTemplate.queryForObject(sql, Integer.class, username);
    return count != null && count > 0;
  }

  @Override
  public boolean existsByEmail(String email) throws UserRepositoryException {
    String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
    Integer count = jdbcTemplate.queryForObject(sql, Integer.class, email);
    return count != null && count > 0;
  }

  @Override
  public User findByUsername(String username) throws UserRepositoryException {
    String sql = "SELECT * FROM users WHERE username = ?";
    List<User> users = jdbcTemplate.query(sql, userRowMapper, username);
    return users.isEmpty() ? null : users.get(0);
  }

  @Override
  public List<User> searchUsers(String query) throws UserRepositoryException {
    String sql = "SELECT * FROM users WHERE LOWER(username) LIKE ? LIMIT 20";
    return jdbcTemplate.query(sql, userRowMapper, "%" + query.toLowerCase() + "%");
  }

  // ── Conversation operations (ChatRepository) ──

  @Override
  public long createConversation(String type, String name, List<Long> participantIds) throws ChatRepositoryException {
    jdbcTemplate.update(
        "INSERT INTO conversations (type, name, created_at, updated_at) VALUES (?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
        type, name);
    Long convId = jdbcTemplate.queryForObject("SELECT MAX(id) FROM conversations", Long.class);
    if (convId == null) {
      throw new ChatRepositoryException("Failed to create conversation");
    }
    for (Long userId : participantIds) {
      jdbcTemplate.update("INSERT INTO conversation_participants (conversation_id, user_id) VALUES (?, ?)", convId, userId);
    }
    return convId;
  }

  @Override
  public Long findExistingPrivateConversation(long userId1, long userId2) throws ChatRepositoryException {
    String sql = "SELECT cp1.conversation_id FROM conversation_participants cp1 "
        + "JOIN conversation_participants cp2 ON cp1.conversation_id = cp2.conversation_id "
        + "JOIN conversations c ON c.id = cp1.conversation_id "
        + "WHERE cp1.user_id = ? AND cp2.user_id = ? AND c.type = 'private'";
    List<Long> ids = jdbcTemplate.queryForList(sql, Long.class, userId1, userId2);
    return ids.isEmpty() ? null : ids.get(0);
  }

  @Override
  public List<Conversation> findConversationsByUserId(long userId) throws ChatRepositoryException {
    String sql = "SELECT c.* FROM conversations c "
        + "JOIN conversation_participants cp ON c.id = cp.conversation_id "
        + "WHERE cp.user_id = ? ORDER BY c.updated_at DESC";
    List<Conversation> conversations = jdbcTemplate.query(sql, (rs, rowNum) -> {
      long convId = rs.getLong("id");
      try {
        return new Conversation(
            convId,
            rs.getString("type"),
            rs.getString("name"),
            getParticipantUsernames(convId),
            getUnreadCount(convId, userId),
            rs.getTimestamp("created_at"),
            rs.getTimestamp("updated_at")
        );
      } catch (ChatRepositoryException e) {
        throw new RuntimeException(e);
      }
    }, userId);
    return conversations;
  }

  @Override
  public List<String> getParticipantUsernames(long conversationId) throws ChatRepositoryException {
    String sql = "SELECT u.username FROM users u "
        + "JOIN conversation_participants cp ON u.id = cp.user_id "
        + "WHERE cp.conversation_id = ?";
    return jdbcTemplate.queryForList(sql, String.class, conversationId);
  }

  @Override
  public int getUnreadCount(long conversationId, long userId) throws ChatRepositoryException {
    String sql = "SELECT COUNT(*) FROM message_receipts mr "
        + "JOIN messages m ON mr.message_id = m.id "
        + "WHERE m.conversation_id = ? AND mr.recipient_id = ? AND mr.is_read = FALSE";
    Integer count = jdbcTemplate.queryForObject(sql, Integer.class, conversationId, userId);
    return count != null ? count : 0;
  }

  @Override
  public boolean isParticipant(long conversationId, long userId) throws ChatRepositoryException {
    String sql = "SELECT COUNT(*) FROM conversation_participants WHERE conversation_id = ? AND user_id = ?";
    Integer count = jdbcTemplate.queryForObject(sql, Integer.class, conversationId, userId);
    return count != null && count > 0;
  }

  // ── Message operations (ChatRepository) ──

  @Override
  public Message saveMessage(long conversationId, long senderId, String content) throws ChatRepositoryException {
    String sql = "INSERT INTO messages (conversation_id, sender_id, content, created_at) VALUES (?, ?, ?, CURRENT_TIMESTAMP)";
    jdbcTemplate.update(sql, conversationId, senderId, content);

    long messageId = getLastInsertedMessageId();

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

