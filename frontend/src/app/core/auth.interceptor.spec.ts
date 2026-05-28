import { TestBed } from '@angular/core/testing';
import {
  HttpClient,
  provideHttpClient,
  withInterceptors,
} from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { authInterceptor } from './auth.interceptor';
import { AuthService } from './auth.service';

describe('authInterceptor', () => {
  let http: HttpClient;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    sessionStorage.clear();
    TestBed.configureTestingModule({
      providers: [
        AuthService,
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
      ],
    });
    http = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
    sessionStorage.clear();
  });

  it('adds Authorization header for /api/ requests when token exists', () => {
    sessionStorage.setItem('ycyw_auth_token', 'my-token');

    http.get(`${'http://localhost:8081'}/api/chat/sessions/history`).subscribe();

    const req = httpMock.expectOne(
      'http://localhost:8081/api/chat/sessions/history'
    );
    expect(req.request.headers.get('Authorization')).toBe('Bearer my-token');
    req.flush({ content: [], totalPages: 0 });
  });

  it('does not add Authorization when token is missing', () => {
    http.get('http://localhost:8081/api/auth/client').subscribe();

    const req = httpMock.expectOne('http://localhost:8081/api/auth/client');
    expect(req.request.headers.has('Authorization')).toBeFalse();
    req.flush({});
  });

  it('does not add Authorization for non-api URLs', () => {
    sessionStorage.setItem('ycyw_auth_token', 'my-token');

    http.get('/assets/foo.json').subscribe();

    const req = httpMock.expectOne('/assets/foo.json');
    expect(req.request.headers.has('Authorization')).toBeFalse();
    req.flush({});
  });
});
