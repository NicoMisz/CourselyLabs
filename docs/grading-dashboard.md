# Dashboard de calificación (`/instructor/calificar`)

> Vista única para que el instructor califique entregas pendientes (proyectos y preguntas abiertas) de **todos** sus cursos en un solo sitio.
>
> Introducida con el refactor multi-bloque (rama `feature/assessments`).

---

## Por qué existe

Antes, las entregas pendientes se mostraban dentro del **editor de evaluación** (en `CourseWizard.vue` → bloque assessment-type). Esto tenía varios problemas:

1. **Distracción durante la autoría**: el instructor estaba creando contenido y veía notificaciones de calificación.
2. **Por curso**: tenías que entrar curso por curso y bloque por bloque para ver qué calificar.
3. **Fragmentación**: si un curso tenía 5 lecciones con evaluaciones, había 5 listados separados.

El dashboard unifica toda la cola de calificación en **una sola vista** con filtro por curso.

---

## UX de la vista

### Acceso
- Enlace en el sidebar de instructor (`InstructorLayout.vue`): item **"Calificar"** con icono `assignment_turned_in`.
- Ruta: `/instructor/calificar`.

### Layout

```
┌──────────────────────────────────────────────────────┐
│ Calificar entregas                                   │
│ Aquí puedes ver todas las entregas pendientes...     │
│                                                      │
│ [Filtrar por curso ▾]              [↻ Actualizar]   │
├──────────────────────────────────────────────────────┤
│ ◉ Estudiante 1            [Proyecto]                │
│   Curso · Lección                                    │
│   2026-04-28 14:32 · archivo.pdf                     │
│                                          [↗] [>]    │
├──────────────────────────────────────────────────────┤
│ ◉ Estudiante 2            [Pregunta abierta]        │
│   Otro curso · Otra lección                          │
│   2026-04-28 09:15                                   │
│                                          [↗] [>]    │
└──────────────────────────────────────────────────────┘
```

