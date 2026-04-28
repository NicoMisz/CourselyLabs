# Valoraciones (reviews)

> Sistema de valoraciones de cursos. Backend completo, frontend mínimo.

---

## Modelo

```
reviews:
    id           UUID PK
    user_id      UUID FK → users
    course_id    UUID FK → courses
    rating       INT (1..5)
    comment      TEXT NULL
    created_at   TIMESTAMP
    updated_at   TIMESTAMP

UNIQUE (user_id, course_id)  -- un alumno una valoración por curso
```

---

## Reglas

- Solo **alumnos inscritos** pueden valorar.
- **Una valoración por (usuario, curso)** — actualizar reemplaza la anterior.
- **Rating 1-5 estrellas obligatorio**, comment opcional.
- **El alumno puede editar su propia valoración** en cualquier momento.
- **El alumno puede eliminar su propia valoración**.
- **Admin puede eliminar cualquier valoración**.
- **El instructor del curso NO puede eliminar valoraciones de sus cursos** — sería conflicto de interés.

---

## Cálculos derivados

### Valoración media del curso

```sql
SELECT AVG(rating)::numeric(3,2)
FROM reviews
WHERE course_id = X
```

Mostrado en cards y detalle del curso.

### Distribución

Histograma de cuántas valoraciones de cada estrella (1-5):

```sql
SELECT rating, COUNT(*)
FROM reviews
WHERE course_id = X
GROUP BY rating
ORDER BY rating DESC
```

Mostrado en `RatingDistribution.vue`.

---

## Backend

### Endpoints

| Método | Path | Auth | Descripción |
|---|---|---|---|
| `GET` | `/api/courses/{courseId}/reviews` | público | Lista de valoraciones (paginada) |
| `GET` | `/api/courses/{courseId}/reviews/me` | autenticado | Mi valoración para este curso (404 si no hay) |
| `POST` | `/api/courses/{courseId}/reviews` | autenticado (inscrito) | Crear o actualizar (upsert) mi valoración |
| `DELETE` | `/api/reviews/{id}` | autor o admin | Eliminar |

### Servicio

- `ReviewService` — CRUD + check de inscripción + cálculo de medias.

---

## Frontend

### Estado actual

- Backend completo y funcional.
- Frontend tiene componentes pero **falta una página dedicada**:
  - `components/StarRating.vue` — selector de 1-5 estrellas.
  - `components/RatingDistribution.vue` — histograma.
  - `components/ReviewForm.vue` — formulario de creación/edición.
  - `components/ReviewItem.vue` — render de una valoración individual.
  - `components/ReviewList.vue` — lista paginada.
  - `components/CourseTabReviews.vue` — pestaña "Valoraciones" en `/cursos/{slug}`.

### API client

- `api/review.ts` — `listReviews`, `getMyReview`, `submitReview`, `deleteReview`.

---

## Limitaciones conocidas (frontend)

- **No hay página dedicada** del estilo `/cursos/{slug}/valoraciones`. Solo está la pestaña en el detalle.
- **No hay UI para reportar valoraciones** ofensivas/spam.
- **No hay moderación** ni filtros de palabras malsonantes.
- **No hay verificación** de "fue alumno hace tiempo" — cualquier inscrito puede valorar al instante (ideal: requerir cierto % de progreso antes de poder valorar).
- **Sin respuesta del instructor** a las valoraciones (sería un nice-to-have).

---

## Referencias en código

- Backend: `service/ReviewService.java`, `controller/ReviewController.java`
- Backend entidad: `ReviewEntity.java`
- Frontend: `api/review.ts`, `components/Review*.vue`, `components/StarRating.vue`, `components/RatingDistribution.vue`
