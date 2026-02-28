const BASE = '/api';

async function request(url, options = {}) {
  const { headers: extraHeaders, ...rest } = options;
  const res = await fetch(BASE + url, {
    headers: { 'Content-Type': 'application/json', ...extraHeaders },
    ...rest,
  });
  if (!res.ok) {
    const body = await res.text();
    throw new Error(body || res.statusText);
  }
  const text = await res.text();
  return text ? JSON.parse(text) : null;
}

export function signUp(username, email) {
  return request(`/users/signup?username=${encodeURIComponent(username)}&email=${encodeURIComponent(email)}`, {
    method: 'POST',
  });
}

export function login(username) {
  return request(`/users/login?username=${encodeURIComponent(username)}`, {
    method: 'POST',
  });
}

export function searchUsers(query) {
  return request(`/users/search?query=${encodeURIComponent(query)}`);
}

export function getConversations(userId) {
  return request(`/users/${userId}/conversations`);
}

export function createConversation(type, participantIds, name = null) {
  const body = { type, participant_ids: participantIds };
  if (name) body.name = name;
  return request('/conversations', {
    method: 'POST',
    body: JSON.stringify(body),
  });
}

export function getMessages(conversationId, offsetId = 0, limit = 50) {
  return request(`/conversations/${conversationId}/messages?offsetId=${offsetId}&limit=${limit}`);
}

export function sendMessage(conversationId, senderId, content) {
  return request(`/conversations/${conversationId}/messages`, {
    method: 'POST',
    body: JSON.stringify({ sender_id: senderId, content }),
  });
}

export function markAsRead(conversationId, userId) {
  return request(`/conversations/${conversationId}/read`, {
    method: 'PUT',
    body: JSON.stringify({ user_id: userId }),
  });
}

