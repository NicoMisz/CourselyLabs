# Evaluaciones (quiz / project / open_text)

> Sistema de evaluación. Hoy las evaluaciones se vinculan a **bloques**, no a lecciones (V11). Para entender el cambio ver [`lesson-blocks.md`](lesson-blocks.md). Para la cola de calificación ver [`grading.md`](grading.md).

---

## Tipos

| Tipo | Cómo funciona | Calificación |
|---|---|---|
| `quiz` | Preguntas de respuesta múltiple. El alumno selecciona una opción por pregunta. Tiene timer opcional. | **Automática** al enviar. Score = (puntos correctos / puntos totales) × 100. Aprobado si >= `passing_score`. |
| `project` | El alumno sube un archivo (≤ 50 MB). MIME types permitidos: PDF, Word, Excel, PPTX, imágenes, ZIP, TXT, JSON, MD, CSV. | **Manual** por el instructor desde `/instructor/calificar`. |
| `open_text` | El alumno escribe respuesta libre (≤ 20.000 caracteres). | **Manual** por el instructor desde `/instructor/calificar`. |

---

## Modelo

```
LessonBlock (type ∈ {quiz, project, open_text})
    │ 1:1 (block_id UNIQUE)
    ▼
Assessment
    │
    ├─ description, max_attempts, time_limit_minutes (quiz),
    │  passing_score, shuffle_options (quiz)
    │
    ├─ Quiz:    QuizQuestion[] → QuizOption[]
    │
    └─ AssessmentAttempt[] (uno por intento del alumno)
           │
           ├─ Quiz:        QuizAnswer[] (selección por pregunta)
           │
           └─ Submission   (1:1 con attempt para project / open_text)
                  │
                  ├─ project:    storage_key, file_name, file_size
                  ├─ open_text:  answer_text
                  │
                  └─ instructor_feedback, graded_at, graded_by
```

---

## Flujos

### Crear una evaluación (instructor)

1. En `CourseWizard`, abrir una lección.
2. Pulsar `+ Añadir bloque` → seleccionar tipo `Cuestionario`, `Proyecto` o `Pregunta abierta` (sección "Evaluaciones" del dropdown).
3. El bloque se crea vacío. En el editor del bloque aparece el botón **"Configurar evaluación"**.
4. Al pulsarlo, `POST /api/blocks/{blockId}/assessment` crea la evaluación con valores por defecto (3 intentos, 70% para aprobar, sin time limit).
5. Aparece el formulario de configuración: descripción/enunciado, intentos máximos, time limit (solo quiz), passing score, shuffle options (solo quiz).
6. Si es **quiz**, se muestran las preguntas: añadir, editar, eliminar, marcar opción correcta, configurar puntos y explicaciones.
7. Si es **project** u **open_text**, no hay preguntas — solo el enunciado en `description`.

### Eliminar una evaluación

Botón "Eliminar evaluación" en el editor → `DELETE /api/assessments/{id}`. **El bloque se mantiene**; el instructor puede reconfigurar.

### Tomar una evaluación (alumno)

1. El alumno entra en la lección → ve el bloque assessment-type.
2. `AssessmentLessonView.vue` muestra el "intro": descripción, número de preguntas (quiz), time limit, passing score, intentos restantes.
3. Si tiene intentos disponibles y no hay uno abierto, pulsa **Empezar**.
4. `POST /api/assessments/{id}/start` → crea `AssessmentAttempt` con `status = 'in_progress'`. Devuelve el assessment con preguntas (sin respuestas correctas).
5. Según el tipo:
   - **Quiz**: `QuizRunner.vue` muestra preguntas con timer. El alumno selecciona, marca para revisar. Al enviar:
     - `POST /api/attempts/{id}/submit-quiz [QuizAnswer[]]`
     - Backend autocorrige, calcula score, marca attempt como `submitted` o directamente `graded` (en quiz no hay corrección manual).
     - Si pasa, se marca la lección como completada.
   - **Project**: `ProjectSubmitView.vue` con `FileUploader`. Al enviar archivo:
     - `POST /api/attempts/{id}/submit-project` (multipart) → MinIO + tabla `submissions`.
     - Attempt → `submitted`, esperando calificación manual.
   - **Open text**: `OpenTextSubmitView.vue` con textarea (contador de caracteres). Al enviar:
     - `POST /api/attempts/{id}/submit-open-text {answerText}`
     - Attempt → `submitted`, esperando calificación manual.
6. Resultado:
   - Quiz → `AssessmentResultsView.vue` muestra cada pregunta con la respuesta correcta y explicación.
   - Project / open_text → "Entrega enviada. Te avisaremos cuando se califique."

### Calificar (instructor)

Ver [`grading.md`](grading.md).

---

## Límites por rol

| Rol | Evaluaciones del mismo tipo por curso |
|---|---|
| `user` | Máx. 2 (de cada tipo: 2 quiz + 2 project + 2 open_text por curso) |
| `premium` | ∞ |
| `admin` | ∞ |

