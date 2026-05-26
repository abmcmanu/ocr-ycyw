# Maintenance & Exploitation

## Purge RGPD (Conservation 12 mois)
Conformément à la réglementation européenne et locale (France), l'historique des conversations est conservé 12 mois maximum.
- **Processus** : Un `CronJob` Spring Boot (ou une procédure stockée PostgreSQL `archive_old_chat_messages`) s'exécute périodiquement.
- **Archivage** : Les messages sont transférés dans une table partitionnée `chat_messages_archive`, ou directement supprimés (selon la politique exacte).
- **Tracabilité** : Une trace de l'exécution de la purge est logguée.

## Observabilité
- **Logs** : Loki (à intégrer)
- **Métriques** : Prometheus (via Actuator `/actuator/prometheus`)
- **Visualisation** : Grafana (Dashboards pour la latence WebSocket, le nombre de sessions actives, etc.)
- **Objectifs (SLA)** : MTTD < 5 min, MTTR < 1 h.
