import { Routes } from '@angular/router';
import { AdvisorConsoleComponent } from './advisor-console/advisor-console.component';
import { ChatWidgetComponent } from './chat-widget/chat-widget.component';
import { ProfileHistoryComponent } from './profile-history/profile-history.component';

export const routes: Routes = [
  {
    path: '',
    component: ChatWidgetComponent,
    title: 'Support client | YCYW',
    data: {
      description:
        'Contactez le support YCYW par tchat : réservations, questions et assistance en temps réel.',
    },
  },
  {
    path: 'profile/history',
    component: ProfileHistoryComponent,
    title: 'Mon historique | YCYW',
    data: {
      description: 'Historique de vos conversations support YCYW et export texte.',
    },
  },
  {
    path: 'advisor',
    component: AdvisorConsoleComponent,
    title: 'Console conseiller | YCYW',
    data: {
      description:
        'Espace conseiller YCYW : file d\'attente, conversations clients et contexte réservations.',
    },
  },
];
