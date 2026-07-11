import { useState, useEffect, useRef, useCallback } from 'react';

const WS_URL = 'ws://localhost:8080/chat';

export function useWebSocket() {
  // Estado principal
  const [messages, setMessages] = useState([]);
  const [connected, setConnected] = useState(false);
  const [userCount, setUserCount] = useState(0);

  // useRef para acceder al socket sin re-renders
  const wsRef = useRef(null);

  useEffect(() => {
    // Crear la conexión WebSocket
    const ws = new WebSocket(WS_URL);
    wsRef.current = ws;

    ws.onopen = () => {
      console.log('✅ Conectado al servidor WS');
      setConnected(true);
    };

    ws.onmessage = (event) => {
      const msg = JSON.parse(event.data);

      if (msg.type === 'system') {
        // Actualizar contador de usuarios
        setUserCount(msg.count);
        // Agregar como mensaje de sistema en el chat
        setMessages((prev) => [...prev, { ...msg, id: Date.now() }]);
      } else if (msg.type === 'message') {
        setMessages((prev) => [...prev, { ...msg, id: Date.now() }]);
      }
    };

    ws.onclose = () => {
      console.log('❌ Desconectado del servidor WS');
      setConnected(false);
    };

    ws.onerror = (e) => console.error('WS Error:', e);

    // Cleanup: cerrar socket al desmontar el componente
    return () => ws.close();
  }, []); // [] = solo se ejecuta una vez al montar

  // Función para enviar mensajes al servidor
  const sendMessage = useCallback((username, text) => {
    if (wsRef.current?.readyState === WebSocket.OPEN) {
      wsRef.current.send(JSON.stringify({ username, text }));
    }
  }, []);

  return { messages, connected, userCount, sendMessage };
}
