# Cursos y lecciones

> Modelo y flujo de creación, edición y publicación. Para el contenido **dentro** de una lección ver [`lesson-blocks.md`](lesson-blocks.md). Para evaluaciones ver [`assessments.md`](assessments.md).

---

## Modelo

```
Course
 ├─ created_by → User (owner)
 ├─ course_instructors → Users (co-instructores)
 ├─ category → Categoria (catálogo cerrado)
 ├─ status: draft / pending_review / published / rejected
 ├─ is_free: boolean (false = curso Premium)
 ├─ thumbnail_url, slug, level, short_description, description
 ├─ storage_bytes (auto-actualizado)
 │
 └─ Sections (ordenados por position)
     │
     └─ Lessons (ordenadas por position)
         │
         ├─ is_free: vista pública aunque el curso sea Premium
         │
         ├─ Lesson Blocks (ver lesson-blocks.md)
         ├─ Lesson Resources (descargables, ver storage.md)
         └─ Lesson Progress (por usuario)
```

---

## Estados del curso

```
draft ──────────────► pending_review ──────► published
  │                          │                    ▲
  │                          ▼                    │
  └──── (vuelve a draft     rejected ─────────────┘
        si admin pide        (con rejection_reason)
        cambios)
```

| Estado | Visible al público | Editable por instructor | Acceso a contenido |
|---|---|---|---|
| `draft` | ❌ | ✅ | Solo el instructor / admin |
| `pending_review` | ❌ | ⚠️ Limitado (no se puede borrar) | Solo el instructor / admin |
| `published` | ✅ | ✅ (cambios menores; cambios mayores requieren re-revisión) | Inscritos + instructor / admin |
| `rejected` | ❌ | ✅ (con `rejection_reason` mostrado) | Solo el instructor / admin |

---

## Flujo de creación

### 1. Crear curso

`POST /api/courses {title, slug, ...}` → status `draft`. El usuario actual queda como `created_by` y como `is_main` instructor.

### 2. Editar en CourseWizard

Frontend en `/instructor/cursos/{id}/editar`:

Sidebar:
- **Información básica**: título, slug, descripción corta, categoría, nivel.
- **Detalles**: descripción completa (rich text), thumbnail, premium toggle.
- **Prerequisitos**: lista de cursos requeridos (Premium-only).
- **Contenido**: árbol de secciones → lecciones, con drag-and-drop.

Panel principal: editor del elemento seleccionado.

### 3. Añadir secciones

`POST /api/courses/{courseId}/sections {title, position}`. Sin lecciones inicialmente.

### 4. Añadir lecciones

`POST /api/sections/{sectionId}/lessons {title}`. Lección vacía (sin bloques). El instructor configura los bloques desde el panel de lección con el botón `+ Añadir bloque` (ver [`lesson-blocks.md`](lesson-blocks.md)).

### 5. Subir thumbnail

`POST /api/courses/{id}/thumbnail` (multipart). El backend valida tipo (JPG/PNG/WebP/GIF) y tamaño (≤ 5 MB), sube a MinIO, devuelve URL pública.

### 6. Enviar a revisión

`POST /api/courses/{id}/submit-for-review` → cambia `status` a `pending_review`. El frontend muestra un diálogo de pre-flight con checks:

- ✅ Tiene título (≥ 3 caracteres)
- ✅ Descripción ≥ 20 caracteres (HTML stripped)
- ✅ Categoría seleccionada
- ✅ Al menos 1 sección
- ✅ Al menos 1 lección en alguna sección

Si alguno falla, el botón "Enviar" se deshabilita.

### 7. Admin aprueba/rechaza

En `/admin/cursos`, un admin ve la cola y:
- **Aprueba** → `POST /api/courses/{id}/approve` → `status = published`, `published_at = now()`.
- **Rechaza** con motivo → `POST /api/courses/{id}/reject {reason}` → `status = rejected`, `rejection_reason = reason`. El instructor lo ve y puede volver a editar y reenviar.

---

## Permisos

| Acción | Quién |
|---|---|
| Crear curso | Cualquier user autenticado (sujeto a límite de cursos por rol — ver `product-spec.md`) |
| Editar curso | Owner, instructor co-asignado, admin |
| Eliminar curso | Owner, admin (no instructor co-asignado) |
| Aprobar/rechazar | Solo admin |
| Submit-for-review | Owner, instructor (no admin — el admin ya puede publicar directo si quiere, o crear con admin role) |
| Crear curso Premium (`is_free = false`) | Premium o admin |

---

## Backend

### Endpoints (resumen)

Lista completa en [`04-backend/api.md`](../04-backend/api.md). Los más relevantes:

