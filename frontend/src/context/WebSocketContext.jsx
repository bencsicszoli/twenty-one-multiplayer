import { createContext, useContext, useEffect, useState, useRef } from "react";
import SockJS from "sockjs-client";
import { over } from "stompjs";
import { usePlayer } from "./PlayerContext.jsx";

const WebSocketContext = createContext(null);

export function WebSocketProvider({ children }) {
  const { token } = usePlayer();
  const [connected, setConnected] = useState(false);
  const stompClientRef = useRef(null);

  // Queue a deferred feliratkozásokhoz
  const subscriptionQueueRef = useRef([]);

  useEffect(() => {
    if (!token) {
      console.warn("No JWT token found. WebSocket connection aborted.");
      return;
    }

    const socket = new SockJS("http://localhost:8080/ws");
    const client = over(socket);
    stompClientRef.current = client;

    client.connect(
      { Authorization: "Bearer " + token },
      () => {
        console.log("✅ WebSocket connected");
        setConnected(true);

        // Lefuttatjuk a deferred feliratkozásokat
        subscriptionQueueRef.current.forEach(({ destination, callback }) => {
          client.subscribe(destination, callback);
        });
        subscriptionQueueRef.current = [];
      },
      (err) => console.error("❌ WebSocket error:", err)
    );

    return () => {
      if (stompClientRef.current && stompClientRef.current.connected) {
        stompClientRef.current.disconnect(() =>
          console.log("🔌 WebSocket disconnected cleanly")
        );
      }
      setConnected(false);
    };
  }, [token]);

  // Helper: Feliratkozás
  function subscribe(destination, callback) {
    const client = stompClientRef.current;

    if (client && client.connected) {
      return client.subscribe(destination, callback);
    } else {
      console.warn("Not connected yet. Subscription deferred.");
      subscriptionQueueRef.current.push({ destination, callback });
      return { unsubscribe: () => {} }; // dummy unsubscribe
    }
  }

  // Helper: Üzenet küldése
  function send(destination, body) {
    const client = stompClientRef.current;

    if (client && client.connected) {
      client.send(destination, {}, JSON.stringify(body));
    } else {
      console.warn("Not connected yet. Message not sent.");
    }
  }

  return (
    <WebSocketContext.Provider value={{ connected, subscribe, send }}>
      {children}
    </WebSocketContext.Provider>
  );
}

// eslint-disable-next-line react-refresh/only-export-components
export function useWebSocket() {
  return useContext(WebSocketContext);
}
