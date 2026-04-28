# Referencia de la API REST

> Endpoints expuestos por el backend. Todos bajo `http://localhost:8080` en desarrollo.
>
> **Autenticación**: salvo los marcados como públicos, hay que enviar `Authorization: Bearer <accessToken>`.

---

## Autenticación

| Método | Path | Auth | Descripción |
|---|---|---|---|
| `POST` | `/api/auth/register` | público | Crear cuenta. Devuelve sesión + envía email de verificación |
| `POST` | `/api/auth/login` | público | Login con email + password |
| `POST` | `/api/auth/refresh` | público | Renovar access token con refresh token |
| `POST` | `/api/auth/logout` | autenticado | Invalida refresh token en BD |
| `POST` | `/api/auth/verify-email` | público | Verificar email con token |
| `POST` | `/api/auth/resend-verification` | autenticado | Reenviar email de verificación |

Ver [`05-features/auth.md`](../05-features/auth.md).

---

## Usuarios

| Método | Path | Auth | Descripción |
|---|---|---|---|
| `GET` | `/api/users/me` | autenticado | Datos del usuario actual |
| `PATCH` | `/api/users/me` | autenticado | Actualizar perfil propio |
| `GET` | `/api/users/{id}` | autenticado | Datos de otro usuario (limitado por privacy) |

---

## Cursos

| Método | Path | Auth | Descripción |
|---|---|---|---|
| `GET` | `/api/courses/all` | público | Lista de cursos publicados |
| `GET` | `/api/courses/search?keyword=&category=&level=&isFree=` | público | Búsqueda con filtros (paginada) |
| `GET` | `/api/courses/by-slug/{slug}` | público | Curso por slug (vista pública) |
| `GET` | `/api/courses/{id}/edit` | instructor/owner | Datos completos para editar |
| `POST` | `/api/courses` | autenticado (no admin necesario) | Crear curso (en `draft`) |
| `PATCH` | `/api/courses/{id}` | owner/instructor | Actualizar curso |
| `DELETE` | `/api/courses/{id}` | owner/admin | Eliminar curso |
| `POST` | `/api/courses/{id}/submit-for-review` | owner | Marcar como `pending_review` |
| `POST` | `/api/courses/{id}/approve` | admin | Publicar curso |
| `POST` | `/api/courses/{id}/reject` | admin | Rechazar con motivo |
| `GET` | `/api/courses/pending-review` | admin | Cola de admin |
| `GET` | `/api/courses/instructor/{userId}` | autenticado | Cursos creados por un user |
| `POST` | `/api/courses/{id}/thumbnail` | owner/instructor | Subir thumbnail (multipart) |

---

## Categorías

| Método | Path | Auth | Descripción |
|---|---|---|---|
| `GET` | `/api/categories` | público | Lista catálogo |

---

## Inscripciones

| Método | Path | Auth | Descripción |
|---|---|---|---|
| `POST` | `/api/enrollments` | autenticado | Inscribirse en curso (valida prerequisitos + acceso) |
| `GET` | `/api/enrollments/me` | autenticado | Mis inscripciones |
| `GET` | `/api/enrollments/check?courseId=` | autenticado | ¿Estoy inscrito? |
| `DELETE` | `/api/enrollments/{courseId}` | autenticado | Cancelar inscripción |

---

## Secciones y lecciones

| Método | Path | Auth | Descripción |
|---|---|---|---|
| `GET` | `/api/courses/{courseId}/sections` | autenticado | Secciones con lecciones (incluye `blocks[]` desde V11) |
| `POST` | `/api/courses/{courseId}/sections` | owner/instructor | Crear sección |
| `PATCH` | `/api/sections/{id}` | owner/instructor | Actualizar sección |
| `DELETE` | `/api/sections/{id}` | owner/instructor | Eliminar |
| `PATCH` | `/api/sections/reorder` | owner/instructor | Reordenar (body: lista de `{id, position}`) |
| `GET` | `/api/lessons/{id}` | canAccessLesson | Lección con bloques |
| `POST` | `/api/sections/{sectionId}/lessons` | owner/instructor | Crear lección |
| `PATCH` | `/api/lessons/{id}` | owner/instructor | Actualizar |
| `DELETE` | `/api/lessons/{id}` | owner/instructor | Eliminar |
| `PATCH` | `/api/lessons/reorder` | owner/instructor | Reordenar |
| `POST` | `/api/lessons/{id}/content` | owner/instructor | Subir vídeo/PDF (legacy, se mantiene) |