| Método | Path | Notas |
|---|---|---|
| `GET` | `/api/courses/all` | Solo published |
| `GET` | `/api/courses/search?...` | Con filtros |
| `GET` | `/api/courses/by-slug/{slug}` | Vista pública |
| `GET` | `/api/courses/{id}/edit` | Vista de edición (incluye `created_by`, `status`, etc.) |
| `POST` | `/api/courses` | Crear |
| `PATCH` | `/api/courses/{id}` | Actualizar |
| `POST` | `/api/courses/{id}/submit-for-review` | |
| `POST` | `/api/courses/{id}/approve` | Solo admin |
| `POST` | `/api/courses/{id}/reject` | Solo admin |

### Servicios clave

- `CourseService` — CRUD, validaciones de límites de cursos por rol, cálculo de `storage_bytes` agregado.
- `SectionService` — CRUD + reorden.
- `LessonService` — CRUD + reorden.

### Validaciones de negocio

- **Slug único**.
- **Imagen del thumbnail**: JPG/PNG/WebP/GIF ≤ 5 MB.
- **Storage del curso**: la suma de `lesson_resources.file_size` + tamaños de bloques (vídeo + PDF de bloques) no debe superar el límite del rol del owner.
- **No puedes publicar un curso vacío** (sin secciones o sin lecciones).
- **No puedes hacer un curso Premium si no eres Premium o admin**.
- **Cantidad máxima de cursos por rol**: 2 (`user`), 10 (`premium`), ∞ (`admin`).

---

## Frontend

### Vistas

- **Catálogo público**: `views/CoursesView.vue`. Search bar + filtros (categoría, nivel, gratis/premium). Pagina infinita o numérica.
- **Detalle público**: `views/CourseDetailView.vue`. Tabs: Contenido, Descripción, Valoraciones, Instructores, Prerequisitos. Botón de inscripción si autenticado.
- **Editor de curso**: `views/instructor/CourseWizard.vue`. ~1300 líneas (candidato a refactor). Sidebar + panel principal.
- **Lista del instructor**: `views/instructor/InstructorCourseList.vue`. Cards con estado del curso y acciones (editar, eliminar, ver, enviar a revisión).
- **Cola admin**: `views/admin/AdminCourseQueue.vue`. Lista de pending con preview, botones aprobar/rechazar.

### Componentes

- `CourseCard.vue`, `CourseCardEnrolled.vue` — tarjetas en el catálogo.
- `CourseHero.vue` — header del detalle.
- `CourseSectionList.vue` — listado de secciones expandibles con lecciones.
- `CoursePrerequisitesTab.vue`, `CoursePrerequisites.vue` — prerequisitos en detalle.
- `CourseSidebar.vue` — sidebar de inscripción / acceso.
- `CourseTabContent.vue`, `CourseTabDescription.vue`, `CourseTabReviews.vue`, `CourseTabInstructors.vue` — tabs.
- `LevelBadge.vue`, `PriceBadge.vue` — badges visuales.
- `CourseBreadcrumb.vue` — breadcrumb.

### API clients

- `api/course.ts` — fetch público (catálogo, detalle).
- `api/courseSearch.ts` — búsqueda con filtros.
- `api/instructor.ts` — operaciones de owner (crear, actualizar, submit, approve, reject…).
- `api/lesson.ts` — secciones y lecciones (públicas).
- `api/sectionEditor.ts`, `api/lessonEditor.ts` — CRUD desde el wizard.

---

## Limitaciones conocidas

- **`CourseWizard.vue` es muy grande** (~1300 líneas). Mezcla muchas responsabilidades. Refactor pendiente en componentes más pequeños.
- **No hay versionado de cursos**: si un instructor cambia un curso publicado, los alumnos ya inscritos ven el cambio inmediatamente. Para revisiones mayores que requieran re-aprobación admin, falta un mecanismo.
- **No hay borrado lógico**: `DELETE` borra de verdad. Si el curso tiene inscripciones, debería preservarse el histórico.
- **`is_main` en `course_instructors`** — establecido al crear el curso pero no hay UI para cambiar el principal después.
- **Reorden de lecciones**: hay endpoint pero la UI todavía no lo expone bien (`vuedraggable` está usado para bloques, no para lecciones todavía).

---

## Referencias en código

- Backend: `service/CourseService.java`, `service/SectionService.java`, `service/LessonService.java`
- Backend controllers: `CourseController.java`, `SectionController.java`, `LessonController.java`
- Frontend: `views/instructor/CourseWizard.vue`, `views/CoursesView.vue`, `views/CourseDetailView.vue`
