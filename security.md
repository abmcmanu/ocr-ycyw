# Sécurité (OWASP & Meilleures pratiques)

## Authentification & Autorisation
- **JWT (JSON Web Tokens)** : Utilisé pour authentifier les appels REST et la négociation WebSocket.
- **Rôles** : Distinction stricte entre `CLIENT` et `ADVISOR` (Conseiller).

## Protections OWASP
- **XSS (Cross-Site Scripting)** : Angular neutralise le HTML par défaut. Côté Backend, nettoyage des entrées avant persistance (`message_html`).
- **CORS** : Strictement configuré pour n'autoriser que l'origine du frontend Angular.
- **Dépendances** : Utilisation du plugin `mvn dependency-check:check` pour scanner les vulnérabilités CVE connues.

## WebSocket
- Origines autorisées vérifiées lors du Handshake.
- Injection STOMP prévenue par la validation des payloads DTO.
