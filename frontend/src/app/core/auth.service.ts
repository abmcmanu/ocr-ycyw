import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { firstValueFrom } from 'rxjs';

export interface AuthTokenResponse {
  accessToken: string;
  tokenType: string;
  role: 'CLIENT' | 'ADVISOR';
  customerId?: string;
  agentId?: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly storageKey = 'ycyw_auth_token';

  constructor(private http: HttpClient) {}

  get token(): string | null {
    return sessionStorage.getItem(this.storageKey);
  }

  get role(): string | null {
    return sessionStorage.getItem('ycyw_auth_role');
  }

  get customerId(): string | null {
    return sessionStorage.getItem('ycyw_customer_id');
  }

  async loginClient(email: string): Promise<AuthTokenResponse> {
    const res = await firstValueFrom(
      this.http.post<AuthTokenResponse>(`${environment.apiUrl}/api/auth/client`, { email })
    );
    this.persist(res);
    return res;
  }

  async loginAdvisor(agentId: string, password: string): Promise<AuthTokenResponse> {
    const res = await firstValueFrom(
      this.http.post<AuthTokenResponse>(`${environment.apiUrl}/api/auth/advisor`, {
        agentId,
        password,
      })
    );
    this.persist(res);
    return res;
  }

  logout(): void {
    sessionStorage.removeItem(this.storageKey);
    sessionStorage.removeItem('ycyw_auth_role');
    sessionStorage.removeItem('ycyw_customer_id');
  }

  private persist(res: AuthTokenResponse): void {
    sessionStorage.setItem(this.storageKey, res.accessToken);
    sessionStorage.setItem('ycyw_auth_role', res.role);
    if (res.customerId) {
      sessionStorage.setItem('ycyw_customer_id', res.customerId);
    }
  }
}
