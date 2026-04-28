# Estado del proyecto — CourselyLabs

> Snapshot único de "qué hay hecho hoy". Léelo en 10 minutos para tener el contexto completo del proyecto.
>
> Última actualización: **2026-04-28** (rama `feature/assessments`).

---

## 1. Qué es CourselyLabs

Plataforma de cursos online con foco en autoría asistida y evaluaciones variadas. Un instructor crea un curso → secciones → lecciones; cada lección es un contenedor de **bloques** (texto, vídeo, PDF, cuestionario, proyecto, pregunta abierta). Los alumnos se inscriben, consumen contenido, hacen evaluaciones y reciben calificaciones manuales o automáticas.

- **Modelo de negocio**: freemium con suscripción Premium mensual única (no hay compra de cursos individuales).
- **Roles**: `admin`, `premium`, `user` (cualquier `user` puede ser instructor; los cursos públicos requieren aprobación admin).
- **Idioma**: castellano (infraestructura `vue-i18n` lista para multi-idioma futuro).

Especificación funcional completa: [`CLAUDE.md`](../CLAUDE.md).

---

## 2. Stack real (lo que se usa **hoy**, no el aspiracional)

### Backend
- **Spring Boot 4.1.0-M1**, Java 21, Maven Wrapper
- **PostgreSQL 16-alpine** (puerto 4321 en local) — usuario `myuser`, BD `cursos_db`
- **Flyway** para migraciones (`V1` → `V11`)
- **Spring Security + JWT** (`io.jsonwebtoken 0.12.6`) con refresh tokens en BD
- **Stripe** (`26.3.0`) — solo Premium, no cursos individuales
- **MinIO** (S3-compatible) para uploads y recursos descargables
- **MailHog** para correo en desarrollo (Web UI: `http://localhost:8025`)
- **Lombok** (`@Data`, `@AllArgsConstructor`, etc.)

### Frontend
- **Vue 3.5.26 + Quasar 2.18.6 + TypeScript + Vite 7**
- **Pinia** para estado global, **vue-router 4**
- **vue-i18n@9.14** (configurado, claves principales en [`src/i18n/locales/es.json`](../CourselyLabs-front/src/i18n/locales/es.json))
- **vuedraggable 4** para reorden de bloques en el editor
- **TipTap 3** como rich-text editor
- **axios** con interceptores para JWT
- **Video.js** + **PDF.js** para reproducción

### Infra
- Todo en `docker-compose.yml` raíz: `postgres`, `mailhog`, `minio` (puertos 4321, 1025/8025, 9000/9001).
- Backend y frontend se levantan localmente (no en Docker) durante el desarrollo.

> **Aviso**: `CLAUDE.md` menciona Redis, Cloudflare, RabbitMQ, AWS S3, SendGrid… **nada de eso está implementado**. Es la visión a largo plazo.

---

## 3. Estructura del repositorio

```
CourselyLabs/
├── CourselyLabs-back/                  # Spring Boot
│   ├── src/main/java/com/courselylabs/courselylab/
│   │   ├── controller/                 # 18 controllers REST
│   │   ├── service/                    # Lógica de negocio
│   │   ├── entity/                     # 22 entidades JPA
│   │   ├── repository/                 # Spring Data
│   │   ├── dto/                        # DTOs de I/O
│   │   ├── mapper/                     # Entity ↔ DTO
│   │   ├── security/                   # JWT, filtros, CourseSecurityService
│   │   └── exception/                  # Excepciones tipadas + handler global
│   └── src/main/resources/
│       └── db/migration/               # Flyway V1 → V11
├── CourselyLabs-front/                 # Vue 3 + Quasar
│   ├── src/
│   │   ├── api/                        # Clientes axios por dominio
│   │   ├── components/                 # ~40 componentes
│   │   ├── views/{admin,instructor,…}  # Páginas
│   │   ├── stores/                     # auth (Pinia)
│   │   ├── i18n/locales/es.json        # Claves de traducción
│   │   ├── router/                     # routes.ts + guards
│   │   └── types/                      # Tipos TypeScript
│   └── CHANGELOG.md                    # Registro de cambios
├── init_db/                            # Schema base + seeders SQL
├── docker-compose.yml                  # postgres + mailhog + minio
├── CLAUDE.md                           # Especificaciones funcionales
├── branch-plan.md                      # Plan de ramas + estado por feature
└── docs/                               # Documentación técnica
```

---

## 4. Features completadas

> Cada entrada apunta al doc detallado cuando existe; el resto está documentado solo en `branch-plan.md`.

