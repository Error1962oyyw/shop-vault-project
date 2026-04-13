import { ref, onUnmounted } from 'vue';
import { secureStorage } from '@/utils/secureStorage';

interface WebSocketOptions {
  onMessage?: (data: unknown) => void;
  onConnect?: () => void;
  onDisconnect?: () => void;
}

export function useWebSocket(options: WebSocketOptions = {}) {
  const connected = ref(false);
  let ws: WebSocket | null = null;
  let reconnectAttempts = 0;
  let reconnectTimer: ReturnType<typeof setTimeout> | null = null;
  const MAX_RECONNECT_ATTEMPTS = 5;
  const RECONNECT_DELAY = 5000;

  const connect = () => {
    const token = secureStorage.getItem('token');
    if (!token) return;

    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    const host = window.location.host;
    const wsUrl = `${protocol}//${host}/ws?token=${encodeURIComponent(token)}`;

    try {
      ws = new WebSocket(wsUrl);

      ws.onopen = () => {
        connected.value = true;
        reconnectAttempts = 0;
        options.onConnect?.();
      };

      ws.onmessage = (event) => {
        try {
          const data = JSON.parse(event.data);
          options.onMessage?.(data);
        } catch {
          options.onMessage?.(event.data);
        }
      };

      ws.onclose = () => {
        connected.value = false;
        options.onDisconnect?.();
        attemptReconnect();
      };

      ws.onerror = () => {
        ws?.close();
      };
    } catch {
      attemptReconnect();
    }
  };

  const attemptReconnect = () => {
    if (reconnectAttempts >= MAX_RECONNECT_ATTEMPTS) return;
    reconnectAttempts++;
    reconnectTimer = setTimeout(connect, RECONNECT_DELAY);
  };

  const disconnect = () => {
    if (reconnectTimer) {
      clearTimeout(reconnectTimer);
      reconnectTimer = null;
    }
    if (ws) {
      ws.onclose = null;
      ws.close();
      ws = null;
      connected.value = false;
    }
  };

  const send = (data: unknown) => {
    if (ws && ws.readyState === WebSocket.OPEN) {
      ws.send(JSON.stringify(data));
    }
  };

  onUnmounted(() => {
    disconnect();
  });

  return {
    connected,
    connect,
    disconnect,
    send
  };
}
