import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import { environment } from '../../environments/environment';
import { AuthService } from '../core/auth.service';

export interface SessionHistoryItem {
  threadId: string;
  subject: string;
  status: string;
  createdAt: string;
  messageCount: number;
}

export interface HistoryPage {
  content: SessionHistoryItem[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

@Component({
  selector: 'app-profile-history',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './profile-history.component.html',
  styleUrls: ['./profile-history.component.scss'],
})
export class ProfileHistoryComponent implements OnInit {
  history: SessionHistoryItem[] = [];
  page = 0;
  size = 10;
  totalPages = 0;
  loading = false;
  error = '';

  constructor(private http: HttpClient, private auth: AuthService) {}

  ngOnInit(): void {
    if (!this.auth.token || this.auth.role !== 'CLIENT') {
      this.error = 'Connectez-vous via le tchat avec votre email pour accéder à l\'historique.';
      return;
    }
    this.load();
  }

  load(): void {
    this.loading = true;
    this.error = '';
    this.http
      .get<HistoryPage>(`${environment.apiUrl}/api/chat/sessions/history`, {
        params: { page: this.page, size: this.size },
      })
      .subscribe({
        next: res => {
          this.history = res.content;
          this.totalPages = res.totalPages;
          this.loading = false;
        },
        error: () => {
          this.error = 'Impossible de charger l\'historique. Vérifiez votre connexion.';
          this.loading = false;
        },
      });
  }

  prev(): void {
    if (this.page > 0) {
      this.page--;
      this.load();
    }
  }

  next(): void {
    if (this.page < this.totalPages - 1) {
      this.page++;
      this.load();
    }
  }

  export(threadId: string): void {
    this.http
      .get(`${environment.apiUrl}/api/chat/sessions/${threadId}/export`, {
        responseType: 'blob',
      })
      .subscribe({
        next: blob => {
          const url = URL.createObjectURL(blob);
          const a = document.createElement('a');
          a.href = url;
          a.download = `ycyw-chat-${threadId.substring(0, 8)}.txt`;
          a.click();
          URL.revokeObjectURL(url);
        },
        error: () => {
          this.error = 'Export impossible.';
        },
      });
  }
}
