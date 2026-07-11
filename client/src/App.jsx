import { useState } from 'react';
import Chat from './Chat';

export default function App() {
  const [username, setUsername] = useState('');
  const [entered, setEntered] = useState(false);

  const handleEnter = () => {
    if (username.trim().length >= 2) setEntered(true);
  };

  if (entered) return <Chat username={username} />;

  return (
    <div style={{
      display: 'flex', flexDirection: 'column',
      alignItems: 'center', justifyContent: 'center',
      height: '100vh', gap: '16px',
      background: '#0d1117', color: '#e6edf3'
    }}>
      <h2 style={{ fontSize: '1.5rem', fontWeight: 700 }}>
        Entrar al Chat
      </h2>
      <input
        type="text"
        placeholder="Tu nombre de usuario"
        value={username}
        onChange={(e) => setUsername(e.target.value)}
        onKeyDown={(e) => e.key === 'Enter' && handleEnter()}
        autoFocus
        style={{
          padding: '12px 16px', borderRadius: '8px',
          border: '1px solid #30363d', background: '#161b22',
          color: '#e6edf3', fontSize: '16px', width: '280px',
          outline: 'none'
        }}
      />
      <button
        onClick={handleEnter}
        disabled={username.trim().length < 2}
        style={{
          padding: '12px 28px', borderRadius: '8px',
          border: 'none', background: '#58a6ff',
          color: '#0d1117', fontWeight: 700,
          fontSize: '15px', cursor: 'pointer'
        }}
      >
        Entrar
      </button>
    </div>
  );
}
