# Modelo de datos

> Esquema relacional vivo del proyecto. Cada cambio de schema va por una migración Flyway en `CourselyLabs-back/src/main/resources/db/migration/`. Para añadir una nueva ver [`04-backend/migrations.md`](../04-backend/migrations.md).

---

## Diagrama relacional simplificado

```
┌─────────┐     ┌──────────────┐     ┌──────────┐
│  users  │─────│ enrollments  │─────│ courses  │
└─────────┘     └──────────────┘     └──────────┘
     │                                    │
     ├──< course_instructors >─────────────┤
     │                                    │
     ├──< subscriptions (Premium/Stripe)  │
     │                                    │
     ├──< payments                        │
     │                                    │
     ├──< verification_tokens             │
     │                                    │
     ├──< refresh_tokens                  │
     │                                    │
     ├──< lesson_progress                 │
     │                                    │
     └──< reviews                         │
                                          │
            ┌─────────────────────────────┤
            ▼                             │
      ┌──────────┐                ┌──────────────────────┐
      │ sections │                │ course_prerequisites │
      └──────────┘                └──────────────────────┘
            │
            ▼
       ┌─────────┐
       │ lessons │
       └─────────┘
            │
            ├──< lesson_resources         (archivos en MinIO)
            │
            ├──< lesson_blocks            (V11: contenedor de bloques)
            │       │
            │       └─── 1:1 ── assessments (block_id UNIQUE)
            │                       │
            │                       ├──< quiz_questions ──< quiz_options
            │                       │
            │                       └──< assessment_attempts
            │                               │
            │                               ├──< quiz_answers
            │                               │
            │                               └─── 1:1 ── submissions
            │
            └──< lesson_progress
```

---

## Tablas

### Usuarios y autenticación

#### `users`
| Columna | Tipo | Notas |
|---|---|---|
| `id` | UUID PK | |
| `first_name`, `last_name` | VARCHAR | |
| `email` | VARCHAR UNIQUE | |
| `password` | VARCHAR | bcrypt |
| `role` | VARCHAR(20) | `admin` / `premium` / `user` |
| `email_verified` | BOOLEAN | |
| `bio`, `avatar_url` | VARCHAR | opcionales |
| `created_at`, `updated_at` | TIMESTAMP | |

#### `verification_tokens` (V4)
Tokens de un solo uso para verificar email tras registro. Caducan.

#### `refresh_tokens`
Tokens persistentes para refrescar el JWT de acceso. Eliminados al cerrar sesión.

### Cursos y contenido

#### `courses`
| Columna | Tipo | Notas |
|---|---|---|
| `id` | UUID PK | |
| `title`, `description`, `short_description` | VARCHAR/TEXT | |
| `slug` | VARCHAR UNIQUE | URL-friendly |
| `thumbnail_url` | VARCHAR | |
| `category_id` | INT FK | tabla `categorias` |
| `level` | VARCHAR | `beginner` / `intermediate` / `advanced` |
| `is_free` | BOOLEAN | `false` = curso Premium |
| `status` | VARCHAR | `draft` / `pending_review` / `published` / `rejected` |
| `rejection_reason` | TEXT | V6 |
| `created_by` | UUID FK | V5, owner del curso |
| `storage_bytes` | BIGINT | uso acumulado de MinIO |
| `created_at`, `updated_at`, `published_at` | TIMESTAMP | |

#### `course_instructors`
Tabla N:M entre `users` y `courses` para múltiples instructores. Campo `is_main` indica el principal (el `created_by`).

#### `categorias`
Catálogo cerrado de categorías. Edición solo admin.

#### `course_prerequisites` (V8)
| Columna | Tipo |
|---|---|
| `course_id` | UUID FK |
| `prerequisite_course_id` | UUID FK |
| `completion_threshold` | INT (0-100, % mínimo del prereq para desbloquear) |

#### `sections`
Agrupación dentro de un curso. Tiene `position` para reorden.

#### `lessons`
| Columna | Tipo | Notas |
|---|---|---|
| `id` | UUID PK | |
| `section_id` | UUID FK | |
| `title`, `description` | VARCHAR/TEXT | |
| `type` | VARCHAR(20) **NULLABLE** | **legacy desde V11** |
| `content_text`, `content_url` | TEXT/VARCHAR | **legacy desde V11** |
| `duration` | INT | minutos |
| `position` | INT | reorden dentro de sección |
| `is_free` | BOOLEAN | preview público |

A partir de V11, el contenido vive en `lesson_blocks`. Los campos `type`, `content_text`, `content_url` se mantienen por compatibilidad pero no se usan para renderizar.

#### `lesson_blocks` (V11)
| Columna | Tipo | Notas |
|---|---|---|
| `id` | UUID PK | |
| `lesson_id` | UUID FK | ON DELETE CASCADE |
| `type` | VARCHAR(20) | `text` / `video` / `pdf` / `quiz` / `project` / `open_text` |
| `position` | INT | orden |
| `text_content` | TEXT | si `type=text` |
| `video_url` | VARCHAR(500) | si `type=video` |
| `pdf_url` | VARCHAR(500) | si `type=pdf` |

#### `lesson_resources` (V9)
Archivos descargables adjuntos a una lección. Persistidos como referencia + `storage_key` apuntando a MinIO.

### Inscripciones y progreso

