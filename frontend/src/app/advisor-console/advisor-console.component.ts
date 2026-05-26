import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { Subscription } from 'rxjs';
import {
  ChatWebsocketService,
  ChatMessage,
  SessionStatusEvent,
} from '../shared/chat-websocket.service';
import { logDebug, logError } from '../core/logger';
import { environment } from '../../environments/environment';

export interface ChatSession {
  id: string;
  customerId: string;
  subject: string;
  status: string;
  createdAt: string;
  assignedAgentId?: string;
  customerFirstName?: string;
  customerLastName?: string;
  customerEmail?: string;
  customerName?: string;
}

export interface MockReservation {
  vehicle: string;
  location: string;
  status: 'completed' | 'upcoming' | 'active';
  date: string;
}

export interface ActiveTab {
  session: ChatSession;
  messages: ChatMessage[];
  isTyping: boolean;
  unread: number;
  customerContext: {
    name: string;
    email: string;
    reservations: MockReservation[];
  };
}

const MAX_CONCURRENT = 3;
const API = environment.apiUrl;

export type MobilePanel = 'queue' | 'chat' | 'context';

@Component({
  selector: 'app-advisor-console',
  standalone: true,
  imports: [CommonModule, FormsModule, HttpClientModule, DatePipe],
  templateUrl: './advisor-console.component.html',
  styleUrls: ['./advisor-console.component.scss']
})
export class AdvisorConsoleComponent implements OnInit, OnDestroy {
  agentId = 'agent-001';
  agentName = 'Sophie Martin';

  // Login mock
  isLoggedIn = false;
  loginError = '';
  loginId = '';
  loginPassword = '';

  // Data
  waitQueue: ChatSession[] = [];
  activeTabs: ActiveTab[] = [];
  currentTabId: string | null = null;

  messageText = '';
  maxConcurrent = MAX_CONCURRENT;
  liveAnnouncement = '';
  mobilePanel: MobilePanel = 'queue';

  private msgSubscription: Subscription | null = null;
  private typingSubscription: Subscription | null = null;
  private queueInterval: any;

  private audioCtx: AudioContext | null = null;
  private lastNotifiedAt = 0;

  constructor(private http: HttpClient, private wsService: ChatWebsocketService) {}

  ngOnInit(): void {
    // L'agent doit se connecter d'abord — pas d'init auto
  }

  // ─── AUTH MOCK ───────────────────────────────────────────────────────────────

  login() {
    // Credentials mock pour le PoC (remplacera JWT en étape 7)
    const validAgents: Record<string, { name: string; password: string }> = {
      'agent-001': { name: 'Sophie Martin', password: 'ycyw2024' },
      'agent-002': { name: 'Thomas Leclerc', password: 'ycyw2024' },
    };
    const agent = validAgents[this.loginId];
    if (agent && agent.password === this.loginPassword) {
      this.agentId = this.loginId;
      this.agentName = agent.name;
      this.isLoggedIn = true;
      this.loginError = '';
      this._initAudioContext();
      this._initAfterLogin();
    } else {
      this.loginError = 'Identifiant ou mot de passe incorrect.';
    }
  }

  private _initAfterLogin(): void {
    this.refreshQueue();
    this.queueInterval = setInterval(() => this.refreshQueue(), 5000);

    // Souscription globale aux messages WebSocket
    this.msgSubscription = this.wsService.messages$.subscribe(msg => {
      const tab = this.activeTabs.find(t => String(t.session.id) === String(msg.threadId));
      if (!tab) return;

      const isDuplicate = tab.messages.some(
        m => m.senderId === msg.senderId
          && m.senderType === msg.senderType
          && m.messageText === msg.messageText
      );
      if (isDuplicate) return;

      tab.messages.push(msg);

      if (msg.senderType === 'customer') {
        if (this.currentTabId !== msg.threadId) {
          tab.unread++;
        }
        this._playNotification();
      }
    });

    this.typingSubscription = this.wsService.typing$.subscribe(indicator => {
      const tab = this.activeTabs.find(t => String(t.session.id) === String(indicator.threadId));
      if (tab) {
        tab.isTyping = indicator.isTyping;
        if (indicator.isTyping && indicator.userType === 'customer') {
          this.announce('Le client est en train d\'écrire');
        }
      }
    });

    this.wsService.sessionStatus$.subscribe((event: SessionStatusEvent) => {
      const tab = this.activeTabs.find(t => String(t.session.id) === String(event.threadId));
      if (tab && event.status === 'closed') {
        tab.session.status = 'closed';
        this.messageText = '';
        this.announce('Le client ne peut plus écrire : conversation clôturée');
      }
    });
  }

