import { Injectable, isDevMode } from '@angular/core';
import { Client, Message, StompSubscription } from '@stomp/stompjs';
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
  providedIn: 'root',
})
export class ChatWebsocketService {
  private readonly stompClient: Client;

  private messageSubject = new Subject<ChatMessage>();
  public messages$ = this.messageSubject.asObservable();

  private typingSubject = new Subject<TypingIndicator>();
  public typing$ = this.typingSubject.asObservable();

  private sessionStatusSubject = new Subject<SessionStatusEvent>();
  public sessionStatus$ = this.sessionStatusSubject.asObservable();

  private connectionStateSubject = new BehaviorSubject<boolean>(false);
  public isConnected$ = this.connectionStateSubject.asObservable();

  private readonly subscribedThreads = new Set<string>();
  private readonly threadSubscriptions = new Map<string, StompSubscription[]>();

  constructor(stompClient?: Client) {
    this.stompClient = stompClient ?? new Client({
      brokerURL: apiUrlToBrokerUrl(environment.apiUrl),
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      debug: isDevMode() ? (msg) => logDebug('STOMP', msg) : () => undefined,
    });

    this.stompClient.onConnect = () => {
      logDebug('WS', 'Connecté au broker STOMP');
      this.connectionStateSubject.next(true);
      this.subscribedThreads.forEach((threadId) =>
        this.subscribeToThread(threadId)
      );
    };

    this.stompClient.onDisconnect = () => {
      logDebug('WS', 'Déconnecté');
      this.connectionStateSubject.next(false);
      this.threadSubscriptions.clear();
    };

    this.stompClient.onStompError = (frame) => {
      logError('WS', frame);
    };

    this.stompClient.onWebSocketError = () => {
      logError('WS', 'WebSocket error');
    };
  }

  connect(threadId: string): void {
    this.subscribedThreads.add(threadId);

    if (this.stompClient.connected) {
      this.subscribeToThread(threadId);
      return;
    }

    if (!this.stompClient.active) {
      this.stompClient.activate();
    }
  }

  private subscribeToThread(threadId: string): void {
    if (this.threadSubscriptions.has(threadId)) {
      return;
    }

    const subs: StompSubscription[] = [];

    subs.push(
      this.stompClient.subscribe(`/topic/session/${threadId}`, (message: Message) => {
        if (message.body) {
          this.messageSubject.next(JSON.parse(message.body) as ChatMessage);
        }
      })
    );

    subs.push(
      this.stompClient.subscribe(`/topic/session/${threadId}/typing`, (message: Message) => {
        if (message.body) {
          this.typingSubject.next(JSON.parse(message.body) as TypingIndicator);
        }
      })
    );

    subs.push(
      this.stompClient.subscribe(`/topic/session/${threadId}/status`, (message: Message) => {
        if (message.body) {
          this.sessionStatusSubject.next(
            JSON.parse(message.body) as SessionStatusEvent
          );
        }
      })
    );

    this.threadSubscriptions.set(threadId, subs);
  }

  unsubscribeThread(threadId: string): void {
    this.subscribedThreads.delete(threadId);
    const subs = this.threadSubscriptions.get(threadId);
    if (subs) {
      subs.forEach((s) => s.unsubscribe());
      this.threadSubscriptions.delete(threadId);
    }
  }

  disconnect(): void {
    if (this.stompClient.active) {
      this.threadSubscriptions.forEach((subs) => subs.forEach((s) => s.unsubscribe()));
      this.threadSubscriptions.clear();
      this.subscribedThreads.clear();
      this.stompClient.deactivate();
    }
  }

  sendMessage(msg: ChatMessage): void {
    if (this.stompClient.connected) {
      this.stompClient.publish({
        destination: '/app/chat.send',
        body: JSON.stringify(msg),
      });
    }
  }

  sendTypingIndicator(indicator: TypingIndicator): void {
    if (this.stompClient.connected) {
      this.stompClient.publish({
        destination: '/app/chat.typing',
        body: JSON.stringify(indicator),
      });
    }
  }
}
