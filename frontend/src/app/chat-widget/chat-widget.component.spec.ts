import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import {
  HttpClientTestingModule,
  HttpTestingController,
} from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { Subject } from 'rxjs';
import { ChatWidgetComponent } from './chat-widget.component';
import {
  ChatWebsocketService,
  SessionStatusEvent,
} from '../shared/chat-websocket.service';
import { AuthService } from '../core/auth.service';
import { environment } from '../../environments/environment';

describe('ChatWidgetComponent', () => {
  let component: ChatWidgetComponent;
  let fixture: ComponentFixture<ChatWidgetComponent>;
  let httpMock: HttpTestingController;
  let wsMock: {
    connect: jasmine.Spy;
    disconnect: jasmine.Spy;
    sendMessage: jasmine.Spy;
    sendTypingIndicator: jasmine.Spy;
    isConnected$: Subject<boolean>;
    messages$: Subject<unknown>;
    typing$: Subject<unknown>;
    sessionStatus$: Subject<SessionStatusEvent>;
  };
  let authMock: { loginClient: jasmine.Spy };

  beforeEach(async () => {
    wsMock = {
      connect: jasmine.createSpy('connect'),
      disconnect: jasmine.createSpy('disconnect'),
      sendMessage: jasmine.createSpy('sendMessage'),
      sendTypingIndicator: jasmine.createSpy('sendTypingIndicator'),
      isConnected$: new Subject<boolean>(),
      messages$: new Subject(),
      typing$: new Subject(),
      sessionStatus$: new Subject<SessionStatusEvent>(),
    };
    authMock = {
      loginClient: jasmine.createSpy('loginClient').and.returnValue(Promise.resolve()),
    };

    await TestBed.configureTestingModule({
      imports: [ChatWidgetComponent, HttpClientTestingModule],
      providers: [
        provideRouter([]),
        { provide: ChatWebsocketService, useValue: wsMock },
        { provide: AuthService, useValue: authMock },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ChatWidgetComponent);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
    fixture.detectChanges();
  });

  afterEach(() => {
    httpMock.verify();
    component.ngOnDestroy();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('canWrite is false when not connected', () => {
    component.isConnected = false;
    component.sessionStatus = 'in_progress';
    expect(component.canWrite).toBeFalse();
  });

  it('canWrite is true when connected and session in progress', () => {
    component.isConnected = true;
    component.sessionStatus = 'in_progress';
    expect(component.canWrite).toBeTrue();
  });

  it('startChatSession connects WebSocket and starts polling', () => {
    component.email = 'test@example.com';
    component.firstName = 'Jean';
    component.lastName = 'Dupont';
    component.subject = 'Aide';

    component.startChatSession();

    const req = httpMock.expectOne(`${environment.apiUrl}/api/chat/sessions`);
    expect(req.request.method).toBe('POST');
    req.flush({
      threadId: 'thread-1',
      queuePosition: 1,
      estimatedWaitTimeMinutes: 2,
      status: 'waiting',
    });

    expect(component.threadId).toBe('thread-1');
    expect(component.isSessionCreated).toBeTrue();
    expect(wsMock.connect).toHaveBeenCalledWith('thread-1');
    expect(authMock.loginClient).toHaveBeenCalledWith('test@example.com');
  });

  it('updates UI when session status becomes in_progress via WebSocket', fakeAsync(() => {
    component.threadId = 'thread-1';
    component.isSessionCreated = true;
    component.sessionStatus = 'waiting';
    component.queuePosition = 2;

    wsMock.sessionStatus$.next({
      threadId: 'thread-1',
      status: 'in_progress',
      agentName: 'Sophie Martin',
    });
    tick(60);

    expect(component.sessionStatus).toBe('in_progress');
    expect(component.queuePosition).toBe(0);
    expect(component.assignedAgentName).toBe('Sophie Martin');
  }));

  it('sendMessage delegates to websocket service when allowed', () => {
    component.threadId = 'thread-1';
    component.isConnected = true;
    component.sessionStatus = 'in_progress';
    component.firstName = 'Jean';
    component.lastName = 'Dupont';
    component.messageText = 'Bonjour';

    component.sendMessage();

    expect(wsMock.sendMessage).toHaveBeenCalled();
    expect(component.messageText).toBe('');
  });

  it('disconnect stops websocket', () => {
    component.disconnect();
    expect(wsMock.disconnect).toHaveBeenCalled();
  });
});
