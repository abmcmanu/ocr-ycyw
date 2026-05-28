import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { AdvisorConsoleComponent, ChatSession } from './advisor-console.component';
import { ChatWebsocketService } from '../shared/chat-websocket.service';
import { AuthService } from '../core/auth.service';
import { Subject } from 'rxjs';

describe('AdvisorConsoleComponent', () => {
  let component: AdvisorConsoleComponent;
  let fixture: ComponentFixture<AdvisorConsoleComponent>;

  beforeEach(async () => {
    const wsMock = {
      connect: jasmine.createSpy('connect'),
      disconnect: jasmine.createSpy('disconnect'),
      messages$: new Subject(),
      typing$: new Subject(),
      sessionStatus$: new Subject(),
      isConnected$: new Subject<boolean>(),
    };

    await TestBed.configureTestingModule({
      imports: [AdvisorConsoleComponent, HttpClientTestingModule],
      providers: [
        { provide: ChatWebsocketService, useValue: wsMock },
        {
          provide: AuthService,
          useValue: {
            loginAdvisor: jasmine
              .createSpy('loginAdvisor')
              .and.returnValue(Promise.resolve()),
            logout: jasmine.createSpy('logout'),
            token: null,
          },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(AdvisorConsoleComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('starts logged out', () => {
    expect(component.isLoggedIn).toBeFalse();
  });

  describe('getCustomerName', () => {
    const base: ChatSession = {
      id: '1',
      customerId: 'c1',
      subject: 'Test',
      status: 'waiting',
      createdAt: new Date().toISOString(),
    };

    it('returns customerName when set', () => {
      expect(
        component.getCustomerName({ ...base, customerName: '  Marie Curie  ' })
      ).toBe('Marie Curie');
    });

    it('returns first and last name when customerName absent', () => {
      expect(
        component.getCustomerName({
          ...base,
          customerFirstName: 'Jean',
          customerLastName: 'Dupont',
        })
      ).toBe('Jean Dupont');
    });

    it('returns email as fallback', () => {
      expect(
        component.getCustomerName({ ...base, customerEmail: 'jean@test.com' })
      ).toBe('jean@test.com');
    });

    it('returns "Client" when no identity fields', () => {
      expect(component.getCustomerName(base)).toBe('Client');
    });
  });

  it('getWaitDuration formats minutes', () => {
    const fiveMinAgo = new Date(Date.now() - 5 * 60 * 1000).toISOString();
    expect(component.getWaitDuration(fiveMinAgo)).toBe('5 min');
  });

  it('totalUnread sums tab unread counts', () => {
    component.activeTabs = [
      {
        session: { id: '1', customerId: 'c', subject: 's', status: 'in_progress', createdAt: '' },
        messages: [],
        isTyping: false,
        unread: 2,
        customerContext: { name: 'A', email: 'a@b.com', reservations: [] },
      },
      {
        session: { id: '2', customerId: 'c', subject: 's', status: 'in_progress', createdAt: '' },
        messages: [],
        isTyping: false,
        unread: 3,
        customerContext: { name: 'B', email: 'b@b.com', reservations: [] },
      },
    ];
    expect(component.totalUnread).toBe(5);
  });
});
