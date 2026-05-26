import { Injectable, inject } from '@angular/core';
import { Meta, Title } from '@angular/platform-browser';
import { NavigationEnd, Router } from '@angular/router';
import { filter } from 'rxjs/operators';
import { environment } from '../../environments/environment';

const DEFAULT_TITLE = 'YCYW Support — Assistance en ligne';
const DEFAULT_DESCRIPTION =
  'Support client YCYW : tchat en direct avec un conseiller pour vos réservations et questions.';

@Injectable({ providedIn: 'root' })
export class SeoService {
  private readonly title = inject(Title);
  private readonly meta = inject(Meta);
  private readonly router = inject(Router);

  init(): void {
    this.apply(DEFAULT_TITLE, DEFAULT_DESCRIPTION);
    this.setCanonical(this.router.url || '/');

    this.router.events
      .pipe(filter((e): e is NavigationEnd => e instanceof NavigationEnd))
      .subscribe((e) => {
        this.updateFromActiveRoute();
        this.setCanonical(e.urlAfterRedirects);
      });
  }

  private updateFromActiveRoute(): void {
    let route = this.router.routerState.root;
    while (route.firstChild) {
      route = route.firstChild;
    }
    const snapshot = route.snapshot;
    const pageTitle =
      (snapshot.title as string | undefined) ??
      snapshot.data['title'] ??
      DEFAULT_TITLE;
    const description =
      (snapshot.data['description'] as string | undefined) ?? DEFAULT_DESCRIPTION;

    this.apply(pageTitle, description);
  }

  private apply(pageTitle: string, description: string): void {
    this.title.setTitle(pageTitle);
    this.meta.updateTag({ name: 'description', content: description });
    this.meta.updateTag({ property: 'og:title', content: pageTitle });
    this.meta.updateTag({ property: 'og:description', content: description });
    this.meta.updateTag({ name: 'twitter:title', content: pageTitle });
    this.meta.updateTag({ name: 'twitter:description', content: description });
  }

  private setCanonical(path: string): void {
    const base = environment.siteUrl.replace(/\/$/, '');
    const cleanPath = path.split('?')[0] || '/';
    const href = `${base}${cleanPath === '/' ? '' : cleanPath}`;

    if (this.meta.getTag('rel="canonical"')) {
      this.meta.updateTag({ rel: 'canonical', href }, 'rel="canonical"');
    } else {
      this.meta.addTag({ rel: 'canonical', href });
    }
  }
}
