# --- Étape 1 : Build de l'application ---
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copie du pom.xml et téléchargement des dépendances pour mettre en cache cette couche
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copie du code source et compilation
COPY src ./src
RUN mvn clean package -DskipTests

# --- Étape 2 : Image finale de production ---
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Création d'un utilisateur non-root pour des raisons de sécurité
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copie du JAR compilé depuis l'étape précédente
COPY --from=build /app/target/*.jar app.jar

# Port exposé par Spring Boot
EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]