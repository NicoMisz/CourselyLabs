# Especificaciones de la Plataforma de Cursos Online

## 1. FUNCIONALIDADES Y REQUISITOS

### 1.1 Tipo de Cursos y Contenido
- **Formatos de contenido**: Múltiples formatos (vídeo, texto, PDFs, audio, presentaciones, código interactivo)
- **Transmisión en vivo**: No hay contenido en directo por ahora
- **Jerarquía**: Cursos divididos en secciones
- **Prerequisitos**: Puede haber prerequisitos entre cursos
- **Instructores**: Múltiples instructores por curso permitidos

### 1.2 Modelo de Negocio y Precios
- **Modelos de pago**:
  - Pago único por curso
  - Suscripción mensual
  - Freemium (cursos gratuitos y de pago)
  - Modelo mixto (combinación de los anteriores)
- **Promociones**: Sistema de cupones de descuento
- **Afiliados**: Programa de afiliados implementado
- **Reembolsos**: No hay sistema de reembolsos por ahora

### 1.3 Sistema de Evaluación
- **Tipos de exámenes**:
  - Cuestionarios (respuesta múltiple)
  - Preguntas abiertas (corrección manual)
  - Proyectos abiertos (tareas con instrucciones y entrega de archivo)
- **Límites**: Límite de intentos por examen
- **Certificados**: No hay certificados automáticos
- **Tiempo**: Límite de tiempo en los exámenes

### 1.4 Interacción Social
- **Foros**: Un foro por curso para discusión general
- **Archivado**: Los foros se pueden archivar
- **Mensajería**: Sistema de mensajes privados entre usuarios
- **Comentarios en vídeos**: No hay comentarios en momentos específicos del vídeo
- **Valoraciones**: Sistema de valoración para cursos e instructores
- **Comentarios públicos**: No hay sistema de comentarios, solo foros

### 1.5 Gestión de Usuarios y Roles
- **Roles**:
  - Administrador
  - Instructor
  - Estudiante
- **Proceso de instructor**: Cualquiera puede ser instructor
- **Verificación**: Los cursos públicos deben ser verificados antes de publicarse
- **Privacidad**: Información del usuario solo visible para usuarios del mismo curso
- **Gamificación**: No hay badges ni sistema de puntos por ahora

### 1.6 Análisis y Seguimiento
- **Estadísticas prioritarias**: Historial de actividad de usuarios
- **Otras métricas**: No se requieren por ahora (pueden añadirse después)

### 1.7 Funcionalidades Avanzadas
- **Notificaciones**:
  - Foros seguidos
  - Mensajes directos
- **Recursos**: Archivos descargables (PDFs, código fuente, recursos)
- **Otras funcionalidades**: No se requieren por ahora (listas de deseos, notas privadas, marcadores, subtítulos, transcripciones)

### 1.8 Requisitos Legales y Privacidad
- **Estado actual**: Nada específico por ahora
- **Futuro**: Puede requerir historial de transacciones, consentimientos GDPR, etc.

### 1.9 Multiidioma
- **Estado actual**: Solo castellano
- **Futuro**: Posible expansión a múltiples idiomas

### 1.10 Volumetría Esperada
- **Escala**: Proyecto pequeño/mediano
- **Usuarios**: No se esperan muchos usuarios inicialmente
- **Crecimiento**: Escalable para crecimiento futuro

---

## 2. RESUMEN EJECUTIVO

Esta plataforma es un sistema de cursos online con un modelo de negocio mixto (pago único, suscripción y freemium). Permite múltiples formatos de contenido, evaluaciones variadas, interacción mediante foros y mensajes privados, y un sistema de afiliados. Los cursos pueden tener múltiples instructores y prerequisitos. Cualquiera puede ser instructor, pero los cursos públicos requieren verificación administrativa.

**Características clave**:
- Modelo de pago flexible (único, suscripción, freemium)
- Múltiples instructores por curso
- Evaluaciones con límites de intentos y tiempo
- Sistema de foros y mensajes privados
- Programa de afiliados y cupones
- Archivos descargables
- Solo castellano (por ahora)

**Alcance inicial**: Proyecto de escala pequeña/mediana con posibilidad de crecimiento futuro.

---

## 3. ESPECIFICACIONES TÉCNICAS

### 3.1 Stack Tecnológico

#### Frontend
- **Framework**: Vue 3 con Quasar Framework 2.x
- **Lenguaje**: TypeScript
- **Estilos**: Quasar (SCSS integrado) + Tailwind CSS (opcional para personalización)
- **Reproductor de vídeo**: Video.js con plugin HLS
- **Estado global**: Pinia (gestor de estado oficial para Vue 3)
- **HTTP Client**: Axios