---

## Bloques (V11)

| Método | Path | Auth | Descripción |
|---|---|---|---|
| `GET` | `/api/lessons/{lessonId}/blocks` | canAccessLesson | Listar bloques en orden |
| `POST` | `/api/lessons/{lessonId}/blocks` | canEditLesson | Crear bloque |
| `PUT` | `/api/blocks/{id}` | canEditBlock | Actualizar bloque |
| `DELETE` | `/api/blocks/{id}` | canEditBlock | Eliminar (cascada al assessment si lo tiene) |
| `PATCH` | `/api/lessons/{lessonId}/blocks/reorder` | canEditLesson | Reordenar (body: `{orderedIds: [uuid, …]}`) |

Ver [`05-features/lesson-blocks.md`](../05-features/lesson-blocks.md).

---

## Recursos descargables (lessons)

| Método | Path | Auth | Descripción |
|---|---|---|---|
| `GET` | `/api/lessons/{id}/resources` | canAccessLesson | Listar recursos |
| `POST` | `/api/lessons/{id}/resources` | canEditLesson | Subir archivo (multipart) |
| `GET` | `/api/resources/{id}/download` | canAccessResource | Devuelve `{url}` con presigned URL |
| `DELETE` | `/api/resources/{id}` | canEditResource | Eliminar |

Ver [`storage.md`](storage.md).

---

## Evaluaciones (V11 — base-block)

| Método | Path | Auth | Descripción |
|---|---|---|---|
| `GET` | `/api/blocks/{blockId}/assessment` | canAccessBlock | Detalle público (sin respuestas correctas) |
| `GET` | `/api/blocks/{blockId}/assessment/edit` | canEditBlock | Detalle con respuestas correctas |
| `POST` | `/api/blocks/{blockId}/assessment` | canEditBlock | Crear evaluación para el bloque |
| `PUT` | `/api/assessments/{id}` | canEditAssessment | Actualizar configuración |
| `DELETE` | `/api/assessments/{id}` | canEditAssessment | Eliminar (mantiene el bloque) |
| `POST` | `/api/assessments/{id}/questions` | canEditAssessment | Añadir pregunta de quiz |
| `PUT` | `/api/questions/{id}` | (verifica al servicio) | Actualizar pregunta |
| `DELETE` | `/api/questions/{id}` | (verifica al servicio) | Eliminar pregunta |

Ver [`05-features/assessments.md`](../05-features/assessments.md).

---

## Intentos y entregas

| Método | Path | Auth | Descripción |
|---|---|---|---|
| `POST` | `/api/assessments/{id}/start` | canAccessAssessment | Iniciar intento (devuelve `attemptId` + assessment) |
| `POST` | `/api/attempts/{id}/submit-quiz` | canAccessAttempt | Enviar respuestas de quiz (autocorrección) |
| `POST` | `/api/attempts/{id}/submit-project` | canAccessAttempt | Subir archivo de proyecto |
| `POST` | `/api/attempts/{id}/submit-open-text` | canAccessAttempt | Enviar respuesta de texto |
| `GET` | `/api/attempts/{id}/result` | canAccessAttempt | Resultado del intento |
| `GET` | `/api/attempts/{id}/submission` | canAccessAttempt | Submission asociada |
| `GET` | `/api/assessments/{id}/attempts` | canAccessAssessment | Mis intentos en una evaluación |
| `GET` | `/api/submissions/{id}/download` | canGradeSubmission | Presigned URL del archivo |
| `PATCH` | `/api/submissions/{id}/grade` | canGradeSubmission | Calificar (body: `{score, feedback}`) |

---

## Calificación (cola del instructor)

| Método | Path | Auth | Descripción |
|---|---|---|---|
| `GET` | `/api/grading/pending` | autenticado | Entregas pendientes en cursos donde el caller es owner/instructor (admin → todas) |

