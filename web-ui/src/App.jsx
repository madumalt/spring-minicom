import { useState } from 'react';
import LoginPage from './components/LoginPage';
import ChatLayout from './components/ChatLayout';
import './App.css';

function App() {
  const [user, setUser] = useState(null);

  const handleLogout = () => setUser(null);

  if (!user) {
    return <LoginPage onLogin={setUser} />;
  }

  return <ChatLayout user={user} onLogout={handleLogout} />;
}

export default App;
