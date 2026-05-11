# ─── Stage 1: build ───────────────────────────────────────────────
FROM node:20-alpine AS build
WORKDIR /src

# Cachear deps mientras package.json no cambia
COPY package.json package-lock.json ./
RUN npm ci

COPY . .

# El frontend habla con /api (rutas relativas). Nginx hace proxy al backend.
ENV VITE_API_BASE_URL=""
RUN npm run build

# ─── Stage 2: serve ───────────────────────────────────────────────
FROM nginx:1.27-alpine
WORKDIR /usr/share/nginx/html

# Configuración para SPA + proxy /api → backend
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copiar build
RUN rm -rf ./*
COPY --from=build /src/dist .

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