| Feature | Estado | Notas / docs |
|---|---|---|
| **Auth (registro, login, JWT, refresh)** | ✅ | Spring Security + jjwt; verificación email vía MailHog |
| **Verificación de email** | ✅ | Tabla `verification_tokens`, link → `/verificar-email?token=…` |
| **Catálogo de cursos** | ✅ | Listado público, filtros, búsqueda full-text PostgreSQL |
| **Inscripciones (enrollment)** | ✅ | Checks de prerequisitos antes de inscribir |
| **Curso → secciones → lecciones** | ✅ | CRUD completo + reorden |
| **Recursos descargables** | ✅ | MinIO + signed URLs · doc: [`downloadable-resources.md`](downloadable-resources.md) |
| **Progreso del estudiante** | ✅ | `lesson_progress` + `course_progress` derivado |
| **Prerequisitos entre cursos** | ✅ | `course_prerequisites` con `completion_threshold`; solo creación es feature Premium |
| **Reviews / valoraciones** | ✅ (backend); ⚠️ frontend mínimo | Falta página dedicada |
| **Suscripción Premium (Stripe)** | ✅ | doc: [`payments-flow.md`](payments-flow.md) · webhook + `subscriptions` table |
| **Evaluaciones (quiz / project / open_text)** | ✅ | Migrado a multi-bloque en `V11` |
| **Multi-bloque en lecciones** | ✅ (este branch) | doc: [`lesson-blocks-refactor.md`](lesson-blocks-refactor.md) |
| **Dashboard de calificación** | ✅ (este branch) | doc: [`grading-dashboard.md`](grading-dashboard.md) |
| **Panel admin (cursos pendientes, usuarios, conceder Premium)** | ✅ | `/admin/cursos`, `/admin/usuarios` |
| **Panel instructor (mis cursos, calificar)** | ✅ | `/instructor/cursos`, `/instructor/calificar` |
| **Editor visual de cursos (CourseWizard)** | ✅ | Sidebar + panel principal + bloques con DnD |
| **i18n con vue-i18n** | ✅ (este branch, parcial) | doc: [`i18n.md`](i18n.md) — solo 3 componentes migrados |
| **Sweep ortográfico castellano** | ✅ (este branch) | ~150 patrones aplicados con `sed` |

---

## 5. Features en curso / no empezadas

Detalle completo en [`../branch-plan.md`](../branch-plan.md). Resumen:

| Feature | Estado | Prioridad |
|---|---|---|
| Foros por curso | 🆕 Pendiente | Media |
| Mensajería privada | 🆕 Pendiente | Media |
| Notificaciones (in-app + email) | 🆕 Pendiente | Media |
| Sistema de afiliados | 🆕 Pendiente | Baja |
| Cupones de descuento | 🆕 Pendiente | Baja |
| Frontend completo de reviews | ⚠️ Parcial | Media |
| Migración total a `t()` (vue-i18n) | ⚠️ Parcial (3/68 componentes) | Baja |
| Despliegue (CI/CD, hosting) | 🆕 Pendiente | Baja |
| Tests (front Vitest, back Testcontainers) | ⚠️ Parcial | Media |

---

## 6. Modelo de datos (esquema relacional)

### Tablas principales

```
users ──┬──< enrollments >── courses ──┬──< sections ──< lessons ──< lesson_blocks
        │                              │                              │
        ├──< course_instructors        ├──< course_prerequisites      └──< assessments (1:1 con block)
        │                              │                                       │
        ├──< subscriptions (Premium)   ├──< reviews                             ├──< quiz_questions ──< quiz_options
        │                              │                                       │
        ├──< payments                  └── created_by (FK a users)              └──< assessment_attempts ──< submissions
        │                                                                                 │
        ├──< verification_tokens                                                          └──< quiz_answers
        │
        ├──< refresh_tokens
        │
        ├──< lesson_progress >── lessons
        │
        └──< lesson_resources (FK lesson) — almacenadas en MinIO
```

### Reglas de seguridad implementadas (`CourseSecurityService`)
- `isOwnerOrInstructorOrAdmin(courseId)` — base de todos los `@PreAuthorize`.
- `canEditSection`, `canEditLesson`, `canEditBlock`, `canEditAssessment`.
- `canAccessLesson` — gratuita, o inscrito, o instructor/owner/admin.
- `canAccessAttempt`, `canGradeSubmission`, `canAccessResource`.

### Migraciones Flyway (en orden)