- **Filtro de curso**: dropdown poblado con los cursos que tienen entregas pendientes (no muestra todos los cursos del instructor, solo los relevantes).
- **Botón [↗]** (`open_in_new`): navega al editor del bloque concreto (deep-link, ver §[Deep-link al bloque](#deep-link-al-bloque)).
- **Click en la fila** (excepto sobre el botón): abre el diálogo de calificación.

### Diálogo de calificación

Al hacer click en una entrega:

1. Si es un **proyecto**: aparece un botón "Descargar archivo" (signed URL de MinIO).
2. Si es una **pregunta abierta**: aparece el texto que envió el estudiante.
3. Inputs: `Puntuación (0-100)` y `Feedback para el estudiante`.
4. Acciones: `Cancelar` / `Calificar`.

Tras calificar correctamente:
- Se hace `PATCH /api/submissions/{id}/grade`.
- Si la nota >= `passingScore` del assessment, el `LessonProgressService` marca la lección completada para ese alumno (efecto secundario en backend).
- La entrega desaparece del listado (recarga automática).

### Deep-link al bloque

Junto al `chevron_right` cada entrega tiene un botón `open_in_new` con tooltip "Ver bloque en el editor". Al pulsarlo:

```ts
router.push({
  path: `/instructor/cursos/${s.courseId}/editar`,
  query: { lesson: s.lessonId, block: s.blockId },
})
```

`CourseWizard.vue` lee esos query params en `applyDeepLinkFromQuery()`:

1. Selecciona la lección correspondiente en el sidebar.
2. Cuando los bloques se renderizan, hace `scrollIntoView` al elemento `#block-{blockId}`.
3. Aplica la clase CSS `.block-highlight` durante 2.2 s para destacarlo (animación amarillo→transparente).

Esto permite al instructor ir de la entrega → al bloque exacto que la generó → revisar enunciado/configuración → volver a calificar, sin perder contexto.

---

## Backend

### Endpoint

```
GET /api/grading/pending
```

- **Auth**: cualquier usuario autenticado.
- **Comportamiento**:
  - Si el usuario es `admin` → devuelve **todas** las entregas pendientes del sistema.
  - En cualquier otro caso → devuelve solo las entregas pendientes de cursos donde el usuario es `created_by` (owner) o `course_instructors.instructor_id` (instructor co-asignado).
- **Respuesta**: `List<PendingSubmissionDTO>` ordenada por `createdAt ASC` (las más antiguas primero).

### `PendingSubmissionDTO`

Incluye toda la información necesaria para renderizar el dashboard sin más roundtrips:

```java
{
    id: UUID,
    attemptId: UUID,
    type: "project" | "open_text",
    fileName: String,
    fileSize: Long,
    answerText: String,         // null para project
    createdAt: LocalDateTime,

    studentId: UUID,
    studentName: String,        // firstName + lastName

    courseId: UUID,
    courseTitle: String,

    lessonId: UUID,
    lessonTitle: String,

    blockId: UUID,              // para deep-link
    assessmentId: UUID,
    assessmentType: "project" | "open_text"
}
```

### Query principal

`SubmissionRepository.findPendingForGrader(userId)`:

```java
@Query("SELECT s FROM SubmissionEntity s " +
       "WHERE s.gradedAt IS NULL " +
       "AND (s.attempt.assessment.lesson.section.course.createdBy.id = :userId " +
       "     OR EXISTS (SELECT 1 FROM CourseInstructorEntity ci " +
       "                WHERE ci.course.id = s.attempt.assessment.lesson.section.course.id " +
       "                AND ci.instructor.id = :userId)) " +
       "ORDER BY s.createdAt ASC")
List<SubmissionEntity> findPendingForGrader(@Param("userId") UUID userId);
```

Para admin, una query más simple:

```java
@Query("SELECT s FROM SubmissionEntity s WHERE s.gradedAt IS NULL ORDER BY s.createdAt ASC")
List<SubmissionEntity> findAllPending();
```

`GradingService.findPendingForUser(email)` decide cuál llamar según `user.role`.

---

## Frontend

### Archivos

- **Ruta**: `CourselyLabs-front/src/router/routes.ts` — entrada `/instructor/calificar`.
- **Vista**: `CourselyLabs-front/src/views/instructor/GradingDashboard.vue`.
- **API client**: `CourselyLabs-front/src/api/grading.ts` — `listPendingSubmissions()`.
- **Tipo**: `PendingSubmission` (en `api/grading.ts`, espejo del backend).
- **Sidebar**: item añadido en `CourselyLabs-front/src/layouts/InstructorLayout.vue`.

### i18n

Todos los strings del dashboard están migrados a `t()`. Claves bajo `instructor.grading.*`:

```json
"instructor": {
  "grading": {
    "pageTitle": "Calificar entregas",
    "pageSub": "Aquí puedes ver todas las entregas pendientes de calificar de tus cursos.",
    "filterByCourse": "Filtrar por curso",
    "allUpToDate": "Todo al día",
    "noPending": "No hay entregas pendientes de calificar.",
    "errorLoad": "No hemos podido cargar las entregas",
    "viewBlock": "Ver bloque en el editor",
    "gradeDialog": "Calificar entrega",
    "score": "Puntuación (0-100)",
    "feedback": "Feedback para el estudiante",
    "grade": "Calificar",
    "graded": "Entrega calificada",
    "gradeError": "Error al calificar",
    "downloadFile": "Descargar archivo",
    "downloadError": "Error al descargar"
  }
}
```

---

## Flujo end-to-end

### Caso típico: proyecto

1. Estudiante entra en una lección con un bloque `project`.
2. Sube un PDF (40 MB, validación de MIME y tamaño en `SubmissionService.submitProject`).
3. El archivo va a MinIO en la ruta `submissions/{courseId}/{lessonId}/{userId}/{attemptId}-{filename}`.
4. La entrega queda en `submissions` con `graded_at IS NULL`.
5. Instructor entra a `/instructor/calificar` → ve la entrega en el listado.
6. Click en la fila → diálogo. Click en "Descargar archivo" → abre signed URL de MinIO en pestaña nueva.
7. Instructor introduce nota (ej. 85) y feedback. Click en "Calificar".
8. `PATCH /api/submissions/{id}/grade` → `gradedAt = now()`, `gradedBy = instructor`, `attempt.score = 85`, `attempt.passed = (85 >= passingScore)`, `attempt.status = "graded"`.
9. Si `passed`, `LessonProgressService.markCompleted(studentId, lessonId)` se invoca como efecto secundario.
10. La entrega desaparece del listado (recarga automática).

### Caso: pregunta abierta

Igual que arriba pero el alumno envía texto (máx. 20.000 caracteres) en lugar de archivo. El diálogo de calificación muestra el texto en una caja `.answer-box` con scroll.

---

## Permisos y seguridad

- El **endpoint** `/api/grading/pending` solo requiere autenticación; **el filtrado lo hace el query** según `user.role`.
- El **diálogo de calificación** llama a:
  - `getSubmissionDownloadUrl(id)` — `@PreAuthorize("@courseSecurityService.canGradeSubmission(#id, authentication)")`.
  - `gradeSubmission(id, score, feedback)` — misma protección.
- Por tanto, aunque un usuario manipule la URL para llegar a una entrega que no le toca, las acciones de descarga y calificación **fallan a nivel backend**.

---

## Pendiente / mejoras posibles

- **Paginación** — hoy se cargan todas las pendientes del usuario en una sola request. Si el listado crece a cientos, conviene paginar (sería una v2).
- **Filtros adicionales**: por tipo (`project` / `open_text`), por antigüedad, por estudiante.
- **Ordenación interactiva** (hoy es solo `createdAt ASC`).
- **Notificaciones**: avisar al instructor por correo cuando entra una nueva entrega — depende de la feature de notificaciones aún no empezada.
- **Bulk-grade**: seleccionar varias entregas y aplicar misma nota/feedback (uso poco frecuente, valorar coste/beneficio).
- **Vista del estudiante**: ver su lista de entregas y feedback recibido — hoy solo lo ve dentro de cada lección.

---

## Referencias en código

- Backend service: `CourselyLabs-back/src/main/java/com/courselylabs/courselylab/service/GradingService.java`
- Backend controller: `CourselyLabs-back/src/main/java/com/courselylabs/courselylab/controller/GradingController.java`
- Backend repo: `CourselyLabs-back/src/main/java/com/courselylabs/courselylab/repository/SubmissionRepository.java` (queries `findPendingForGrader` / `findAllPending`)
- Backend DTO: `CourselyLabs-back/src/main/java/com/courselylabs/courselylab/dto/PendingSubmissionDTO.java`
- Frontend vista: `CourselyLabs-front/src/views/instructor/GradingDashboard.vue`
- Frontend API: `CourselyLabs-front/src/api/grading.ts`
- Deep-link en wizard: `CourselyLabs-front/src/views/instructor/CourseWizard.vue` → `applyDeepLinkFromQuery()`
