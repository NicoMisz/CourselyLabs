# Refactor multi-bloque de lecciones (V11)

> Cambio estructural en la rama `feature/assessments` que convierte cada lección en un contenedor de **bloques** ordenados.
>
> Migración: **`V11__lesson_blocks.sql`**.

---

## Por qué se hizo

Antes, una lección tenía un único `type` (`text` / `video` / `pdf` / `quiz` / `project` / `open_text`) y todo el contenido se renderizaba en función de ese tipo. Limitaciones:

1. Una lección no podía mezclar formatos. No se podía explicar primero con texto, luego un PDF de apoyo y al final un cuestionario.
2. **Una sola evaluación por lección**: para tener dos quizzes había que crear dos lecciones separadas.
3. **Mala UX**: el editor obligaba a elegir un tipo primero y "encajar" el contenido a ese tipo.
4. La calificación de entregas estaba mezclada en el editor de la lección, distrayendo al instructor mientras autoraba.

El refactor desacopla "qué es la lección" (un contenedor) de "qué contenido tiene" (una lista ordenada de bloques heterogéneos).

---

## Modelo nuevo

```
Course
 └─ Section
     └─ Lesson  (contenedor; type queda como legacy nullable)
         └─ LessonBlock[]   ← ordenados por position
             ├─ type: 'text'    → textContent
             ├─ type: 'video'   → videoUrl
             ├─ type: 'pdf'     → pdfUrl
             ├─ type: 'quiz'    → 1:1 con AssessmentEntity (block_id)
             ├─ type: 'project' → 1:1 con AssessmentEntity
             └─ type: 'open_text' → 1:1 con AssessmentEntity
```

### Tabla `lesson_blocks`

```sql
CREATE TABLE lesson_blocks (
    id UUID PRIMARY KEY,
    lesson_id UUID NOT NULL REFERENCES lessons(id) ON DELETE CASCADE,
    type VARCHAR(20) NOT NULL,
    position INT NOT NULL DEFAULT 0,
    text_content TEXT,
    video_url VARCHAR(500),
    pdf_url VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);
```

### Cambios en `assessments`

- `block_id UUID UNIQUE` (FK a `lesson_blocks`) — relación **1:1** con bloque.
- `lesson_id` queda **nullable** y se mantiene por compatibilidad (se rellena al crear desde el bloque, derivado de `block.lesson`).
- Se elimina la unicidad anterior `assessments.lesson_id UNIQUE` — ahora una lección puede tener N evaluaciones (una por bloque assessment-type).

### Cambios en `lessons`

- `type` ahora es `nullable` y sin CHECK constraint. Es campo legacy: se sigue rellenando al migrar pero **no se usa para renderizar**. Pendiente de eliminar en una migración futura cuando todos los datos vivos hayan dejado de depender de él.

### Migración de datos (incluida en V11)

Cada lección existente se convierte en **1 bloque** del tipo previo, preservando contenido:

```sql
INSERT INTO lesson_blocks (id, lesson_id, type, position, text_content, video_url, pdf_url)
SELECT
    gen_random_uuid(),
    l.id,
    l.type,
    0,
    CASE WHEN l.type = 'text' THEN l.content_text END,
    CASE WHEN l.type = 'video' THEN l.content_url END,
    CASE WHEN l.type = 'pdf' THEN l.content_url END
FROM lessons l
WHERE l.type IN ('text', 'video', 'pdf', 'quiz', 'project', 'open_text');
```

Las evaluaciones existentes se vinculan al bloque recién creado:

```sql
UPDATE assessments a
SET block_id = (SELECT id FROM lesson_blocks WHERE lesson_id = a.lesson_id LIMIT 1)
WHERE a.lesson_id IS NOT NULL;
```

> Resultado en BD vivo al aplicar V11: 26 filas insertadas en `lesson_blocks`, evaluaciones existentes vinculadas correctamente.

---

## Backend — endpoints nuevos

### Bloques (`/api/lessons/{lessonId}/blocks` y `/api/blocks/{blockId}`)

