import { TestBed } from '@angular/core/testing';
import { ChatWebsocketService, ChatMessage } from './chat-websocket.service';

/** Mock STOMP client injecté à la place du vrai Client (@stomp/stompjs UMD). */
type MockSubscription = { unsubscribe: jasmine.Spy };

type MockStompClient = {
  connected: boolean;
  active: boolean;
  onConnect?: () => void;
  onDisconnect?: () => void;
  onStompError?: (frame: unknown) => void;
  onWebSocketError?: () => void;
  activate: jasmine.Spy;
  deactivate: jasmine.Spy;
  subscribe: jasmine.Spy;
  publish: jasmine.Spy;
  _subscriptions: Array<{
    destination: string;
    callback: (msg: { body: string }) => void;
  }>;
  _emit: (destination: string, body: unknown) => void;
};

function createMockStompClient() {
  const subscriptions: Array<{
    destination: string;
    callback: (msg: { body: string }) => void;
  }> = [];

  const client: MockStompClient = {
    connected: false,
    active: false,
    onConnect: undefined,
    onDisconnect: undefined,
    onStompError: undefined,
    onWebSocketError: undefined,
    activate: jasmine.createSpy('activate').and.callFake(() => {
      client.active = true;
      client.connected = true;
      client.onConnect?.();
    }),
    deactivate: jasmine.createSpy('deactivate').and.callFake(() => {
      client.active = false;
      client.connected = false;
      client.onDisconnect?.();
    }),
    subscribe: jasmine
      .createSpy('subscribe')
      .and.callFake((destination: string, callback: (msg: { body: string }) => void) => {
        subscriptions.push({ destination, callback });
        return { unsubscribe: jasmine.createSpy('unsubscribe') } as MockSubscription;
      }),
    publish: jasmine.createSpy('publish'),
    _subscriptions: subscriptions,
    _emit(destination: string, body: unknown) {
      const sub = subscriptions.find((s) => s.destination === destination);
      sub?.callback({ body: JSON.stringify(body) });
    },
  };

  return client;
}

describe('ChatWebsocketService', () => {
  let service: ChatWebsocketService;
  let mockClient: ReturnType<typeof createMockStompClient>;

  beforeEach(() => {
    mockClient = createMockStompClient();
    TestBed.configureTestingModule({
      providers: [
        {
          provide: ChatWebsocketService,
          useFactory: () => new ChatWebsocketService(mockClient as unknown as never),
        },
      ],
    });
    service = TestBed.inject(ChatWebsocketService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('connect activates client and subscribes to thread topics', () => {
    const threadId = 'thread-abc';
    service.connect(threadId);

    expect(mockClient.activate).toHaveBeenCalled();
    expect(
      mockClient.subscribe.calls.allArgs().map((args: unknown[]) => args[0] as string)
    ).toContain(`/topic/session/${threadId}`);
    expect(
      mockClient.subscribe.calls.allArgs().map((args: unknown[]) => args[0] as string)
    ).toContain(`/topic/session/${threadId}/status`);
  });

  it('emits session status events from STOMP subscription', (done) => {
    const threadId = 'thread-xyz';
    service.sessionStatus$.subscribe((event) => {
      expect(event.status).toBe('in_progress');
      expect(event.agentName).toBe('Sophie');
      done();
    });

    service.connect(threadId);
    mockClient._emit(`/topic/session/${threadId}/status`, {
      threadId,
      status: 'in_progress',
      agentName: 'Sophie',
    });
  });

  it('sendMessage publishes to /app/chat.send when connected', () => {
    mockClient.connected = true;
    mockClient.active = true;

    const msg: ChatMessage = {
      threadId: 't1',
      senderType: 'customer',
      senderId: 'u1',
      senderName: 'Alice',
      messageText: 'Bonjour',
      messageType: 'text',
    };
    service.sendMessage(msg);

    expect(mockClient.publish).toHaveBeenCalledWith({
      destination: '/app/chat.send',
      body: JSON.stringify(msg),
    });
  });

  it('disconnect deactivates client', () => {
    mockClient.active = true;
    service.connect('t1');
    service.disconnect();
    expect(mockClient.deactivate).toHaveBeenCalled();
  });
});
