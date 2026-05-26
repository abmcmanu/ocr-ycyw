import { Injectable, isDevMode } from '@angular/core';
import type { Client, Message, StompSubscription } from '@stomp/stompjs';
import { BehaviorSubject, Subject } from 'rxjs';
import { logDebug, logError } from '../core/logger';
import { apiUrlToBrokerUrl } from '../core/ws-url';
import { environment } from '../../environments/environment';

export interface ChatMessage {
  threadId: string;
  senderType: string;
  senderId: string;
  senderName: string;
  messageText: string;
  messageType: string;
}

export interface TypingIndicator {
  threadId: string;
  userId: string;
  userType: string;
  isTyping: boolean;
}

export interface SessionStatusEvent {
  threadId: string;
  status: string;
  agentId?: string;
  agentName?: string;
}

@Injectable({
  providedIn: 'root'
})
export class ChatWebsocketService {
  private stompClient: Client | null = null;
  private clientReady: Promise<Client> | null = null;

  private messageSubject = new Subject<ChatMessage>();
  public messages$ = this.messageSubject.asObservable();

  private typingSubject = new Subject<TypingIndicator>();
  public typing$ = this.typingSubject.asObservable();

  private sessionStatusSubject = new Subject<SessionStatusEvent>();
  public sessionStatus$ = this.sessionStatusSubject.asObservable();

  private connectionStateSubject = new BehaviorSubject<boolean>(false);
  public isConnected$ = this.connectionStateSubject.asObservable();

  private subscribedThreads = new Set<string>();
  private threadSubscriptions = new Map<string, StompSubscription[]>();

  /** STOMP chargé à la demande (évite sockjs-client et ses listeners `unload`). */
  private ensureClient(): Promise<Client> {
    if (!this.clientReady) {
      this.clientReady = this.createClient();
    }
    return this.clientReady;
  }

  private async createClient(): Promise<Client> {
    const { Client } = await import('@stomp/stompjs');
    const client = new Client({
      brokerURL: apiUrlToBrokerUrl(environment.apiUrl),
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      debug: isDevMode() ? (msg) => logDebug('STOMP', msg) : () => undefined,
    });

    client.onConnect = () => {
      logDebug('WS', 'Connecté au broker STOMP');
      this.connectionStateSubject.next(true);
      this.subscribedThreads.forEach(threadId => this._subscribeToThread(client, threadId));
    };

    client.onDisconnect = () => {
      logDebug('WS', 'Déconnecté');
      this.connectionStateSubject.next(false);
      this.threadSubscriptions.clear();
    };

    client.onStompError = (frame) => {
      logError('WS', frame);
    };

    client.onWebSocketError = () => {
      logError('WS', 'WebSocket error');
    };

    this.stompClient = client;
    return client;
  }

  public async connect(threadId: string): Promise<void> {
    this.subscribedThreads.add(threadId);
    const client = await this.ensureClient();

    if (!client.active) {
      client.activate();
      return;
    }

    if (client.connected) {
      this._subscribeToThread(client, threadId);
    }
  }

  private _subscribeToThread(client: Client, threadId: string): void {
    if (this.threadSubscriptions.has(threadId)) {
      return;
    }

    const subs: StompSubscription[] = [];

    subs.push(
      client.subscribe(`/topic/session/${threadId}`, (message: Message) => {
        if (message.body) {
          this.messageSubject.next(JSON.parse(message.body) as ChatMessage);
        }
      })
    );

    subs.push(
      client.subscribe(`/topic/session/${threadId}/typing`, (message: Message) => {
        if (message.body) {
          this.typingSubject.next(JSON.parse(message.body) as TypingIndicator);
        }
      })
    );

    subs.push(
      client.subscribe(`/topic/session/${threadId}/status`, (message: Message) => {
        if (message.body) {
          this.sessionStatusSubject.next(JSON.parse(message.body) as SessionStatusEvent);
        }
      })
    );

    this.threadSubscriptions.set(threadId, subs);
  }

  public unsubscribeThread(threadId: string): void {
    this.subscribedThreads.delete(threadId);
    const subs = this.threadSubscriptions.get(threadId);
    if (subs) {
      subs.forEach(s => s.unsubscribe());
      this.threadSubscriptions.delete(threadId);
    }
  }

  public disconnect(): void {
    const client = this.stompClient;
    if (client?.active) {
      this.threadSubscriptions.forEach(subs => subs.forEach(s => s.unsubscribe()));
      this.threadSubscriptions.clear();
      this.subscribedThreads.clear();
      client.deactivate();
    }
  }

  public sendMessage(msg: ChatMessage): void {
    if (this.stompClient?.connected) {
      this.stompClient.publish({
        destination: '/app/chat.send',
        body: JSON.stringify(msg)
      });
    }
  }

  public sendTypingIndicator(indicator: TypingIndicator): void {
    if (this.stompClient?.connected) {
      this.stompClient.publish({
        destination: '/app/chat.typing',
        body: JSON.stringify(indicator)
      });
    }
  }
}
