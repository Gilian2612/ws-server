import { useState, useEffect, useRef } from 'react';
import { useWebSocket } from './useWebSocket';
import './Chat.css';

export default function Chat({ username }) {
  const { messages, connected, userCount, sendMessage } = useWebSocket();
  const [input, setInput] = useState('');
  const bottomRef = useRef(null);

  // Auto-scroll al último mensaje
  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  const handleSend = () => {
    if (!input.trim()) return;
    sendMessage(username, input.trim());
    setInput('');
  };

  const handleKey = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSend();
    }
  };

  return (
    <div className="chat-container">

      {/* ── Barra superior ── */}
      <header className="chat-header">
        <div className="header-left">
          <span className={`status-dot ${connected ? 'online' : 'offline'}`}/>
          <h1>ChatWS</h1>
        </div>
        <div className="header-right">
          <span className="user-count">👥 {userCount}</span>
          <span className="username-badge">{username}</span>
        </div>
      </header>

      {/* ── Lista de mensajes ── */}
      <main className="chat-messages">
        {messages.map((msg) => (
          <div
            key={msg.id}
            className={`message ${
              msg.type === 'system' ? 'system' :
              msg.username === username ? 'mine' : 'theirs'
            }`}
          >
            {msg.type !== 'system' && (
              <div className="msg-meta">
                <span className="msg-author">{msg.username}</span>
                <span className="msg-time">
                  {new Date(msg.timestamp).toLocaleTimeString()}
                </span>
              </div>
            )}
            <div className="msg-bubble">{msg.text}</div>
          </div>
        ))}
        <div ref={bottomRef} />
      </main>

      {/* ── Input ── */}
      <footer className="chat-footer">
        <input
          type="text"
          value={input}
          onChange={(e) => setInput(e.target.value)}
          onKeyDown={handleKey}
          placeholder="Escribe un mensaje... (Enter para enviar)"
          disabled={!connected}
          autoFocus
        />
        <button
          onClick={handleSend}
          disabled={!connected || !input.trim()}
        >
          Enviar
        </button>
      </footer>

    </div>
  );
}
