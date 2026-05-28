import { ComponentFixture, TestBed } from '@angular/core/testing';
import {
  HttpClientTestingModule,
  HttpTestingController,
} from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { ProfileHistoryComponent } from './profile-history.component';
import { AuthService } from '../core/auth.service';
import { environment } from '../../environments/environment';

describe('ProfileHistoryComponent', () => {
  let component: ProfileHistoryComponent;
  let fixture: ComponentFixture<ProfileHistoryComponent>;
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProfileHistoryComponent, HttpClientTestingModule],
      providers: [
        provideRouter([]),
        {
          provide: AuthService,
          useValue: {
            token: 'jwt',
            role: 'CLIENT',
            customerId: 'cust-1',
          },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ProfileHistoryComponent);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('loads history on init when authenticated', () => {
    fixture.detectChanges();

    const req = httpMock.expectOne(
      (r) =>
        r.url === `${environment.apiUrl}/api/chat/sessions/history` &&
        r.params.get('page') === '0'
    );
    expect(req.request.method).toBe('GET');
    req.flush({
      content: [
        {
          threadId: 't1',
          subject: 'Question',
          status: 'closed',
          createdAt: '2024-01-01T00:00:00Z',
          messageCount: 5,
        },
      ],
      totalElements: 1,
      totalPages: 1,
      number: 0,
      size: 10,
    });

    expect(component.history.length).toBe(1);
    expect(component.history[0].subject).toBe('Question');
    expect(component.loading).toBeFalse();
  });

  it('shows error when not authenticated', async () => {
    TestBed.resetTestingModule();
    await TestBed.configureTestingModule({
      imports: [ProfileHistoryComponent, HttpClientTestingModule],
      providers: [
        provideRouter([]),
        {
          provide: AuthService,
          useValue: { token: null, role: null },
        },
      ],
    }).compileComponents();

    const f = TestBed.createComponent(ProfileHistoryComponent);
    const c = f.componentInstance;
    const mock = TestBed.inject(HttpTestingController);
    f.detectChanges();

    expect(c.error).toContain('Connectez-vous');
    mock.expectNone(`${environment.apiUrl}/api/chat/sessions/history`);
  });
});