| Versión | Contenido |
|---|---|
| `V1` | Schema base (users, courses, categorias, sections, lessons) |
| `V2` | Sections + lessons (CRUD completo) |
| `V3` | `lesson_progress` + `course_progress` |
| `V4` | `verification_tokens` |
| `V5` | `created_by` en courses |
| `V6` | `rejection_reason` para flujo de revisión |
| `V7` | `subscriptions` + `payments` (Stripe) |
| `V8` | `course_prerequisites` con threshold |
| `V9` | `lesson_resources` (MinIO) |
| `V10` | `assessments`, `quiz_questions/options`, `attempts`, `submissions`, `quiz_answers` |
| `V11` | `lesson_blocks` + migración de datos + `block_id` en assessments |

---

## 7. Decisiones arquitectónicas vivas

Estas decisiones están **conscientemente** así y conviene conocerlas antes de cambiarlas:

1. **Sin compra individual de cursos** — solo Premium mensual. Stripe se usa exclusivamente para suscripciones.
2. **MinIO en lugar de S3 en desarrollo** — código habla con S3 SDK, MinIO es drop-in. Producción seguiría usando MinIO o S3 real con la misma config.
3. **Lección como contenedor de bloques** (V11) — el campo legacy `lessons.type` queda nullable; cada bloque tiene su tipo.
4. **Una evaluación por bloque, no por lección** — múltiples evaluaciones por lección ahora son posibles.
5. **`lesson.type` legacy todavía se rellena al migrar pero no se usa para renderizar** — se preserva por compatibilidad con datos previos.
6. **Permisos via `@PreAuthorize` con bean SpEL** — todo va por `@courseSecurityService.canX(#id, authentication)`.
7. **Storage limits por rol** — `user`: 300 MB/curso, `premium`: 1 GB/curso, `admin`: ∞.
8. **Límite de evaluaciones gratuito** — `user` máx. 2 de cada tipo por curso; `premium` y `admin` ilimitadas.
9. **Submissions NO cuentan contra storage del curso** — los archivos del alumno son aparte.
10. **Rich-text editor TipTap v3** — la signatura `setContent(html, { emitUpdate: false })` cambió en v3 (era `setContent(html, false)` antes).

---

## 8. Limitaciones conocidas y deuda técnica

- **i18n**: solo 3 componentes migrados a `t()` (InstructorLayout, GradingDashboard, AssessmentEditor). El resto sigue con strings inline en castellano correcto. Migración progresiva pendiente.
- **Reviews**: backend completo, frontend mínimo (sin página dedicada).
- **CourseWizard.vue** y **LessonView.vue** son grandes (~1300 y ~370 líneas) y mezclan muchas responsabilidades — candidatos a refactor.
- **Sin tests E2E** — Vitest solo cubre lo unitario, y de forma muy parcial.
- **API reference desactualizada** — la tabla en `CourselyLabs-front/README.md` lista endpoints que ya no existen (`/api/users/{id}` p.ej.).
- **`CourselyLabs-back/HELP.md`** es boilerplate de Spring Initializr y debería borrarse.
- **`CLAUDE.md` lista stack aspiracional** (Redis, RabbitMQ, AWS, etc.) que no está implementado — confunde a quien llega nuevo.
- **Bundle del frontend**: `LessonView.js` y `CourseWizard.js` superan 700 KB cada uno — pendiente code-splitting más fino.

---

## 9. Cómo seguir

1. **Para levantar el proyecto en local**: [`../README.md`](../README.md) sección "Instalación" (también `DOCKER.md` y `servicios.md`).
2. **Para entender qué hay en cada feature**: lee este documento y abre el doc específico (columna "Notas / docs" de §4).
3. **Para añadir una feature nueva**: crea rama desde `develop`, sigue convenciones de [`../branch-plan.md`](../branch-plan.md).
4. **Para añadir migraciones**: siguiente Flyway sería `V12__…sql`. Actualiza también `init_db/01_schema.sql`.
5. **Para añadir strings UI**: ver [`i18n.md`](i18n.md) — añade clave a `es.json` y usa `t('clave')`.

---

## 10. Glosario rápido

- **Bloque** (`lesson_block`): unidad de contenido dentro de una lección. Tipos: `text`, `video`, `pdf`, `quiz`, `project`, `open_text`.
- **Assessment**: evaluación asociada a un bloque de tipo evaluación. Tiene `attempts` y, según el tipo, `submissions`.
- **Submission**: entrega de un alumno (archivo para `project`, texto para `open_text`). Pendiente de calificar manualmente por el instructor.
- **Attempt**: cada vez que un alumno empieza una evaluación. Limitado por `max_attempts`.
- **Storage limit**: cuota de bytes por curso, depende del rol del owner. Solo cuenta vídeo/PDF de bloques + recursos descargables (no submissions).
- **Premium**: usuario con suscripción Stripe activa. Desbloquea cursos Premium, prerequisitos, evaluaciones ilimitadas y 1 GB/curso.
