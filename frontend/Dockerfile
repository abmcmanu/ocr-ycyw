# --- Étape 1 : Build de l'application Angular ---
FROM node:20-alpine AS build
WORKDIR /app

# Copie des fichiers de configuration et installation des dépendances
COPY package*.json ./
RUN npm ci

# Copie du reste du code source
COPY . .

# Compilation de l'application en mode production
RUN npm run build -- --configuration=production

# --- Étape 2 : Serveur Nginx de production ---
FROM nginx:1.25-alpine

# Copie de la configuration personnalisée de Nginx (gestion du routage Angular)
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copie des fichiers compilés d'Angular (Vérifiez le nom du dossier dans dist/ après le build d'Angular 17)
COPY --from=build /app/dist/frontend/browser /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
