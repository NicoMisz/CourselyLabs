# Progreso del estudiante

> Tracking de lecciones completadas y posición en vídeos. Usado para barra de progreso, "continuar aprendiendo" en la home, y validación de prerequisitos.

---

## Modelo

```
lesson_progress:
    user_id                UUID FK
    lesson_id              UUID FK
    completed              BOOLEAN
    completed_at           TIMESTAMP NULL
    last_position_seconds  INT (para vídeos)
    updated_at             TIMESTAMP

PRIMARY KEY (user_id, lesson_id)
```

Migración: V3.

> **Nota**: no hay tabla `course_progress`. El progreso del curso se calcula on-demand agregando filas de `lesson_progress`.

---

## Cálculos

### Progreso de una lección

Se considera **completada** cuando:
- El alumno la marca manualmente con el botón "Marcar como completada", o
- Termina de ver el vídeo (>= 90% reproducido), o
- Aprueba todas las evaluaciones de los bloques assessment-type de la lección (vía `markCompleted` invocado por `SubmissionService.grade()` o `AttemptService.submitQuiz()` cuando el score >= passing).

Botón "Marcar completada" **se oculta** si la lección tiene bloques assessment-type, porque la completación viene determinada por aprobar las evaluaciones.

### Progreso de un curso

```
progress_percent = (lecciones_completadas / total_lecciones) × 100

donde:
- total_lecciones = COUNT(lessons WHERE section.course_id = X)
- lecciones_completadas = COUNT(lesson_progress WHERE user_id = U AND lesson.section.course_id = X AND completed = TRUE)
```

Devuelto por `GET /api/courses/{id}/progress` como:
```json
{
  "totalLessons": 12,
  "completedLessons": 7,
  "progressPercent": 58,
  "completedLessonIds": ["uuid1", "uuid2", ...]
}
```

---

## Flujos

### Marcar como completada (manual)

```
1. Alumno en /cursos/{slug}/leccion/{id}
2. Click en "Marcar como completada"
3. POST /api/lessons/{id}/toggle-complete
4. Backend:
   - Busca o crea LessonProgressEntity (user, lesson)
   - Toggle de completed
   - Si pasa a true → completed_at = now()
   - Si pasa a false → completed_at = null
5. Frontend:
   - Refresca courseProgress
   - Actualiza estado del botón
   - Si progress_percent === 100 → muestra dialog "¡Has completado el curso!"
```

### Auto-complete por vídeo

```
1. Alumno reproduce un bloque tipo video
2. LessonVideoPlayer emite eventos:
   - time-update (currentTime, duration)  — cada cierto tiempo
   - ended — al terminar
3. LessonView:
   - En time-update: si currentTime/duration >= 0.9 y no está completada → handleToggleComplete()
   - En ended: si no está completada → handleToggleComplete(); luego goToNext()
```

### Auto-complete por evaluación aprobada

```
1. Alumno envía un quiz / project / open_text
2. Si es quiz: AttemptService.submitQuiz autocorrige; si pasa, llama a LessonProgressService.markCompleted(userId, lessonId)
3. Si es project / open_text: el instructor califica desde /instructor/calificar; si la nota >= passing, SubmissionService.grade() llama a markCompleted
```

> **Importante**: si una lección tiene **varios** bloques assessment-type, todos deben aprobarse para que se marque como completada. Hoy `markCompleted` se llama tras cada uno aprobado individualmente — la implementación tendría que verificar que los demás también están aprobados antes de marcar completed. Verificar este caso edge.

### Resume de vídeo

Cada ~10 s, el frontend manda la posición actual del vídeo:

```
PATCH /api/lessons/{id}/position {seconds: 245}
```

El backend actualiza `lesson_progress.last_position_seconds`. La próxima vez que el alumno vuelva a la lección, aparece un banner "¿Continuar desde 4:05?" con botones Sí / Empezar de nuevo.

---

## Backend

### Endpoints

| Método | Path | Auth | Descripción |
|---|---|---|---|
| `GET` | `/api/courses/{id}/progress` | autenticado | Progreso global del curso para el usuario actual |
| `GET` | `/api/lessons/{id}/progress` | autenticado | Progreso de una lección |
| `POST` | `/api/lessons/{id}/toggle-complete` | autenticado | Toggle manual |
| `PATCH` | `/api/lessons/{id}/position` | autenticado | Guardar `last_position_seconds` |

### Servicios

- `LessonProgressService` — todo lo de progreso.
  - `getCourseProgress(courseId, userId)` — agrega.
  - `getLessonProgress(lessonId, userId)` — devuelve fila o crea con defaults.
  - `toggleCompleted(lessonId, userId)` — toggle manual.
  - `markCompleted(userId, lessonId)` — invocado por otros servicios.
  - `updatePosition(lessonId, userId, seconds)` — actualiza posición de vídeo.

### Tablas

- `lesson_progress` (V3).

---

## Frontend

### Componentes y vistas

- **`views/LessonView.vue`**: maneja el ciclo completo. `handleToggleComplete`, `handleVideoEnded`, `handleTimeUpdate`, `startPositionTracking` (interval cada 10 s), banner de resume.
- **`views/MyCoursesView.vue`**: lista de inscripciones del alumno con barra de progreso de cada uno.
- **`views/HomeView.vue`** sección "Continuar aprendiendo": cards de cursos con progreso > 0% y < 100%, ordenados por última actividad.
- **`components/CourseCardEnrolled.vue`**: card con barra de progreso embedida.
- **`components/CourseNavSidebar.vue`**: sidebar lateral durante la lección, marca lecciones completadas con `check_circle`, muestra "X / Y lecciones (Z%)".

### API client

- `api/progress.ts` — `getCourseProgress`, `getLessonProgress`, `toggleLessonComplete`, `updateLessonPosition`.

### Tipos

```ts
// types/progress.ts
interface CourseProgress {
  totalLessons: number
  completedLessons: number
  progressPercent: number  // 0-100
  completedLessonIds: string[]
}

interface LessonProgress {
  lessonId: string
  completed: boolean
  completedAt: string | null
  lastPositionSeconds: number
}
```

---

## Limitaciones conocidas

- **El cálculo de progreso de curso es on-demand** (no cacheado). Para cursos con muchas lecciones puede ser caro. Considerar materializar como columna `progress_percent` en `enrollments` o cachear con TTL si el rendimiento lo pide.
- **El throttle de posición es 10 s**. Si el alumno cierra el navegador entre intervalos puede perder hasta 10 s de "memoria". Aceptable para el caso de uso.
- **`markCompleted` por evaluación aprobada** no comprueba si hay otras evaluaciones en la misma lección que aún no se han aprobado — se ejecuta tras cada aprobación. Verificar y, si hace falta, ajustar para que solo marque completed cuando **todas** las evaluaciones de la lección están aprobadas.
- **No hay histórico de actividad** — solo el estado actual. Para mostrar gráficos de "actividad de los últimos 7 días" haría falta una tabla `activity_log`.

---

## Referencias en código

- Backend: `service/LessonProgressService.java`, `controller/LessonProgressController.java`
- Backend entidad: `LessonProgressEntity.java`
- Migración: `V3__create_lesson_progress.sql`
- Frontend: `api/progress.ts`, `views/LessonView.vue`, `components/CourseCardEnrolled.vue`, `components/CourseNavSidebar.vue`
