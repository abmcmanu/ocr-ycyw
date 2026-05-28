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

## Structure des Données & Migrations

Ce projet utilise **Flyway** pour la gestion et le versioning du schéma de la base de données. Cela permet de garantir la cohérence des structures de données entre tous les environnements (développement, test, production).

* **Emplacement des scripts de migration :** [backend/src/main/resources/db/migration](./backend/src/main/resources/db/migration)
* **Fonctionnement :** À chaque démarrage de l'application, Flyway vérifie et applique automatiquement les nouveaux scripts SQL non encore exécutés.

## Supervision, Health Check & Métriques

Après avoir démarré l'infrastructure avec la commande `docker-compose up -d`, vous pouvez monitorer l'état de l'application grâce aux points d'accès Spring Boot Actuator.

### 1. Vérification de la disponibilité (Health Check)
Pour valider que l'application tourne correctement et que ses dépendances (base de données, etc.) sont fonctionnelles :
* **URL :** [http://localhost:8081/actuator/health](http://localhost:8081/actuator/health)

### 2. Métriques de Performance (Prometheus)
L'application expose des métriques détaillées destinées à être collectées par un serveur Prometheus. Ces données sont protégées et nécessitent un jeton d'accès.

Pour inspecter les 30 premières lignes des métriques exposées, exécutez la commande suivante :

```bash
curl -s http://localhost:8081/actuator/prometheus \
  -H "Authorization: Bearer <ACCESS_TOKEN_ICI>" | head -30