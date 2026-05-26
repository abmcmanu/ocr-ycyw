import { Routes } from '@angular/router';
import { AdvisorConsoleComponent } from './advisor-console/advisor-console.component';
import { ChatWidgetComponent } from './chat-widget/chat-widget.component';

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
    path: 'advisor',
    component: AdvisorConsoleComponent,
    title: 'Console conseiller | YCYW',
    data: {
      description:
        'Espace conseiller YCYW : file d\'attente, conversations clients et contexte réservations.',
    },
  },
];
