import { TestBed } from '@angular/core/testing';
import {
  HttpClientTestingModule,
  HttpTestingController,
} from '@angular/common/http/testing';
import { AuthService } from './auth.service';
import { environment } from '../../environments/environment';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    sessionStorage.clear();
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
    });
    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
    sessionStorage.clear();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('loginClient stores token and role', async () => {
    const promise = service.loginClient('client@example.com');
    const req = httpMock.expectOne(`${environment.apiUrl}/api/auth/client`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ email: 'client@example.com' });
    req.flush({
      accessToken: 'client-jwt',
      tokenType: 'Bearer',
      role: 'CLIENT',
      customerId: 'cust-1',
    });

    const res = await promise;
    expect(res.accessToken).toBe('client-jwt');
    expect(service.token).toBe('client-jwt');
    expect(service.role).toBe('CLIENT');
    expect(service.customerId).toBe('cust-1');
  });

  it('loginAdvisor stores token and role', async () => {
    const promise = service.loginAdvisor('agent-001', 'ycyw2024');
    const req = httpMock.expectOne(`${environment.apiUrl}/api/auth/advisor`);
    expect(req.request.body).toEqual({ agentId: 'agent-001', password: 'ycyw2024' });
    req.flush({
      accessToken: 'advisor-jwt',
      tokenType: 'Bearer',
      role: 'ADVISOR',
      agentId: 'agent-001',
    });

    await promise;
    expect(service.token).toBe('advisor-jwt');
    expect(service.role).toBe('ADVISOR');
  });

  it('logout clears session storage', async () => {
    const promise = service.loginClient('a@b.com');
    httpMock.expectOne(`${environment.apiUrl}/api/auth/client`).flush({
      accessToken: 't',
      tokenType: 'Bearer',
      role: 'CLIENT',
    });
    await promise;

    service.logout();
    expect(service.token).toBeNull();
    expect(service.role).toBeNull();
  });
});