#### Backend
- **Framework**: Spring Boot 3.x
- **Lenguaje**: Java 17+
- **API**: REST (Spring MVC)
- **Autenticación**: Spring Security con JWT + OAuth2 (para login social opcional)
- **ORM**: Spring Data JPA con Hibernate
- **Validación**: Jakarta Bean Validation

#### Base de Datos
- **Principal**: PostgreSQL 15+
- **Cache**: Redis (para sesiones y cache de consultas)
- **Búsqueda**: PostgreSQL Full-Text Search (inicial), Elasticsearch (futuro opcional)
- **Migraciones**: Flyway / Liquibase

#### Almacenamiento
- **Archivos estáticos**: AWS S3 (producción) / MinIO (desarrollo local)
- **CDN**: Cloudflare CDN
- **Vídeos**: AWS S3 + CloudFront con signed URLs

#### Procesamiento
- **Cola de trabajos**: RabbitMQ
- **Procesamiento de vídeo**: FFmpeg (en contenedor dedicado)
- **Email**: SendGrid
- **Tareas programadas**: Spring Scheduler + Quartz

#### DevOps
- **Hosting**: AWS (EC2/ECS) o DigitalOcean
- **Contenedores**: Docker + Docker Compose
- **CI/CD**: GitHub Actions
- **Monitoreo**: Sentry (errores) + Spring Boot Actuator (métricas)

### 3.2 Arquitectura

#### Patrón arquitectónico
- **Tipo**: Modular Monolith (con posibilidad de evolucionar a microservicios)
- **Estructura**: Layered Architecture (Controller → Service → Repository)
- **Módulos principales**:
  - Módulo de Autenticación y Usuarios
  - Módulo de Cursos y Contenido
  - Módulo de Evaluaciones
  - Módulo de Pagos y Suscripciones
  - Módulo de Foros y Mensajería
  - Módulo de Afiliados

#### Comunicación
- **API REST**: Endpoints RESTful para operaciones CRUD
- **WebSockets**: Spring WebSocket (STOMP) para notificaciones en tiempo real y mensajería
- **Server-Sent Events**: Spring MVC SSE para actualizaciones de progreso de vídeo

### 3.3 Seguridad

- **Autenticación**: Sistema de tokens JWT con refresh tokens
- **Autorización**: Control de acceso basado en roles (RBAC)
- **Encriptación**: 
  - Contraseñas: bcrypt / argon2
  - Datos sensibles: AES-256
- **HTTPS**: Obligatorio en producción
- **Rate Limiting**: Protección contra ataques de fuerza bruta
- **Validación**: Validación de entrada en frontend y backend
- **CORS**: Configuración restrictiva

### 3.4 Rendimiento

- **Paginación**: Para listados de cursos, lecciones, foros
- **Lazy Loading**: Carga diferida de imágenes y vídeos
- **Caché**:
  - Datos estáticos: Cache HTTP
  - Sesiones: Redis
  - Queries frecuentes: Cache de base de datos
- **Optimización de imágenes**: WebP, compresión automática
- **CDN**: Para assets estáticos y vídeos

### 3.5 Testing

**Frontend**:
- **Unit Tests**: Vitest + Vue Test Utils
- **Component Tests**: Vitest + @vue/test-utils
- **E2E Tests**: Playwright

**Backend**:
- **Unit Tests**: JUnit 5 + Mockito
- **Integration Tests**: Spring Boot Test + Testcontainers (PostgreSQL, Redis)
- **API Tests**: REST Assured + Spring MockMvc
- **Cobertura objetivo**: 70%+

### 3.6 Versionado y Documentación

- **Control de versiones**: Git con GitHub
- **Documentación API**: Swagger / OpenAPI 3.0 (Springdoc)
- **Convenciones**: Conventional Commits + Semantic Versioning
- **README**: Documentación de configuración y deployment
- **Diagramas**: PlantUML para diagramas de arquitectura

### 3.7 Pagos y Facturación

- **Pasarela de pago**: Stripe
- **Gestión de suscripciones**: Stripe Subscriptions
- **Webhooks**: Stripe Webhooks para eventos de pago
- **Facturación**: Stripe Invoicing
- **Monedas soportadas**: EUR (principal), USD (opcional)
- **Cupones de descuento**: Sistema propio integrado con Stripe Coupons

### 3.8 Configuración del Entorno

#### Variables de Entorno Principales