  logout() {
    clearInterval(this.queueInterval);
    this.msgSubscription?.unsubscribe();
    this.typingSubscription?.unsubscribe();
    this.wsService.disconnect();
    this.isLoggedIn = false;
    this.waitQueue = [];
    this.activeTabs = [];
    this.currentTabId = null;
    this.mobilePanel = 'queue';
  }

  showMobilePanel(panel: MobilePanel): void {
    this.mobilePanel = panel;
  }

  isMobilePanelActive(panel: MobilePanel): boolean {
    return this.mobilePanel === panel;
  }

  // ─── QUEUE ───────────────────────────────────────────────────────────────────

  refreshQueue() {
    this.http.get<ChatSession[]>(`${API}/api/advisor/queue`).subscribe({
      next: queue => { this.waitQueue = queue; },
      error: err => logError('queue', err)
    });
  }

  getWaitDuration(createdAt: string): string {
    const diffMs = Date.now() - new Date(createdAt).getTime();
    const mins = Math.floor(diffMs / 60000);
    if (mins < 1) return '< 1 min';
    if (mins < 60) return `${mins} min`;
    return `${Math.floor(mins / 60)}h${mins % 60}`;
  }

  // ─── TABS ────────────────────────────────────────────────────────────────────

  assignSession(session: ChatSession) {
    if (this.activeTabs.length >= this.maxConcurrent) {
      alert(`Limite de ${this.maxConcurrent} conversations simultanées atteinte.`);
      return;
    }
    if (this.activeTabs.find(t => t.session.id === session.id)) {
      this.currentTabId = session.id;
      this.showMobilePanel('chat');
      return;
    }

    const assignUrl = `${API}/api/advisor/sessions/${session.id}/assign`
      + `?agentId=${encodeURIComponent(this.agentId)}`
      + `&agentName=${encodeURIComponent(this.agentName)}`;
    this.http.post<ChatSession>(assignUrl, {}).subscribe({
      next: updatedSession => {
        const tab: ActiveTab = {
          session: updatedSession,
          messages: [],
          isTyping: false,
          unread: 0,
          customerContext: this._buildCustomerContext(updatedSession)
        };
        this.activeTabs.push(tab);
        this.currentTabId = updatedSession.id;
        this.wsService.connect(updatedSession.id);
        this.showMobilePanel('chat');
        this.refreshQueue();
      },
      error: err => logError('assign', err)
    });
  }

  selectTab(threadId: string) {
    this.currentTabId = threadId;
    const tab = this.activeTabs.find(t => t.session.id === threadId);
    if (tab) tab.unread = 0;
    this.showMobilePanel('chat');
  }

  closeTab(tab: ActiveTab, event: Event) {
    event.stopPropagation();
    const closeUrl = `${API}/api/advisor/sessions/${tab.session.id}/close`
      + `?agentId=${encodeURIComponent(this.agentId)}`
      + `&agentName=${encodeURIComponent(this.agentName)}`;

    this.http.post(closeUrl, {}).subscribe({
      next: () => {
        this.wsService.unsubscribeThread(tab.session.id);
        this.activeTabs = this.activeTabs.filter(t => t.session.id !== tab.session.id);
        if (this.currentTabId === tab.session.id) {
          this.currentTabId = this.activeTabs.length > 0
            ? this.activeTabs[this.activeTabs.length - 1].session.id
            : null;
          if (!this.currentTabId) {
            this.showMobilePanel('queue');
          }
        }
        this.messageText = '';
        this.announce('Conversation terminée et clôturée pour le client');
        this.refreshQueue();
      },
      error: err => logError('close', err),
    });
  }

