import {
  Component,
  OnInit,
  ViewChild,
  ElementRef,
  AfterViewChecked,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClientModule, HttpClient } from '@angular/common/http';
import {
  ChatWebsocketService,
  ChatMessage,
  SessionStatusEvent,
} from '../shared/chat-websocket.service';
import { logError } from '../core/logger';
import { environment } from '../../environments/environment';

@Component({
  selector: 'app-chat-widget',
  standalone: true,
  imports: [CommonModule, FormsModule, HttpClientModule],
  templateUrl: './chat-widget.component.html',
  styleUrls: ['./chat-widget.component.scss'],
})
export class ChatWidgetComponent implements OnInit, AfterViewChecked {
  email = '';
  firstName = '';
  lastName = '';
  subject = '';
  queuePosition = 0;
  estimatedWaitTime = 0;
  sessionStatus = 'waiting';
  assignedAgentName = '';
  isSessionCreated = false;

  threadId = '';
  userId = Math.random().toString(36).substring(7);
  messageText = '';
  messages: ChatMessage[] = [];
  isConnected = false;
  isSomeoneTyping = false;
  typingTimeout: ReturnType<typeof setTimeout> | null = null;

  liveAnnouncement = '';
  private shouldScrollToBottom = false;
  private wasTyping = false;

  @ViewChild('firstNameInput') firstNameInput?: ElementRef<HTMLInputElement>;
  @ViewChild('messageInput') messageInput?: ElementRef<HTMLTextAreaElement>;
  @ViewChild('messagesContainer') messagesContainer?: ElementRef<HTMLDivElement>;

  constructor(
    private chatService: ChatWebsocketService,
    private http: HttpClient
  ) {}

  get canWrite(): boolean {
    return this.isConnected && this.sessionStatus !== 'closed';
  }

  get isClosed(): boolean {
    return this.sessionStatus === 'closed';
  }

  ngOnInit(): void {
    setTimeout(() => this.firstNameInput?.nativeElement.focus(), 0);

    this.chatService.isConnected$.subscribe(connected => {
      this.isConnected = connected;
      if (connected && this.isSessionCreated && this.canWrite) {
        setTimeout(() => this.messageInput?.nativeElement.focus(), 0);
      }
    });

    this.chatService.messages$.subscribe(msg => {
      if (String(msg.threadId) !== String(this.threadId)) return;

      this.messages.push(msg);
      this.shouldScrollToBottom = true;

      if (msg.senderType !== 'customer') {
        const preview = msg.messageText.length > 60
          ? msg.messageText.substring(0, 60) + '…'
          : msg.messageText;
        this.announce(`Nouveau message de ${msg.senderName} : ${preview}`);
      }
    });

    this.chatService.typing$.subscribe(typing => {
      if (
        String(typing.threadId) !== String(this.threadId) ||
        typing.userId === this.userId
      ) {
        return;
      }
      this.isSomeoneTyping = typing.isTyping;
      if (typing.isTyping && !this.wasTyping) {
        this.announce('Le conseiller est en train d\'écrire');
      }
      this.wasTyping = typing.isTyping;
    });

    this.chatService.sessionStatus$.subscribe((event: SessionStatusEvent) => {
      if (String(event.threadId) === String(this.threadId)) {
        this.onSessionStatusUpdate(event);
      }
    });
  }

  ngAfterViewChecked(): void {
    if (this.shouldScrollToBottom) {
      this.scrollMessagesToBottom();
      this.shouldScrollToBottom = false;
    }
  }

  private onSessionStatusUpdate(event: SessionStatusEvent): void {
    this.sessionStatus = event.status;
    if (event.status === 'in_progress') {
      this.queuePosition = 0;
      this.assignedAgentName = event.agentName ?? 'un conseiller';
      this.announce(`${this.assignedAgentName} a rejoint la conversation`);
      setTimeout(() => this.messageInput?.nativeElement.focus(), 0);
    } else if (event.status === 'closed') {
      this.queuePosition = 0;
      this.isSomeoneTyping = false;
      this.messageText = '';
      this.announce('Conversation terminée. Vous ne pouvez plus envoyer de messages.');
      this.sendTyping(false);
    }
  }

  startChatSession(): void {
    const payload = {
      email: this.email,
      firstName: this.firstName,
      lastName: this.lastName,
      subject: this.subject,
    };

    this.http
      .post<{
        threadId: string;
        queuePosition: number;
        estimatedWaitTimeMinutes: number;
        status: string;
      }>(`${environment.apiUrl}/api/chat/sessions`, payload)
      .subscribe({
        next: res => {
          this.threadId = res.threadId;
          this.queuePosition = res.queuePosition;
          this.estimatedWaitTime = res.estimatedWaitTimeMinutes;
          this.sessionStatus = res.status ?? 'waiting';
          this.isSessionCreated = true;
          this.chatService.connect(this.threadId);
          this.announce('Session de tchat démarrée. En attente d\'un conseiller.');
          setTimeout(() => this.messageInput?.nativeElement.focus(), 100);
        },
        error: err => {
          logError('session', err);
          this.announce('Erreur lors de la création de la session de tchat.');
        },
      });
  }

  disconnect(): void {
    this.chatService.disconnect();
    this.announce('Déconnecté du tchat');
  }

  sendMessage(): void {
    if (!this.canWrite || !this.messageText.trim()) return;

    const msg: ChatMessage = {
      threadId: this.threadId,
      senderType: 'customer',
      senderId: this.userId,
      senderName: `${this.firstName} ${this.lastName}`.trim() || 'Client Anonyme',
      messageText: this.messageText.trim(),
      messageType: 'text',
    };
    this.chatService.sendMessage(msg);
    this.messageText = '';
    this.sendTyping(false);
    this.announce('Message envoyé');
    this.shouldScrollToBottom = true;
    setTimeout(() => this.messageInput?.nativeElement.focus(), 0);
  }

  onMessageKeydown(event: KeyboardEvent): void {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.sendMessage();
    }
  }

  onTyping(): void {
    if (!this.canWrite) return;
    this.sendTyping(true);
    if (this.typingTimeout) clearTimeout(this.typingTimeout);
    this.typingTimeout = setTimeout(() => this.sendTyping(false), 2000);
  }

  private sendTyping(isTyping: boolean): void {
    if (!this.threadId) return;
    this.chatService.sendTypingIndicator({
      threadId: this.threadId,
      userId: this.userId,
      userType: 'customer',
      isTyping,
    });
  }

  private announce(message: string): void {
    this.liveAnnouncement = '';
    setTimeout(() => {
      this.liveAnnouncement = message;
    }, 50);
  }

  private scrollMessagesToBottom(): void {
    const el = this.messagesContainer?.nativeElement;
    if (el) {
      el.scrollTop = el.scrollHeight;
    }
  }
}