**Base de Datos**:
- `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`
- `REDIS_HOST`, `REDIS_PORT`, `REDIS_PASSWORD`

**Almacenamiento**:
- `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`
- `AWS_S3_BUCKET`, `AWS_REGION`
- `CLOUDFLARE_ACCOUNT_ID`, `CLOUDFLARE_API_TOKEN`

**Autenticación**:
- `JWT_SECRET`, `JWT_EXPIRATION`
- `OAUTH2_GOOGLE_CLIENT_ID`, `OAUTH2_GOOGLE_CLIENT_SECRET` (opcional)

**Pagos**:
- `STRIPE_PUBLIC_KEY`, `STRIPE_SECRET_KEY`
- `STRIPE_WEBHOOK_SECRET`

**Email y Notificaciones**:
- `SENDGRID_API_KEY`, `SENDGRID_FROM_EMAIL`

**Frontend**:
- `VUE_APP_API_BASE_URL`
- `VUE_APP_STRIPE_PUBLIC_KEY`
- `VUE_APP_CDN_URL`

---

## 4. GUÍA PARA PERSONALIZAR ESTE DOCUMENTO

### Cómo añadir tus propios parámetros:

#### Opción 1: Editar directamente las secciones existentes
Reemplaza los valores entre corchetes `[...]` con tus decisiones técnicas. Por ejemplo:

**Antes**:
```
- **Framework**: [Especificar: React, Vue, Angular, Next.js, etc.]
```

**Después**:
```
- **Framework**: Next.js 14 con App Router
```

#### Opción 2: Añadir nuevas subsecciones
Puedes agregar nuevas categorías técnicas según tus necesidades:

```markdown
#### Pagos y Facturación
- **Pasarela de pago**: Stripe
- **Facturación**: Stripe Invoicing
- **Monedas soportadas**: EUR, USD
```

#### Opción 3: Crear secciones personalizadas
Añade secciones completamente nuevas al final del documento:

```markdown
## 5. INTEGRACIONES DE TERCEROS

### 5.1 Servicios externos
- **Analytics**: Google Analytics 4
- **Chat de soporte**: Intercom / Crisp
- **Email marketing**: Mailchimp
- **CRM**: HubSpot

### 5.2 APIs externas
- **Mapas**: Google Maps API
- **Traducción**: DeepL API
- **IA**: OpenAI API para sugerencias
```

#### Opción 4: Añadir detalles de configuración
Expande cualquier sección con configuraciones específicas:

```markdown
### 3.7 Variables de Entorno

#### Desarrollo
- `DATABASE_URL`: postgresql://localhost:5432/courses_dev
- `REDIS_URL`: redis://localhost:6379
- `JWT_SECRET`: [generar secreto]
- `AWS_BUCKET`: courses-dev-bucket

#### Producción
- `DATABASE_URL`: [RDS URL]
- `REDIS_URL`: [ElastiCache URL]
- `CDN_URL`: https://cdn.miplataforma.com
```

### Ejemplo de personalización completa:

```markdown
### 3.1 Stack Tecnológico (PERSONALIZADO)

#### Frontend
- **Framework**: Next.js 14 con App Router
- **Lenguaje**: TypeScript 5.0
- **Estilos**: Tailwind CSS 3.4
- **UI Components**: shadcn/ui
- **Reproductor de vídeo**: Plyr con HLS.js
- **Estado global**: Zustand + React Query

#### Backend
- **Framework**: Next.js API Routes + tRPC
- **Lenguaje**: TypeScript 5.0
- **ORM**: Prisma
- **Autenticación**: NextAuth.js con JWT

#### Base de Datos
- **Principal**: PostgreSQL 16 (Supabase)
- **Cache**: Vercel KV (Redis)
- **Storage**: Supabase Storage para archivos

#### Almacenamiento
- **Vídeos**: Bunny Stream
- **CDN**: Bunny CDN
- **Archivos descargables**: Supabase Storage
```

### Plantilla rápida para copiar y rellenar:

```markdown
## MI CONFIGURACIÓN TÉCNICA

**Frontend**: _______________
**Backend**: _______________
**Base de datos**: PostgreSQL + _______________
**Almacenamiento de vídeos**: _______________
**Hosting**: _______________
**Autenticación**: _______________
**Procesamiento de pagos**: _______________
**Email**: _______________

**Notas adicionales**:
- _______________
- _______________
```

---

**💡 Recomendación**: Completa primero las decisiones más importantes (framework frontend/backend, hosting, almacenamiento de vídeos) y luego añade detalles progresivamente a medida que avance el proyecto.