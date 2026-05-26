import { isDevMode } from '@angular/core';

/** Pas de console en prod : pénalisé par Lighthouse Best Practices. */
export function logError(context: string, err: unknown): void {
  if (isDevMode()) {
    console.error(`[${context}]`, err);
  }
}

export function logDebug(context: string, message: string): void {
  if (isDevMode()) {
    console.log(`[${context}] ${message}`);
  }
}