#### `enrollments`
N:M entre `users` y `courses` con `enrolled_at`. La existencia de la fila implica acceso al contenido del curso.

#### `lesson_progress` (V3)
Por cada lección que un alumno empieza:
- `last_position_seconds` (vídeo)
- `completed` (boolean)
- `completed_at`

#### `reviews`
Valoraciones de alumnos sobre cursos. `rating` 1-5, `comment` opcional.

### Pagos (Stripe)

#### `subscriptions` (V7)
| Columna | Tipo | Notas |
|---|---|---|
| `id` | UUID PK | |
| `user_id` | UUID FK | |
| `stripe_subscription_id` | VARCHAR UNIQUE | |
| `status` | VARCHAR | `active` / `past_due` / `canceled` / etc. |
| `current_period_start`, `current_period_end` | TIMESTAMP | |

#### `payments`
Histórico de pagos de Stripe (eventos webhook validados).

### Evaluaciones (V10 + V11)

#### `assessments`
| Columna | Tipo | Notas |
|---|---|---|
| `id` | UUID PK | |
| `lesson_id` | UUID FK **NULLABLE** | legacy desde V11 |
| `block_id` | UUID FK **UNIQUE** (V11) | 1:1 con `lesson_blocks` |
| `type` | VARCHAR(20) | `quiz` / `project` / `open_text` |
| `description` | TEXT | enunciado o instrucciones |
| `max_attempts` | INT | default 3 |
| `time_limit_minutes` | INT | solo quiz, NULL = sin límite |
| `passing_score` | INT | 0-100, default 70 |
| `shuffle_options` | BOOLEAN | solo quiz |

#### `quiz_questions` + `quiz_options`
Preguntas de respuesta múltiple. Cada `quiz_option` tiene `is_correct` y `explanation` opcional.

#### `assessment_attempts`
Cada vez que un alumno empieza una evaluación. Limitado por `max_attempts`. Status: `in_progress` / `submitted` / `graded` / `expired`.

#### `quiz_answers`
Respuestas concretas de un attempt de quiz (selección por pregunta).

#### `submissions`
| Columna | Tipo | Notas |
|---|---|---|
| `attempt_id` | UUID FK UNIQUE | 1:1 con un attempt |
| `type` | VARCHAR(20) | `project` / `open_text` |
| `storage_key`, `file_name`, `file_size` | VARCHAR/BIGINT | si project |
| `answer_text` | TEXT | si open_text |
| `instructor_feedback` | TEXT | tras calificar |
| `graded_at`, `graded_by` | TIMESTAMP/UUID | |

---

## Relaciones clave (cardinalidades)

| Origen | Destino | Cardinalidad |
|---|---|---|
| `users.created_by` | `courses` | 1:N (un user crea muchos cursos) |
| `users` | `enrollments` ↔ `courses` | N:M |
| `users` | `course_instructors` ↔ `courses` | N:M |
| `courses` | `sections` | 1:N |
| `sections` | `lessons` | 1:N |
| `lessons` | `lesson_blocks` | 1:N |
| `lesson_blocks` | `assessments` | 1:1 (vía `block_id UNIQUE`) |
| `assessments` | `quiz_questions` | 1:N |
| `quiz_questions` | `quiz_options` | 1:N |
| `assessments` | `assessment_attempts` | 1:N |
| `assessment_attempts` | `submissions` | 1:1 (vía `attempt_id UNIQUE`) |
| `users` | `subscriptions` | 1:N (histórico de suscripciones) |
| `users` | `lesson_progress` ↔ `lessons` | N:M con metadatos |

---

## Migraciones Flyway (orden cronológico)

| V | Contenido | Fecha aprox. |
|---|---|---|
| V1 | Schema base: users, courses, categorias, sections, lessons | inicio |
| V2 | Sections + lessons CRUD completo | |
| V3 | `lesson_progress` | |
| V4 | `verification_tokens` | |
| V5 | `created_by` en courses | |
| V6 | `rejection_reason` para flujo de revisión | |
| V7 | `subscriptions` + `payments` (Stripe) | |
| V8 | `course_prerequisites` con threshold | |
| V9 | `lesson_resources` (MinIO) | |
| V10 | `assessments`, `quiz_*`, `attempts`, `submissions`, `quiz_answers` | |
| V11 | `lesson_blocks` + migración de datos + `block_id` en assessments | 2026-04 |

> El schema base completo también vive en `init_db/01_schema.sql` para inicialización limpia (PostgreSQL lo ejecuta al primer arranque del contenedor).

---

## Convenciones de schema

- **PKs**: `UUID` salvo `categorias` que usa `INT` (catálogo cerrado).
- **FKs**: con `ON DELETE CASCADE` cuando la dependencia es total (`lesson_blocks` se borran si la lección se borra) y sin cascada cuando hay que preservar histórico (`payments` no se borra si el usuario se borra — eso podría ser una decisión legal).
- **Timestamps**: `created_at` con `DEFAULT now()`, `updated_at` con trigger `BEFORE UPDATE` o gestión Hibernate (`@CreationTimestamp`, `@UpdateTimestamp`).
- **Naming**: snake_case en SQL, camelCase en Java/TS.
- **Tipos VARCHAR**: longitud explícita en columnas que serán indexadas; `TEXT` para contenido libre largo.
- **Booleans**: nunca nullable. `false` por defecto donde aplica.
