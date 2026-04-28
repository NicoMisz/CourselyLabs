# Arquitectura

> Vista de alto nivel de cómo está organizado el sistema. Para detalles de cada feature mira [`05-features/`](../05-features/).

---

## Diagrama de componentes

```
┌──────────────────────────────────────────────────────────────────────┐
│                            Cliente (browser)                          │
│  Vue 3 + Quasar + Pinia + vue-router + axios + vue-i18n              │
└────────────────────────────┬─────────────────────────────────────────┘
                             │ HTTP/JSON  ·  JWT Bearer
                             ▼
┌──────────────────────────────────────────────────────────────────────┐
│                         Backend (Spring Boot 4)                       │
│  ┌────────────────┐  ┌──────────────────┐  ┌──────────────────────┐ │
│  │  Controllers   │→ │  Services        │→ │  Repositories (JPA)  │ │
│  │  (REST + DTOs) │  │  (lógica negocio)│  │                      │ │
│  └────────────────┘  └──────────────────┘  └──────────────────────┘ │
│         ▲                     │                       │             │
│         │ @PreAuthorize        │                       │             │
│         │                      ▼                       ▼             │
│  ┌─────────────────┐    ┌────────────┐     ┌──────────────────┐     │
│  │ Spring Security │    │  Stripe    │     │  PostgreSQL 16   │     │
│  │ JWT + filtros   │    │  (webhook) │     │  Flyway V1..V11  │     │
│  └─────────────────┘    └────────────┘     └──────────────────┘     │
│                                                                      │
│                          ┌────────────┐                              │
│                          │  MinIO     │  (S3-compatible para uploads)│
│                          └────────────┘                              │
│                                                                      │
│                          ┌────────────┐                              │
│                          │  MailHog   │  (SMTP de desarrollo)        │
│                          └────────────┘                              │
└──────────────────────────────────────────────────────────────────────┘
```

---

## Capas backend

```
controller/        ← REST endpoints, validación de DTOs, @PreAuthorize
    │
    ▼
service/           ← lógica de negocio, transacciones (@Transactional),
    │                reglas de límites (storage, evaluaciones, premium)
    ▼
repository/        ← Spring Data JPA, queries personalizadas con @Query
    │
    ▼
entity/            ← entidades JPA, anotaciones Hibernate
                    
mapper/            ← conversión entity ↔ DTO
dto/               ← objetos de transporte (input + output)
exception/         ← BadRequestException, ResourceNotFoundException, etc. + handler global
security/          ← filtros JWT, CourseSecurityService (helpers de @PreAuthorize)
config/            ← Beans de configuración (Stripe, S3, mail, security)
```

**Convención**: controllers no llaman a repositorios directamente. Siempre pasan por service.

## Capas frontend

```
views/             ← páginas (top-level por ruta)
   ├─ admin/       ← /admin/*
   └─ instructor/  ← /instructor/*

layouts/           ← estructuras envolventes (MainLayout, InstructorLayout, AdminLayout)
components/        ← componentes reutilizables (cards, editors, players, dialogs)
api/               ← clientes axios por dominio (auth.ts, course.ts, lesson.ts...)
stores/            ← Pinia stores (auth.ts es el principal)
types/             ← tipos TypeScript (espejo de los DTOs del backend)
router/            ← routes.ts + index.ts (con guards)
i18n/              ← config + locales/es.json
composables/       ← funciones reactivas reutilizables
css/               ← estilos globales (app.scss)
```

**Convención**: las views llaman a los `api/*.ts`, **no** usan axios directamente.

---

## Flujo de una request típica

Ejemplo: estudiante se inscribe en un curso.

```
1. Frontend: EnrollButton.vue
       └─→ api/enrollment.ts → enroll(courseId)
              └─→ axios.post('/api/enrollments', { courseId },
                              headers: { Authorization: 'Bearer xxx' })

2. Backend: 
   2.1. JwtAuthFilter           → extrae usuario del token, lo pone en SecurityContext
   2.2. EnrollmentController    → @PreAuthorize("isAuthenticated()")
                                  recibe DTO, valida
   2.3. EnrollmentService       → comprueba prerequisitos, comprueba acceso (Premium si aplica),
                                  llama a EnrollmentRepository.save(...)
   2.4. EnrollmentRepository    → INSERT en tabla enrollments
   
3. Backend: respuesta 201 CREATED con EnrollmentDTO
       └─→ Frontend: actualiza store, refresca UI
```