Si un user libre intenta crear la 3ª evaluación de un tipo, recibe 401 "Plan gratuito: límite de 2 evaluaciones de tipo 'X' por curso. Hazte Premium para crear sin límite."

---

## Validaciones

### Backend

- **Solo se puede crear una evaluación en un bloque assessment-type** (`quiz`/`project`/`open_text`). Si el bloque es `text`/`video`/`pdf`, se rechaza.
- **Solo una evaluación por bloque** (FK `block_id` UNIQUE en `assessments`).
- **Un attempt en `in_progress` no permite empezar otro** del mismo assessment.
- **Tras `max_attempts`, el usuario no puede empezar más** (incluso si todos fallaron).
- **Time limit** (quiz): si pasa, el attempt se autoenvía con `status = 'expired'` y score 0.
- **Submissions**: validación de tamaño y MIME type en backend.
- **Solo el alumno dueño del attempt** puede enviar (vía `canAccessAttempt`).

### Frontend

- Validación de tipos MIME y tamaño antes de subir (UX immediate feedback).
- Contador de caracteres en open_text (visualmente avisa al pasar de 18.000 / 20.000).
- Confirmación antes de enviar quiz si quedan preguntas sin marcar.

---

## Backend

### Endpoints

Lista completa en [`04-backend/api.md`](../04-backend/api.md).

Crear/editar:
- `POST /api/blocks/{blockId}/assessment`
- `PUT /api/assessments/{id}`
- `DELETE /api/assessments/{id}`
- `POST /api/assessments/{id}/questions` (solo quiz)
- `PUT /api/questions/{id}`, `DELETE /api/questions/{id}`

Tomar/intentar:
- `POST /api/assessments/{id}/start`
- `POST /api/attempts/{id}/submit-quiz`
- `POST /api/attempts/{id}/submit-project` (multipart)
- `POST /api/attempts/{id}/submit-open-text`
- `GET /api/attempts/{id}/result`
- `GET /api/assessments/{id}/attempts`

### Servicios

- `AssessmentService` — CRUD + límites Premium.
- `AttemptService` — start, submit-quiz (autocorrección + aleatorización por seed del attemptId), expiración por time limit.
- `SubmissionService` — submit project/open_text + grading manual + storage MinIO (50 MB max, no cuenta contra storage del curso).
- `LessonProgressService.markCompleted()` — invocado tras aprobar.

### Detalle: aleatorización por seed

En quizzes con `shuffleOptions = true`, el orden de las opciones se randomiza pero **determinísticamente por attempt** (seed = `attemptId`), de modo que si el alumno recarga la página durante el intento, las opciones siguen en el mismo orden.

---

## Frontend

### Componentes

- **Editor (instructor)**: `components/AssessmentEditor.vue` — usado dentro de `CourseWizard` para configurar evaluaciones. **Migrado a `t()`** (i18n).
- **Vista del alumno**: `components/AssessmentLessonView.vue` — intro + delegación según tipo.
- **Quiz runner**: `components/QuizRunner.vue` — preguntas, timer, panel de navegación, marcar para revisar, dialog de envío.
- **Project submit**: `components/ProjectSubmitView.vue` — usa `FileUploader.vue`.
- **Open text submit**: `components/OpenTextSubmitView.vue` — textarea con contador.
- **Resultados**: `components/AssessmentResultsView.vue` — corregido por pregunta, explicaciones, gradient pass/fail.

### API client

- `api/assessment.ts` — todo lo de evaluaciones, attempts y submissions.

---

## Limitaciones conocidas

- **No hay tipos mixtos**: una evaluación es solo de un tipo. Si quieres mezclar quiz + open_text, necesitas dos bloques separados.
- **No hay regrading masivo** — para corregir notas en bulk hay que ir entrega por entrega.
- **No hay vista del histórico de feedback** — el alumno ve solo el feedback del último intento calificado en cada attempt.
- **Time limit no se enforça server-side** rigurosamente — si el cliente pierde conexión justo al expirar, podría haber discrepancia. La protección backend es: el endpoint `submit-quiz` rechaza si `started_at + timeLimit < now()` con `status = expired`.

---

## Referencias en código

- Backend: `service/AssessmentService.java`, `service/AttemptService.java`, `service/SubmissionService.java`
- Backend controllers: `AssessmentController.java`, `AttemptController.java`, `SubmissionController.java`
- Backend entidades: `AssessmentEntity`, `QuizQuestionEntity`, `QuizOptionEntity`, `AssessmentAttemptEntity`, `QuizAnswerEntity`, `SubmissionEntity`
- Frontend: `components/AssessmentEditor.vue`, `components/AssessmentLessonView.vue`, `components/QuizRunner.vue`, `components/ProjectSubmitView.vue`, `components/OpenTextSubmitView.vue`, `components/AssessmentResultsView.vue`
- Frontend API: `api/assessment.ts`
