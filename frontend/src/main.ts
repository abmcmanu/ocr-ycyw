import { bootstrapApplication } from '@angular/platform-browser';

// Polyfill pour sockjs-client qui cherche l'objet 'global' de Node.js
(window as any).global = window;

import { appConfig } from './app/app.config';
import { AppComponent } from './app/app.component';

bootstrapApplication(AppComponent, appConfig)
  .catch((err) => console.error(err));
