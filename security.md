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

## Politique de Sécurité : failBuildOnCVSS

Le projet embarque nativement la configuration `<failBuildOnCVSS>9</failBuildOnCVSS>` au sein du plugin **OWASP Dependency-Check**.

Le système **CVSS** (*Common Vulnerability Scoring System*) est un standard international qui attribue une note de **0 à 10** pour évaluer la gravité d'une faille de sécurité. Une note supérieure ou égale à 9.0 désigne une **vulnérabilité critique** (par exemple, une exécution de code à distance ou un contournement total des authentifications).

L'intégration de cette règle au cœur du projet répond à plusieurs objectifs :
1. **Sécurité Automatisée (DevSecOps) :** Elle empêche de manière stricte le déploiement en production d'une version de l'application contenant des failles de sécurité majeures.
2. **Blocage au Build :** Dès qu'une dépendance (directe ou amenée indirectement par un starter) présente une faille critique, le processus de build est immédiatement interrompu. Le développeur doit obligatoirement corriger la version pour pouvoir compiler et packager le projet.
3. **Priorisation des Risques :** Placer le curseur à `9` permet de maintenir la flexibilité de développement en ne bloquant pas les builds pour des vulnérabilités mineures ou modérées, tout en restant intransigeant sur les menaces les plus destructrices pour l'infrastructure.

## Commande et Utilisation

Pour lancer l'analyse de sécurité définie dans le projet, exécutez la commande Maven suivante dans votre terminal à la racine du sous-projet backend :

```bash
mvn org.owasp:dependency-check-maven:check