# Prerequisitos entre cursos

> Permite enlazar cursos para crear rutas de aprendizaje. Un curso puede requerir haber completado otros con un % mínimo.

---

## Modelo

```
course_prerequisites:
    course_id              UUID FK → courses
    prerequisite_course_id UUID FK → courses
    completion_threshold   INT (0..100, default 80)

PRIMARY KEY (course_id, prerequisite_course_id)
CHECK (course_id ≠ prerequisite_course_id)  -- no auto-prereq
```

Migración: V8 (`V8__course_prerequisites.sql`).

---

## Reglas

- Un curso puede tener **N prerequisitos** (sin límite hard).
- Cada prereq tiene su propio **threshold** (% mínimo del prereq que el alumno debe haber completado para desbloquear este curso).
- Por defecto, threshold = 80.
- **Configurar prerequisitos es feature Premium**: solo `premium` y `admin` pueden crear/editar. Cualquier alumno puede ver los prerequisitos de un curso.
- **No se permiten ciclos** — el backend valida que el grafo siga siendo acíclico (DAG).

---

## Flujos

### Configurar prerequisitos (instructor Premium)

1. En `CourseWizard`, sidebar → **Prerequisitos**.
2. Si el instructor no es Premium, ve un banner Premium-lock con CTA a `/premium` y la edición está deshabilitada.
3. Si es Premium, ve un selector multi-curso (búsqueda) y por cada curso seleccionado un slider 0-100 para el threshold.
4. `PUT /api/courses/{id}/prerequisites {prerequisites: [{prerequisiteCourseId, completionThreshold}]}` sincroniza la lista (replace, no append).

### Comprobar prerequisitos al inscribirse (alumno)

```
1. Alumno pulsa "Inscribirse" en /cursos/{slug}
2. Frontend → POST /api/enrollments {courseId}
3. Backend (EnrollmentService):
   a. Comprueba acceso (Premium si curso Premium)
   b. Para cada prerequisito del curso:
      - Calcula progreso del alumno en el prereq
      - Si progreso < threshold → 400 BadRequest "Prerequisito no cumplido: {curso}"
   c. Si todos OK → crea EnrollmentEntity
4. Frontend muestra confirmación o, si falla, redirige a /cursos/{prereq-slug} con un banner
```

### Ver prerequisitos en el detalle del curso

`/cursos/{slug}` muestra una pestaña "Prerequisitos" cuando los hay:

- Para cada prereq: card del curso + threshold + estado (✅ cumplido / ❌ pendiente con N% / 80%).
- Si el alumno está autenticado, se muestra su progreso.
- Si los prereqs no están cumplidos, el botón de inscripción se reemplaza por uno que dice "Necesitas completar X primero".

---

## Backend

### Endpoints

| Método | Path | Auth | Descripción |
|---|---|---|---|
| `GET` | `/api/courses/{id}/prerequisites` | público | Lista prerequisitos del curso (con título y threshold) |
| `PUT` | `/api/courses/{id}/prerequisites` | owner/instructor + premium o admin | Sincronizar (replace) lista |
| `GET` | `/api/courses/{id}/prerequisites/check` | autenticado | `{cumple: boolean, faltantes: [{courseId, courseTitle, currentPct, requiredPct}]}` |

### Validaciones

- **No auto-prereq**: el SQL CHECK constraint lo impide.
- **No ciclos**: comprobado en `CoursePrerequisiteService.validateNoCycles()` antes de guardar.
- **Threshold 0-100**: validación en DTO.
- **Premium check**: en el service, `assertPremiumOrAdmin(email)`. El `user` libre recibe 401 con mensaje claro.

### Servicios

- `CoursePrerequisiteService` — CRUD + check + cycle detection.
- `EnrollmentService` — orquesta la verificación al inscribirse.
- `LessonProgressService.getCourseProgress(courseId, userId)` — devuelve % completado, usado para el threshold check.

### Tablas

- `course_prerequisites` (V8).
- Lectura usa joins con `courses` para devolver título.

---

## Frontend

### Componentes

- **Editor (en CourseWizard)**: panel "Prerequisitos" con `q-select` multi-input + sliders.
- **Visualización (detalle de curso)**: `components/CoursePrerequisitesTab.vue` o `CoursePrerequisites.vue`.
- **Banner de bloqueo**: `components/PrerequisiteBlockBanner.vue` — aparece en `/cursos/{slug}` si los prereqs no están cumplidos.
- **Selector**: `components/PrerequisiteSelector.vue` — búsqueda asíncrona de cursos.

### API client

- `api/prerequisite.ts` — `getCoursePrerequisites`, `syncCoursePrerequisites`, `checkPrerequisitesForCourse`.
- `api/instructorPrerequisites.ts` — endpoints específicos del flujo instructor (si aplica).

---

## Limitaciones conocidas

- **No hay grafo visual** de prereqs en la UI; solo lista plana. Para cursos con muchos enlaces sería útil un diagrama.
- **El alumno puede ver el porcentaje "actual / requerido" pero no qué lección concreta le falta** — útil añadir un drill-down.
- **No hay "prereqs blandos"** (sugeridos vs obligatorios) — todos son obligatorios hoy.
- **Cambiar el threshold de un prereq cuando ya hay alumnos inscritos** no afecta a los ya inscritos. Esto puede ser deseable o no según el caso.
- **Sin cascada**: si A es prereq de B, y B es prereq de C, para inscribirte en C se valida solo B (no A indirectamente). Si querías que la cascada se propague, hay que validar el grafo entero.

---

## Referencias en código

- Backend: `service/CoursePrerequisiteService.java`, `controller/CoursePrerequisiteController.java`
- Backend entidad: `CoursePrerequisiteEntity.java`
- Migración: `V8__course_prerequisites.sql`
- Frontend: `api/prerequisite.ts`, `components/CoursePrerequisitesTab.vue`, `components/PrerequisiteBlockBanner.vue`