| Método | Ruta | Auth | Descripción |
|---|---|---|---|
| `GET` | `/api/lessons/{lessonId}/blocks` | `canAccessLesson` | Listar bloques en orden |
| `POST` | `/api/lessons/{lessonId}/blocks` | `canEditLesson` | Crear bloque (body: `{type, position?, textContent?, videoUrl?, pdfUrl?}`) |
| `PUT` | `/api/blocks/{id}` | `canEditBlock` | Actualizar contenido o posición |
| `DELETE` | `/api/blocks/{id}` | `canEditBlock` | Eliminar (cascada al `assessment` si lo tiene) |
| `PATCH` | `/api/lessons/{lessonId}/blocks/reorder` | `canEditLesson` | Reordenar (body: `{orderedIds: [uuid, …]}`) |

### Evaluaciones (migradas a base-block, **se rompió la API previa basada en lección**)

| Antes | Ahora |
|---|---|
| `GET /api/lessons/{id}/assessment` | `GET /api/blocks/{blockId}/assessment` |
| `GET /api/lessons/{id}/assessment/edit` | `GET /api/blocks/{blockId}/assessment/edit` |
| `POST /api/lessons/{id}/assessment` | `POST /api/blocks/{blockId}/assessment` |

`PUT /api/assessments/{id}` y `DELETE /api/assessments/{id}` se mantienen igual.

### Calificación (nuevo endpoint global)

| Método | Ruta | Auth | Descripción |
|---|---|---|---|
| `GET` | `/api/grading/pending` | autenticado | Lista entregas pendientes en cursos donde el caller es owner/instructor (admin → todas). Ver [`grading.md`](grading.md). |

---

## Backend — clases tocadas

### Nuevas
- `entity/LessonBlockEntity.java`
- `repository/LessonBlockRepository.java`
- `service/LessonBlockService.java` — CRUD + reorder + helper `isAssessmentType(type)`
- `controller/LessonBlockController.java`
- `dto/LessonBlockDTO.java` — incluye `assessmentId` derivado para los bloques assessment-type
- `service/GradingService.java` + `controller/GradingController.java` + `dto/PendingSubmissionDTO.java`

### Modificadas
- `entity/AssessmentEntity.java` — `lesson` pasa de `@OneToOne` a `@ManyToOne` (nullable) y se añade `block` como `@OneToOne` con `unique`.
- `repository/AssessmentRepository.java` — añade `findByBlockId(UUID)` y `findAllByLessonId(UUID)`.
- `service/AssessmentService.java` — el método `createForLesson` se reemplaza por `createForBlock(blockId, dto, email)`. La validación "tipo del bloque == tipo de la evaluación" desaparece (se deriva del bloque).
- `controller/AssessmentController.java` — endpoints reescritos (ver tabla arriba).
- `mapper/LessonMapper.java` — `LessonDTO` ahora incluye `blocks[]`. El mapper inyecta `LessonBlockService` para hidratarlos.
- `dto/LessonDTO.java` — añade `private List<LessonBlockDTO> blocks;`. `type` deja de ser `@NotBlank`.
- `dto/AssessmentDTO.java` — añade `private UUID blockId;`.
- `security/CourseSecurityService.java` — añade `canEditBlock` y `canAccessBlock`.
- `repository/SubmissionRepository.java` — añade `findPendingForGrader(userId)` y `findAllPending()`.

---

## Frontend — cambios

### Tipos (`types/lesson.ts`)
- Añadidos `BlockType`, `LessonBlock`.
- `Lesson.type` pasa a opcional. `Lesson` incluye `blocks?: LessonBlock[]`.

### API clients
- **Nuevo**: `api/lessonBlock.ts` — `listBlocks`, `createBlock`, `updateBlock`, `deleteBlock`, `reorderBlocks`, `isAssessmentBlockType`.
- **Modificado**: `api/assessment.ts` — endpoints reescritos a base-block (`getAssessmentByBlock`, `getAssessmentForEditByBlock`, `createAssessmentForBlock`).

