# Architecture du PoC Tchat YCYW

## Vue d'ensemble

Le système repose sur une architecture orientée services simplifiée (monolithe modulaire pour la PoC) pour valider les interactions temps réel.

- **Frontend** : Angular (SPA) - Gestion du widget tchat et de la console conseiller.
- **Backend** : Spring Boot - Fournit l'API REST et gère le canal WebSocket/STOMP.
- **Broker de messages** : Redis Pub/Sub - Permet de diffuser les messages entre les instances du backend.
- **Base de données** : PostgreSQL - Stockage persistant des sessions, messages et état de la file d'attente.

## Flux de données principal (WebSocket)

1. Le client établit une connexion WebSocket (SockJS fallback) au serveur Spring Boot.
2. Spring Boot enregistre la session STOMP et marque l'utilisateur comme actif (Heartbeat).
3. À l'envoi d'un message (`/app/chat.send`), le backend le persiste en base.
4. Le backend publie le message via Redis Pub/Sub.
5. Tous les backends souscrivent à Redis et relaient le message aux clients connectés au topic STOMP concerné (`/topic/session/{id}`).