Ver [`05-features/grading.md`](../05-features/grading.md).

---

## Progreso del estudiante

| Método | Path | Auth | Descripción |
|---|---|---|---|
| `GET` | `/api/courses/{id}/progress` | autenticado | Progreso global en el curso |
| `GET` | `/api/lessons/{id}/progress` | autenticado | Progreso de una lección |
| `POST` | `/api/lessons/{id}/toggle-complete` | autenticado | Marcar/desmarcar completada |
| `PATCH` | `/api/lessons/{id}/position` | autenticado | Guardar posición de vídeo |

Ver [`05-features/progress.md`](../05-features/progress.md).

---

## Prerequisitos

| Método | Path | Auth | Descripción |
|---|---|---|---|
| `GET` | `/api/courses/{id}/prerequisites` | público | Lista de cursos requeridos |
| `PUT` | `/api/courses/{id}/prerequisites` | owner/instructor + premium | Sincronizar prerequisitos |
| `GET` | `/api/courses/{id}/prerequisites/check` | autenticado | ¿Cumplo los prereqs? |

Ver [`05-features/prerequisites.md`](../05-features/prerequisites.md).

---

## Reviews

| Método | Path | Auth | Descripción |
|---|---|---|---|
| `GET` | `/api/courses/{id}/reviews` | público | Lista de valoraciones |
| `POST` | `/api/courses/{id}/reviews` | autenticado (inscrito) | Crear/actualizar mi valoración |
| `DELETE` | `/api/reviews/{id}` | autor o admin | Eliminar |

Ver [`05-features/reviews.md`](../05-features/reviews.md).

---

## Pagos / Premium (Stripe)

| Método | Path | Auth | Descripción |
|---|---|---|---|
| `POST` | `/api/payments/create-checkout-session` | autenticado | Crea sesión Stripe Checkout (mensual o anual) |
| `POST` | `/api/payments/create-portal-session` | autenticado (premium) | Acceso al portal de Stripe para gestionar suscripción |
| `POST` | `/api/payments/webhook` | público (firma Stripe) | Webhook de eventos de Stripe |
| `GET` | `/api/payments/subscription/status` | autenticado | `{isPremium, currentPeriodEnd, status}` |

Ver [`05-features/premium-stripe.md`](../05-features/premium-stripe.md).

---

## Admin

| Método | Path | Auth | Descripción |
|---|---|---|---|
| `GET` | `/api/admin/users` | admin | Listado de usuarios (paginado, búsqueda) |
| `POST` | `/api/admin/users/{id}/grant-premium` | admin | Conceder Premium manual |
| `POST` | `/api/admin/users/{id}/revoke-premium` | admin | Revocar Premium |
| `POST` | `/api/admin/users/{id}/deactivate` | admin | Desactivar cuenta |

---

## Convenciones

- **Códigos de estado**:
  - `200 OK` para GET / PATCH / PUT exitosos.
  - `201 Created` para POST que crean recurso.
  - `204 No Content` para DELETE exitoso.
  - `400 Bad Request` para validaciones de negocio (`BadRequestException`).
  - `401 Unauthorized` para falta de auth.
  - `403 Forbidden` para permisos insuficientes (`@PreAuthorize` falla).
  - `404 Not Found` para recurso inexistente (`ResourceNotFoundException`).
- **Errores**: cuerpo JSON `{ "message": "...", "status": 400 }` (vía `GlobalExceptionHandler`).
- **Paginación**: cuando aplica, devolver `Page<T>` de Spring Data → JSON con `{content, totalPages, totalElements, …}`.
- **Filtros**: query params `?key=value`. Si son muchos, considerar POST con DTO de filtro.

---

## Swagger / OpenAPI

`Springdoc-openapi-starter-webmvc-ui:2.x` está en `pom.xml`. Si está activo, Swagger UI vive en:

```
http://localhost:8080/swagger-ui.html
```

Y la spec OpenAPI en:

```
http://localhost:8080/v3/api-docs
```

Si no responde, verificar que la dependencia está activa (no es `optional` ni `provided`) y que el puerto coincide.