### Componentes
- **`AssessmentEditor.vue`** — recibe `blockId` (no `lessonId`); emite `created`. Botón nuevo "Eliminar evaluación" (no borra el bloque). La sección "Entregas pendientes" se eliminó (vive ahora en `/instructor/calificar`).
- **`AssessmentLessonView.vue`** — recibe `blockId`. Carga `getAssessmentByBlock`.
- **`CourseWizard.vue` (panel de lección)** — rediseñado: lista drag-and-drop (`vuedraggable`) de bloques con botón `+ Añadir bloque` agrupado en dos secciones (`Contenido` / `Evaluaciones`).
- **`LessonView.vue`** — itera `lesson.blocks` en orden. Mantiene fallback legacy para datos sin bloques.

### Rutas y layouts
- `InstructorLayout.vue` — añadido item "Calificar".
- `routes.ts` — `/instructor/calificar` → `GradingDashboard.vue`.

### Deep-link
Desde `GradingDashboard`, cada entrega tiene un botón que navega a `/instructor/cursos/{courseId}/editar?lesson={lessonId}&block={blockId}`. `CourseWizard` lee el query, selecciona la lección y hace scroll al bloque con animación CSS (`.block-highlight`, 2.2 s).

---

## Compatibilidad y rutas migratorias

### Datos viejos siguen funcionando
- Las lecciones existentes (pre-V11) tienen ahora 1 bloque del tipo previo.
- Las evaluaciones existentes apuntan al bloque migrado.
- `lesson.type` y `lesson.contentText` / `lesson.contentUrl` siguen rellenos por seguridad pero no se leen para renderizar — `LessonView` itera `blocks[]` y el fallback solo aplica si `blocks.length === 0` (no debería pasar tras V11).

### Rotura intencionada
La API basada en `lessonId` para evaluaciones (`/api/lessons/{id}/assessment`) **se eliminó**. Cualquier cliente externo apuntando a esa ruta dejará de funcionar. Como solo hay un cliente (este front), no es problema.

### Trabajo futuro recomendado
- Eliminar `lesson.type`, `lesson.content_text`, `lesson.content_url` en una `V12` cuando se confirme que ningún query lee esos campos.
- Refactorizar `CourseWizard.vue` (~1300 líneas tras este cambio) en componentes más pequeños (`LessonBlockList.vue`, `BlockEditor.vue`, etc.).

---

## Testing manual sugerido

1. **Migración**: aplicar V11 a una BD con datos pre-existentes y verificar que cada lección tiene exactamente 1 bloque del tipo previo.
2. **Editor**: abrir una lección existente → debería verse 1 bloque con el contenido migrado. Añadir un bloque PDF, reordenar, eliminar.
3. **Lección con varios bloques de evaluación**: crear lección → añadir bloque texto + bloque quiz + bloque project. Configurar cada uno. El estudiante debería ver los tres en orden.
4. **Calificación**: como estudiante, enviar un proyecto. Como instructor, navegar a `/instructor/calificar`, abrir la entrega, calificar, y verificar que `goToBlock` lleva al bloque con animación de highlight.
5. **Borrar evaluación sin perder bloque**: en el AssessmentEditor, pulsar "Eliminar evaluación" → el bloque debe seguir existiendo y permitir reconfigurar la evaluación.

---

## Referencias en código

- Migración: `CourselyLabs-back/src/main/resources/db/migration/V11__lesson_blocks.sql`
- Schema base actualizado: `init_db/01_schema.sql`
- Entidad: `CourselyLabs-back/src/main/java/com/courselylabs/courselylab/entity/LessonBlockEntity.java`
- Service: `CourselyLabs-back/src/main/java/com/courselylabs/courselylab/service/LessonBlockService.java`
- Editor frontend: `CourselyLabs-front/src/views/instructor/CourseWizard.vue` (sección "Bloques")
- Reproducción frontend: `CourselyLabs-front/src/views/LessonView.vue` (`v-for` en `orderedBlocks`)
