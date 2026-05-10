import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import { getToken } from "@/utils/auth";

const WS_URL =
  (import.meta.env.VITE_API_URL || "http://localhost:8080") + "/ws";

let client: Client | null = null;

export type StompMessage = {
  type: string;
  bookingId: number;
  studentName?: string;
  coachName?: string;
  message?: string;
  timestamp: number;
};

export function connect(): Promise<Client> {
  return new Promise((resolve, reject) => {
    if (client?.connected) {
      resolve(client);
      return;
    }
    if (client) {
      client.deactivate();
    }

    const token = getToken()?.accessToken || "";

    client = new Client({
      webSocketFactory: () => new SockJS(WS_URL),
      connectHeaders: {
        Authorization: "Bearer " + token
      },
      reconnectDelay: 5000,
      onConnect: () => {
        resolve(client!);
      },
      onStompError: frame => {
        console.error("STOMP error", frame);
        reject(frame.headers["message"] || "STOMP connection failed");
      }
    });

    client.activate();
  });
}

export function disconnect() {
  if (client?.connected) {
    client.deactivate();
  }
  client = null;
}

export async function subscribe(
  destination: string,
  callback: (msg: StompMessage) => void
) {
  const stompClient = await connect();
  return stompClient.subscribe(destination, message => {
    try {
      callback(JSON.parse(message.body));
    } catch {
      // ignore parse errors
    }
  });
}

export function isConnected(): boolean {
  return client?.connected ?? false;
}
