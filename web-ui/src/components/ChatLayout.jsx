import { useState, useEffect, useCallback } from 'react';
import { getConversations, searchUsers, createConversation } from '../api';
import ChatPanel from './ChatPanel';

export default function ChatLayout({ user, onLogout }) {
  const [conversations, setConversations] = useState([]);
  const [activeConv, setActiveConv] = useState(null);
  const [searchQuery, setSearchQuery] = useState('');
  const [searchResults, setSearchResults] = useState([]);
  const [chatOpen, setChatOpen] = useState(false);

  const loadConversations = useCallback(async () => {
    try {
      const convs = await getConversations(user.id);
      setConversations(convs);
      setActiveConv((prev) => {
        if (!prev) return prev;
        const updated = convs.find((c) => c.id === prev.id);
        return updated || prev;
      });
    } catch (err) {
      console.error('Failed to load conversations', err);
    }
  }, [user.id]);

  useEffect(() => {
    loadConversations();
    const interval = setInterval(loadConversations, 5000);
    return () => clearInterval(interval);
  }, [loadConversations]);

  useEffect(() => {
    if (!searchQuery.trim()) {
      setSearchResults([]);
      return;
    }
    const timeout = setTimeout(async () => {
      try {
        const users = await searchUsers(searchQuery);
        setSearchResults(users.filter((u) => u.id !== user.id));
      } catch {
        setSearchResults([]);
      }
    }, 300);
    return () => clearTimeout(timeout);
  }, [searchQuery, user.id]);

  const handleSelectUser = async (otherUser) => {
    setSearchQuery('');
    setSearchResults([]);
    try {
      const conv = await createConversation('private', [user.id, otherUser.id]);
      await loadConversations();
      setActiveConv(conv);
      setChatOpen(true);
    } catch (err) {
      console.error('Failed to create conversation', err);
    }
  };

  const handleSelectConv = (conv) => {
    setActiveConv(conv);
    setChatOpen(true);
  };

  const handleBack = () => {
    setChatOpen(false);
    setActiveConv(null);
    loadConversations();
  };

  const getDisplayName = (conv) => {
    if (conv.type === 'group' && conv.name) {
      return conv.name;
    }
    if (conv.participant_usernames && conv.participant_usernames.length > 0) {
      const partner = conv.participant_usernames.find((u) => u !== user.username);
      if (partner) return partner;
    }
    return `Chat #${conv.id}`;
  };

  const formatTime = (ts) => {
    if (!ts) return '';
    const d = new Date(ts);
    const now = new Date();
    if (d.toDateString() === now.toDateString()) {
      return d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    }
    return d.toLocaleDateString([], { month: 'short', day: 'numeric' });
  };

  return (
    <div className={`chat-layout ${chatOpen ? 'chat-open' : ''}`}>
      <div className="sidebar">
        <div className="sidebar-header">
          <div className="user-info">
            <div className="avatar">{user.username[0].toUpperCase()}</div>
            <span className="username">{user.username}</span>
          </div>
          <button className="btn-logout" onClick={onLogout}>Logout</button>
        </div>

        <div className="sidebar-search">
          <input
            type="text"
            placeholder="Search users to start a chat..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
          />
        </div>

        {searchResults.length > 0 && (
          <div className="search-results">
            <div className="search-label">Users</div>
            {searchResults.map((u) => (
              <div key={u.id} className="search-item" onClick={() => handleSelectUser(u)}>
                <div className="avatar-sm">{u.username[0].toUpperCase()}</div>
                <span>{u.username}</span>
              </div>
            ))}
          </div>
        )}

        <div className="conversation-list">
          {conversations.length === 0 ? (
            <div className="no-conversations">
              No conversations yet.<br />Search for a user to start chatting.
            </div>
          ) : (
            conversations.map((conv) => (
              <div
                key={conv.id}
                className={`conversation-item ${activeConv?.id === conv.id ? 'active' : ''}`}
                onClick={() => handleSelectConv(conv)}
              >
                <div className="avatar-md">
                  {getDisplayName(conv)[0].toUpperCase()}
                </div>
                <div className="conv-info">
                  <div className="conv-name">{getDisplayName(conv)}</div>
                  <div className="conv-time">{formatTime(conv.updated_at)}</div>
                </div>
                {conv.unread_count > 0 && (
                  <div className="unread-badge">{conv.unread_count}</div>
                )}
              </div>
            ))
          )}
        </div>
      </div>

      <div className="chat-panel">
        {activeConv ? (
          <ChatPanel
            conversation={activeConv}
            user={user}
            partnerName={getDisplayName(activeConv)}
            onBack={handleBack}
            onMessagesRead={loadConversations}
          />
        ) : (
          <div className="chat-panel-empty">
            Select a conversation to start chatting
          </div>
        )}
      </div>
    </div>
  );
}

