# CourselyLabs

Plataforma de cursos online con múltiples formatos de contenido, sistema de evaluaciones, foros y mensajería.

## Requisitos Previos

Antes de comenzar, asegúrate de tener instalado:

- **Java 21** o superior
- **Node.js 20.19.0+** o **22.12.0+**
- **Maven 3.6+** (o usar el wrapper incluido `mvnw`)
- **Docker** y **Docker Compose**
- **Git**

### Verificar versiones

```bash
java -version
node -v
npm -v
docker --version
docker compose version
```

## Instalación

### 1. Clonar el repositorio

```bash
git clone <url-del-repositorio>
cd CourselyLabs
```

## Backend (Spring Boot)

### 2. Configurar y ejecutar el backend

#### Opción A: Con Docker Compose (Recomendado)

El proyecto incluye un archivo `compose.yaml` que levanta PostgreSQL automáticamente:

```bash
cd CourselyLabs-back

# Levantar PostgreSQL con Docker
docker compose up -d

# Ejecutar la aplicación Spring Boot
./mvnw spring-boot:run

# En Windows, usar:
# mvnw.cmd spring-boot:run
```

#### Opción B: Sin Docker (PostgreSQL local) (NO RECOMENDADO)

Si tienes PostgreSQL instalado localmente, configura las credenciales en `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/mydatabase
spring.datasource.username=myuser
spring.datasource.password=secret
```

Luego ejecuta:

```bash
cd CourselyLabs-back
./mvnw spring-boot:run
```

### Verificar que el backend está corriendo

El backend estará disponible en: **http://localhost:8080**

### Compilar el proyecto (opcional)

```bash
cd CourselyLabs-back
./mvnw clean install
```

## Frontend (Vue 3 + Quasar)

### 3. Configurar y ejecutar el frontend

```bash
cd CourselyLabs-front

# Instalar dependencias
npm install

# Ejecutar en modo desarrollo
npm run dev
```

El frontend estará disponible en: **http://localhost:5173** (o 5174 si el puerto 5173 está ocupado)

### Comandos disponibles del frontend

```bash
# Modo desarrollo
npm run dev

# Compilar para producción
npm run build

# Vista previa de la build de producción
npm run preview

# Ejecutar tests unitarios
npm run test:unit

# Type checking
npm run type-check

# Linting
npm run lint

# Formatear código
npm run format
```

## Stack Tecnológico

### Backend
- **Framework**: Spring Boot 4.1.0-M1
- **Lenguaje**: Java 21
- **Base de datos**: PostgreSQL (latest)
- **Build Tool**: Maven
- **ORM**: Spring Data JPA

### Frontend
- **Framework**: Vue 3.5.26
- **UI Framework**: Quasar 2.18.6
- **Lenguaje**: TypeScript 5.9
- **Build Tool**: Vite 7.3
- **Estado global**: Pinia 3.0
- **Router**: Vue Router 4.6
- **Testing**: Vitest 4.0

## Estructura del Proyecto

```
CourselyLabs/
├── CourselyLabs-back/          # Backend Spring Boot
│   ├── src/
│   ├── pom.xml
│   ├── compose.yaml
│   └── mvnw
├── CourselyLabs-front/         # Frontend Vue + Quasar
│   ├── src/
│   ├── package.json
│   └── vite.config.ts
├── CLAUDE.md                   # Especificaciones del proyecto
└── README.md                   # Este archivo
```

## Configuración de Base de Datos

### Credenciales por defecto (Docker Compose)

```
Host: localhost
Port: 5432
Database: mydatabase
User: myuser
Password: secret
```

Para cambiar estas credenciales, edita el archivo `CourselyLabs-back/compose.yaml`.

## Desarrollo

### Ejecutar ambos proyectos simultáneamente

Abre dos terminales:

**Terminal 1 - Backend:**
```bash
cd CourselyLabs-back
docker compose up -d
./mvnw spring-boot:run
```

**Terminal 2 - Frontend:**
```bash
cd CourselyLabs-front
npm run dev
```

## Solución de Problemas

### Backend

**Error: "Port 8080 is already in use"**
- Detén el proceso que está usando el puerto 8080 o cambia el puerto en `application.properties`:
  ```properties
  server.port=8081
  ```

**Error: "Could not connect to PostgreSQL"**
- Verifica que Docker esté corriendo: `docker ps`
- Reinicia el contenedor: `docker compose restart`

### Frontend

**Error: "Cannot find module '@quasar/vite-plugin'"**
- Ejecuta: `npm install`

**Puerto ocupado**
- Vite cambiará automáticamente al siguiente puerto disponible (5174, 5175, etc.)

## Variables de Entorno

### Backend
Editar `CourselyLabs-back/src/main/resources/application.properties` según necesidades.

### Frontend
Crear archivo `.env` en `CourselyLabs-front/` (si es necesario):
```env
VITE_API_BASE_URL=http://localhost:8080
```

## Despliegue en Producción

### Backend
```bash
cd CourselyLabs-back
./mvnw clean package
java -jar target/CourselyLabs-0.0.1-SNAPSHOT.jar
```

### Frontend
```bash
cd CourselyLabs-front
npm run build
# Los archivos compilados estarán en CourselyLabs-front/dist
```

## Contribuir

1. Crea una rama para tu feature: `git checkout -b feature/nueva-funcionalidad`
2. Haz commit de tus cambios: `git commit -m "Añadir nueva funcionalidad"`
3. Push a la rama: `git push origin feature/nueva-funcionalidad`
4. Crea un Pull Request

## Documentación Adicional

- [CLAUDE.md](CLAUDE.md) - Especificaciones técnicas completas del proyecto
- [Backend HELP.md](CourselyLabs-back/HELP.md) - Documentación de Spring Boot
- [Frontend README](CourselyLabs-front/README.md) - Documentación de Vue

## Licencia

[Especificar licencia]

## Contacto

[Especificar información de contacto]
