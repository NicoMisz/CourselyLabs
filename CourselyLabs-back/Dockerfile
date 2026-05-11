# ─── Stage 1: build ───────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /src

# Cachear dependencias (capa estable mientras pom.xml no cambia)
COPY pom.xml mvnw ./
COPY .mvn .mvn
RUN ./mvnw -B dependency:go-offline -DskipTests || true

# Compilar
COPY src src
RUN ./mvnw -B clean package -DskipTests

# ─── Stage 2: runtime ─────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Crear usuario no-root
RUN addgroup -S app && adduser -S -G app app

COPY --from=build /src/target/*.jar app.jar
RUN chown -R app:app /app
USER app

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
