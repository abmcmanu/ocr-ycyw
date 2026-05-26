import { Injectable } from '@angular/core';
import { Client, Message } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { BehaviorSubject, Subject } from 'rxjs';

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

@Injectable({
  providedIn: 'root'
})
export class ChatWebsocketService {
  private stompClient: Client;
  
  // Observables pour que les composants UI y souscrivent
  private messageSubject = new Subject<ChatMessage>();
  public messages$ = this.messageSubject.asObservable();

  private typingSubject = new Subject<TypingIndicator>();
  public typing$ = this.typingSubject.asObservable();

  private connectionStateSubject = new BehaviorSubject<boolean>(false);
  public isConnected$ = this.connectionStateSubject.asObservable();

  constructor() {
    this.stompClient = new Client({
      // En l'absence d'un broker complet en dev, on utilise SockJS
      webSocketFactory: () => new SockJS('http://localhost:8081/ws'),
      reconnectDelay: 5000, // Reconnexion automatique (fallback)
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
    });

    this.stompClient.onConnect = (frame) => {
      console.log('Connecté à WebSocket via STOMP', frame);
      this.connectionStateSubject.next(true);
    };

    this.stompClient.onDisconnect = (frame) => {
      console.log('Déconnecté de WebSocket', frame);
      this.connectionStateSubject.next(false);
    };

    this.stompClient.onStompError = (frame) => {
      console.error('Erreur STOMP', frame);
    };
  }

  public connect(threadId: string): void {
    if (!this.stompClient.active) {
      this.stompClient.activate();
    }
    
    // Souscription aux messages dès la connexion
    this.stompClient.onConnect = (frame) => {
      this.connectionStateSubject.next(true);
      
      this.stompClient.subscribe(`/topic/session/${threadId}`, (message: Message) => {
        if (message.body) {
          const chatMsg: ChatMessage = JSON.parse(message.body);
          this.messageSubject.next(chatMsg);
        }
      });

      this.stompClient.subscribe(`/topic/session/${threadId}/typing`, (message: Message) => {
        if (message.body) {
          const typingMsg: TypingIndicator = JSON.parse(message.body);
          this.typingSubject.next(typingMsg);
        }
      });
    };
  }

  public disconnect(): void {
    if (this.stompClient.active) {
      this.stompClient.deactivate();
    }
  }

  public sendMessage(msg: ChatMessage): void {
    if (this.stompClient.connected) {
      this.stompClient.publish({
        destination: '/app/chat.send',
        body: JSON.stringify(msg)
      });
    }
  }

  public sendTypingIndicator(indicator: TypingIndicator): void {
    if (this.stompClient.connected) {
      this.stompClient.publish({
        destination: '/app/chat.typing',
        body: JSON.stringify(indicator)
      });
    }
  }
}
