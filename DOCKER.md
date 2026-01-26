# Docker Setup

## Development

Start only the database:

```bash
docker compose up -d
```

Then run backend and frontend separately with hot reload:

```bash
# Terminal 1: Backend
cd CourselyLabs-back && ./mvnw spring-boot:run

# Terminal 2: Frontend
cd CourselyLabs-front && npm run dev
```

Access:
- Frontend: http://localhost:5173
- Backend: http://localhost:8080
- Database: localhost:5432

Stop database:
```bash
docker compose down
```

Reset database (deletes all data):
```bash
docker compose down -v
docker compose up -d
```

---

## Production Deployment

When ready to deploy, create the following files:

### 1. Backend Dockerfile (`CourselyLabs-back/Dockerfile`)

```dockerfile
# Build stage
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline -B

COPY src ./src
RUN ./mvnw clean package -DskipTests -B

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -g 1001 -S appgroup && adduser -u 1001 -S appuser -G appgroup
COPY --from=build /app/target/*.jar app.jar
RUN chown -R appuser:appgroup /app

USER appuser
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 2. Frontend Dockerfile (`CourselyLabs-front/Dockerfile`)

```dockerfile
# Build stage
FROM node:22-alpine AS build
WORKDIR /app

COPY package*.json ./
RUN npm ci

COPY . .
RUN npm run build

# Runtime stage
FROM nginx:alpine
WORKDIR /usr/share/nginx/html

RUN rm -rf ./*
COPY --from=build /app/dist .
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

### 3. Nginx config (`CourselyLabs-front/nginx.conf`)

```nginx
server {
    listen 80;
    server_name localhost;
    root /usr/share/nginx/html;
    index index.html;

    gzip on;
    gzip_types text/plain text/css application/json application/javascript text/xml application/xml text/javascript;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api {
        proxy_pass http://backend:8080;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2|ttf|eot)$ {
        expires 1y;
        add_header Cache-Control "public, immutable";
    }
}
```

### 4. Production docker-compose.yml (replace existing)

```yaml
services:
  postgres:
    image: postgres:16-alpine
    container_name: courselylabs-db
    environment:
      POSTGRES_DB: cursos_db
      POSTGRES_USER: ${DB_USER:-myuser}
      POSTGRES_PASSWORD: ${DB_PASSWORD:-secret}
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./init_db:/docker-entrypoint-initdb.d:ro
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U myuser -d cursos_db"]
      interval: 10s
      timeout: 5s
      retries: 5
    networks:
      - courselylabs-network

  backend:
    build:
      context: ./CourselyLabs-back
    container_name: courselylabs-backend
    environment:
      DB_URL: jdbc:postgresql://postgres:5432/cursos_db
      DB_USER: ${DB_USER:-myuser}
      DB_PASSWORD: ${DB_PASSWORD:-secret}
    ports:
      - "8080:8080"
    depends_on:
      postgres:
        condition: service_healthy
    networks:
      - courselylabs-network

  frontend:
    build:
      context: ./CourselyLabs-front
    container_name: courselylabs-frontend
    ports:
      - "80:80"
    depends_on:
      - backend
    networks:
      - courselylabs-network

volumes:
  postgres_data:

networks:
  courselylabs-network:
    driver: bridge
```

### 5. Deploy

```bash
# Build and start all services
docker compose up --build -d

# View logs
docker compose logs -f

# Stop all
docker compose down
```

Access: http://localhost (frontend proxies API calls to backend)

### Environment Variables

For production, create a `.env` file:

```env
DB_USER=produser
DB_PASSWORD=strongpassword
```