  get currentTab(): ActiveTab | null {
    return this.activeTabs.find(t => t.session.id === this.currentTabId) ?? null;
  }

  // ─── MESSAGING ───────────────────────────────────────────────────────────────

  get canWrite(): boolean {
    return !!this.currentTab && this.currentTab.session.status !== 'closed';
  }

  sendMessage() {
    if (!this.canWrite || !this.messageText.trim() || !this.currentTab) return;
    const msg: ChatMessage = {
      threadId: this.currentTab.session.id,
      senderType: 'agent',
      senderId: this.agentId,
      senderName: this.agentName,
      messageText: this.messageText,
      messageType: 'text'
    };
    this.wsService.sendMessage(msg);
    this.messageText = '';
    this.announce('Message envoyé');
  }

  onAdvisorMessageKeydown(event: KeyboardEvent): void {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.sendMessage();
    }
  }

  private announce(message: string): void {
    this.liveAnnouncement = '';
    setTimeout(() => {
      this.liveAnnouncement = message;
    }, 50);
  }

  get totalUnread(): number {
    return this.activeTabs.reduce((sum, tab) => sum + tab.unread, 0);
  }

  getCustomerName(session: ChatSession): string {
    if (session.customerName?.trim()) {
      return session.customerName.trim();
    }
    const parts = [session.customerFirstName, session.customerLastName]
      .filter((p): p is string => !!p?.trim());
    if (parts.length > 0) {
      return parts.join(' ');
    }
    if (session.customerEmail?.trim()) {
      return session.customerEmail.trim();
    }
    return 'Client';
  }

  private _initAudioContext(): void {
    try {
      if (!this.audioCtx) {
        this.audioCtx = new AudioContext();
      }
      if (this.audioCtx.state === 'suspended') {
        void this.audioCtx.resume();
      }
    } catch (e) {
      logDebug('audio', 'Audio indisponible');
    }
  }

  private _playNotification() {
    const now = Date.now();
    if (now - this.lastNotifiedAt < 300) return;
    this.lastNotifiedAt = now;

    try {
      this._initAudioContext();
      if (!this.audioCtx) return;

      const osc = this.audioCtx.createOscillator();
      const gain = this.audioCtx.createGain();
      osc.connect(gain);
      gain.connect(this.audioCtx.destination);
      osc.type = 'sine';
      osc.frequency.setValueAtTime(880, this.audioCtx.currentTime);
      gain.gain.setValueAtTime(0.1, this.audioCtx.currentTime);
      gain.gain.exponentialRampToValueAtTime(0.0001, this.audioCtx.currentTime + 0.3);
      osc.start(this.audioCtx.currentTime);
      osc.stop(this.audioCtx.currentTime + 0.3);
    } catch (e) {
      logDebug('audio', 'Audio indisponible');
    }
  }

  // ─── MOCK DATA ────────────────────────────────────────────────────────────────

  private _buildCustomerContext(session: ChatSession): ActiveTab['customerContext'] {
    const name = this.getCustomerName(session);
    const email = session.customerEmail?.trim()
      || `client-${session.customerId.substring(0, 6)}@example.com`;
    return {
      name,
      email,
      reservations: [
        { vehicle: 'Peugeot 208', location: 'Paris CDG', status: 'completed', date: '15 mai 2026' },
        { vehicle: 'Tesla Model 3', location: 'Lyon Perrache', status: 'upcoming', date: '02 juin 2026' },
      ],
    };
  }

  ngOnDestroy(): void {
    clearInterval(this.queueInterval);
    this.msgSubscription?.unsubscribe();
    this.typingSubscription?.unsubscribe();
  }
}