---

## Mecanismo de autorización

El proyecto usa `@PreAuthorize` con bean SpEL en los controllers para chequear permisos. La lógica vive en `CourseSecurityService` (un bean Spring inyectado).

```java
@PutMapping("/blocks/{id}")
@PreAuthorize("@courseSecurityService.canEditBlock(#id, authentication)")
public ResponseEntity<LessonBlockDTO> update(@PathVariable UUID id, @RequestBody LessonBlockDTO dto) {
    return ResponseEntity.ok(blockService.update(id, dto));
}
```

`canEditBlock(blockId, auth)` resuelve:
```
block → lesson → section → course → owner / instructor / admin?
```

Métodos disponibles en `CourseSecurityService`:

| Método | Comprueba |
|---|---|
| `isOwnerOrInstructorOrAdmin(courseId, auth)` | Owner del curso, instructor co-asignado o admin |
| `isOwnerOrAdmin(courseId, auth)` | Owner o admin (no instructor) |
| `canEditSection(sectionId, auth)` | Resuelve curso y delega |
| `canEditLesson(lessonId, auth)` | Resuelve curso y delega |
| `canEditBlock(blockId, auth)` | Resuelve curso y delega |
| `canEditResource(resourceId, auth)` | Resuelve curso y delega |
| `canEditAssessment(assessmentId, auth)` | Resuelve curso y delega |
| `canEditAssessmentByLesson(lessonId, auth)` | Resuelve curso y delega |
| `canAccessLesson(lessonId, auth)` | Lección gratis o user inscrito o instructor/admin |
| `canAccessBlock(blockId, auth)` | Delega en `canAccessLesson` |
| `canAccessAssessment(assessmentId, auth)` | Delega en `canAccessLesson` |
| `canAccessAttempt(attemptId, auth)` | Owner del intento o instructor del curso |
| `canGradeSubmission(submissionId, auth)` | Owner/instructor/admin del curso de la submission |
| `canAccessResource(resourceId, auth)` | Delega en `canAccessLesson` |

Detalles en [`04-backend/security.md`](../04-backend/security.md).

---

## Comunicación con Stripe

```
Usuario (Frontend) → /premium → "Suscribirse"
   └─→ POST /api/payments/create-checkout-session
           └─→ Backend crea sesión Stripe → devuelve URL
                  └─→ Browser redirige a Stripe Checkout

Stripe → webhook → POST /api/payments/webhook (con stripe-signature)
   └─→ PaymentService valida firma → actualiza tabla `subscriptions`
   
Usuario completa pago → Stripe redirige a /pago/exito
   └─→ Frontend recarga sesión y verifica isPremium()
```

Detalles en [`05-features/premium-stripe.md`](../05-features/premium-stripe.md).

---

## Comunicación con MinIO

```
Subir un recurso descargable a una lección:
   1. Frontend: FileUploader.vue → api/resources.ts → POST /api/lessons/{id}/resources (multipart)
   2. Backend: LessonResourceService valida tamaño y tipo MIME
              → genera storageKey: "resources/{courseId}/{lessonId}/{filename-sanitized}"
              → FileStorageService.upload(...)  // usa S3 SDK contra MinIO
              → Persiste LessonResourceEntity con la key

Descargar (signed URL):
   1. Frontend: GET /api/resources/{id}/download
   2. Backend: FileStorageService.getPresignedUrl(key) // expira en N minutos
   3. Frontend: window.open(url, '_blank')
```

Detalles en [`04-backend/storage.md`](../04-backend/storage.md).

---

## Decisiones arquitectónicas vivas

Para no repetir, ver [`state.md` §7](state.md#7-decisiones-arquitectónicas-vivas).
