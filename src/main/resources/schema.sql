-- Users table: stores registered users
CREATE TABLE IF NOT EXISTS users (
  id INTEGER PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(64) NOT NULL UNIQUE,
  email VARCHAR(128) NOT NULL UNIQUE,
  status VARCHAR(16) DEFAULT 'offline',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Conversations table: represents a 1-to-1 chat thread between two users
CREATE TABLE IF NOT EXISTS conversations (
  id INTEGER PRIMARY KEY AUTO_INCREMENT,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Conversation participants: links exactly two users to a conversation
CREATE TABLE IF NOT EXISTS conversation_participants (
  id INTEGER PRIMARY KEY AUTO_INCREMENT,
  conversation_id INTEGER NOT NULL,
  user_id INTEGER NOT NULL,
  joined_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (conversation_id) REFERENCES conversations(id),
  FOREIGN KEY (user_id) REFERENCES users(id),
  UNIQUE (conversation_id, user_id)
);

-- Messages table: each message belongs to a conversation and has a sender
CREATE TABLE IF NOT EXISTS messages (
  id INTEGER PRIMARY KEY AUTO_INCREMENT,
  conversation_id INTEGER NOT NULL,
  sender_id INTEGER NOT NULL,
  content VARCHAR(5000) NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (conversation_id) REFERENCES conversations(id),
  FOREIGN KEY (sender_id) REFERENCES users(id)
);

-- Message receipts: tracks delivery and read status per recipient
CREATE TABLE IF NOT EXISTS message_receipts (
  id INTEGER PRIMARY KEY AUTO_INCREMENT,
  message_id INTEGER NOT NULL,
  recipient_id INTEGER NOT NULL,
  delivered BOOLEAN DEFAULT FALSE,
  delivered_at DATETIME,
  is_read BOOLEAN DEFAULT FALSE,
  read_at DATETIME,
  FOREIGN KEY (message_id) REFERENCES messages(id),
  FOREIGN KEY (recipient_id) REFERENCES users(id),
  UNIQUE (message_id, recipient_id)
);

-- Index for fast lookups of messages in a conversation
CREATE INDEX IF NOT EXISTS idx_messages_conversation ON messages(conversation_id, created_at);

-- Index for fast lookups of unread receipts for a user
CREATE INDEX IF NOT EXISTS idx_receipts_recipient ON message_receipts(recipient_id, is_read);
