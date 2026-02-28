import { useState, useEffect, useRef, useCallback } from 'react';
import { getMessages, sendMessage, markAsRead } from '../api';

export default function ChatPanel({ conversation, user, partnerName, onBack, onMessagesRead }) {
  const [messages, setMessages] = useState([]);
  const [input, setInput] = useState('');
  const [sending, setSending] = useState(false);
  const messagesEndRef = useRef(null);
  const containerRef = useRef(null);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  const loadMessages = useCallback(async () => {
    try {
      const msgs = await getMessages(conversation.id, 0, 200);
      setMessages(msgs);
    } catch (err) {
      console.error('Failed to load messages', err);
    }
  }, [conversation.id]);

  // Mark as read when conversation opens or new messages arrive
  const doMarkAsRead = useCallback(async () => {
    try {
      await markAsRead(conversation.id, user.id);
      onMessagesRead();
    } catch {
      // ignore — user may not have unread messages
    }
  }, [conversation.id, user.id, onMessagesRead]);

  useEffect(() => {
    loadMessages();
    doMarkAsRead();
    const interval = setInterval(() => {
      loadMessages();
      doMarkAsRead();
    }, 3000);
    return () => clearInterval(interval);
  }, [loadMessages, doMarkAsRead]);

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  const handleSend = async (e) => {
    e.preventDefault();
    const text = input.trim();
    if (!text || sending) return;
    setSending(true);
    setInput('');
    try {
      const newMsg = await sendMessage(conversation.id, user.id, text);
      setMessages((prev) => [...prev, newMsg]);
      scrollToBottom();
    } catch (err) {
      console.error('Failed to send message', err);
      setInput(text);
    } finally {
      setSending(false);
    }
  };

  const formatTime = (ts) => {
    if (!ts) return '';
    return new Date(ts).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  };

  return (
    <>
      <div className="chat-header">
        <button className="btn-back" onClick={onBack}>
          ←
        </button>
        <div className="avatar-md">{partnerName[0].toUpperCase()}</div>
        <span className="chat-partner-name">{partnerName}</span>
      </div>

      <div className="messages-container" ref={containerRef}>
        {messages.map((msg) => (
          <div
            key={msg.id}
            className={`message-bubble ${msg.sender_id === user.id ? 'sent' : 'received'}`}
          >
            <div>{msg.content}</div>
            <div className="msg-time">{formatTime(msg.created_at)}</div>
          </div>
        ))}
        <div ref={messagesEndRef} />
      </div>

      <form className="message-input-area" onSubmit={handleSend}>
        <input
          type="text"
          placeholder="Type a message..."
          value={input}
          onChange={(e) => setInput(e.target.value)}
          autoFocus
        />
        <button className="btn-send" type="submit" disabled={!input.trim() || sending}>
          Send
        </button>
      </form>
    </>
  );
}

