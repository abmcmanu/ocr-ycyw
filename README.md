# YCYW - POC Tchat Temps Réel

Ce dépôt contient la preuve de concept (PoC) du tchat de support client en temps réel pour YCYW.
Le but est de valider les choix architecturaux et la faisabilité de la fonctionnalité.

## Documentation disponible

- [Architecture (archit.md)](./archit.md) : Détails de l'architecture, diagrammes de composants, flux de données.
- [Performances & Tenue de charge (PERF.md)](./PERF.md) : Modèles de charge K6, métriques et observabilité.
- [Sécurité (security.md)](./security.md) : Pratiques OWASP, gestion des identités, protection des accès.
- [Maintenance & Exploitation (maintenance.md)](./maintenance.md) : Procédures de purge (RGPD), monitoring, et astuces d'exploitation.

## Démarrage rapide

Un fichier `docker-compose.yml` est disponible à la racine pour démarrer l'infrastructure et les applications.

```bash
docker-compose up -d
```
