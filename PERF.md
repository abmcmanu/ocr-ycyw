# Performances et Tenue de charge

Ce document détaille les critères d'acceptation et la stratégie de test de performance du PoC.

## Objectifs (US-24)
- **Latence cible** : < 1s pour la livraison d'un message en condition nominale.
- **Tenue de charge** : ≥ 1 000 requêtes par seconde (req/s) en pic.
- **Taux d'erreur** : < 1 %.

## Stratégie d'optimisation
- **PostgreSQL** : Pool de connexions (HikariCP), indexation optimisée (sur `thread_id`, `created_at`, `status`).
- **Redis** : Pub/Sub léger pour la diffusion temps réel, stockage des sessions actives pour éviter de saturer PostgreSQL sur les requêtes d'état de présence.
- **Gateway/Backend** : Rate-limiting (Bucket4j) pour empêcher les abus (DDoS au niveau applicatif).

## Tests avec K6
Un script `k6-load-test.js` (à venir) simulera :
1. Connexion WS.
2. Envoi de messages en boucle (simulation de pics de 1000 utilisateurs actifs).
3. Mesure de la latence de réception (`p(95) < 1s`).
