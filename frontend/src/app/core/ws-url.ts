/** Convertit l'URL API HTTP(S) en URL WebSocket STOMP. */
export function apiUrlToBrokerUrl(apiUrl: string): string {
  const wsOrigin = apiUrl
    .replace(/^https:\/\//i, 'wss://')
    .replace(/^http:\/\//i, 'ws://');
  return `${wsOrigin.replace(/\/$/, '')}/ws`;
}
