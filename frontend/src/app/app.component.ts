import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ChatWebsocketService, ChatMessage, TypingIndicator } from './shared/chat-websocket.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit {
  threadId = '550e8400-e29b-41d4-a716-446655440000'; // ID de thread fixe pour le POC
  userId = Math.random().toString(36).substring(7);
  messageText = '';
  messages: ChatMessage[] = [];
  isConnected = false;
  isSomeoneTyping = false;
  typingTimeout: any;

  constructor(private chatService: ChatWebsocketService) {}

  ngOnInit() {
    this.chatService.isConnected$.subscribe(connected => this.isConnected = connected);
    
    this.chatService.messages$.subscribe(msg => {
      this.messages.push(msg);
    });

    this.chatService.typing$.subscribe(typing => {
      if (typing.userId !== this.userId) {
        this.isSomeoneTyping = typing.isTyping;
      }
    });
  }

  connect() {
    this.chatService.connect(this.threadId);
  }

  disconnect() {
    this.chatService.disconnect();
  }

  sendMessage() {
    if (this.messageText.trim()) {
      const msg: ChatMessage = {
        threadId: this.threadId,
        senderType: 'customer',
        senderId: this.userId,
        senderName: 'Client ' + this.userId,
        messageText: this.messageText,
        messageType: 'text'
      };
      this.chatService.sendMessage(msg);
      this.messageText = '';
      this.sendTyping(false);
    }
  }

  onTyping() {
    this.sendTyping(true);
    clearTimeout(this.typingTimeout);
    this.typingTimeout = setTimeout(() => this.sendTyping(false), 2000);
  }

  private sendTyping(isTyping: boolean) {
    this.chatService.sendTypingIndicator({
      threadId: this.threadId,
      userId: this.userId,
      userType: 'customer',
      isTyping: isTyping
    });
  }
}
