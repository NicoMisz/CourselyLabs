# Backend (Spring Boot)

> Layout del módulo backend. Para temas concretos, ver:
> - [api.md](api.md) — Endpoints REST.
> - [security.md](security.md) — JWT, roles, `@PreAuthorize`.
> - [migrations.md](migrations.md) — Flyway.
> - [storage.md](storage.md) — MinIO / S3.
> - [../05-features/](../05-features/) — features completas con su backend.

---

## Stack

- **Spring Boot 4.1.0-M1**
- **Java 21**, Maven Wrapper (`./mvnw`)
- **PostgreSQL 16** + **Flyway**
- **Spring Security** + **jjwt 0.12.6**
- **Spring Data JPA** (Hibernate)
- **Lombok** (`@Data`, `@AllArgsConstructor`, `@NoArgsConstructor`)
- **Stripe Java SDK 26.3**
- **AWS S3 SDK** apuntando a MinIO en local (`storage.endpoint`)
- **Spring Boot Starter Mail** (SMTP, MailHog en local)
- **Springdoc** para OpenAPI / Swagger UI (si se activa)

---

## Layout de paquetes

```
com.courselylabs.courselylab/
├── CourselyLabApplication.java           # main()
│
├── controller/                           # 18 controllers REST
│   ├── AdminController.java
│   ├── AssessmentController.java         # /api/blocks/{id}/assessment + /api/assessments/...
│   ├── AttemptController.java
│   ├── AuthController.java               # /api/auth/...
│   ├── CategoriaController.java
│   ├── CourseController.java
│   ├── CoursePrerequisiteController.java
│   ├── EnrollmentController.java
│   ├── GradingController.java            # /api/grading/pending
│   ├── LessonBlockController.java        # /api/lessons/{id}/blocks + /api/blocks/{id}
│   ├── LessonController.java
│   ├── LessonProgressController.java
│   ├── LessonResourceController.java
│   ├── PaymentController.java            # /api/payments/...
│   ├── ReviewController.java
│   ├── SectionController.java
│   ├── SubmissionController.java
│   └── UserController.java
│
├── service/                              # Lógica de negocio
│   ├── AssessmentService.java
│   ├── AttemptService.java
│   ├── AuthService.java
│   ├── CourseService.java
│   ├── CoursePrerequisiteService.java
│   ├── EmailService.java
│   ├── EnrollmentService.java
│   ├── FileStorageService.java           # wrapper de S3 SDK contra MinIO
│   ├── GradingService.java
│   ├── LessonBlockService.java
│   ├── LessonProgressService.java
│   ├── LessonResourceService.java
│   ├── LessonService.java
│   ├── ReviewService.java
│   ├── SectionService.java
│   ├── StripeService.java
│   ├── SubmissionService.java
│   ├── SubscriptionService.java
│   └── UserService.java
│
├── entity/                               # 22 entidades JPA
│   ├── AssessmentAttemptEntity.java
│   ├── AssessmentEntity.java
│   ├── CategoriaEntity.java
│   ├── CourseEntity.java
│   ├── CourseInstructorEntity.java
│   ├── CoursePrerequisiteEntity.java
│   ├── EnrollmentEntity.java
│   ├── LessonBlockEntity.java            ← V11
│   ├── LessonEntity.java
│   ├── LessonProgressEntity.java
│   ├── LessonResourceEntity.java
│   ├── PaymentEntity.java
│   ├── QuizAnswerEntity.java
│   ├── QuizOptionEntity.java
│   ├── QuizQuestionEntity.java
│   ├── RefreshTokenEntity.java
│   ├── ReviewEntity.java
│   ├── SectionEntity.java
│   ├── SubmissionEntity.java
│   ├── SubscriptionEntity.java
│   ├── UserEntity.java
│   └── VerificationTokenEntity.java
│
├── repository/                           # Spring Data JPA
│   └── *Repository.java                  # uno por entidad
│
├── dto/                                  # Objects de I/O
│   └── *DTO.java
│
├── mapper/                               # Entity ↔ DTO
│   ├── LessonMapper.java
│   ├── SectionMapper.java
│   └── ...
│
├── exception/                            # Excepciones tipadas
│   ├── BadRequestException.java          # 400
│   ├── ResourceNotFoundException.java    # 404
│   ├── UnauthorizedException.java        # 401/403
│   └── GlobalExceptionHandler.java       # @RestControllerAdvice
│
├── security/                             # JWT + autorización
│   ├── JwtAuthenticationFilter.java
│   ├── JwtTokenProvider.java
│   ├── SecurityConfig.java
│   ├── CourseSecurityService.java        # @PreAuthorize helpers
│   └── ...
│
└── config/                               # Beans
    ├── StripeConfig.java
    ├── StorageConfig.java                # S3 bean → MinIO
    ├── MailConfig.java
    ├── CorsConfig.java
    └── ...
```

---

## Comandos

```bash
cd CourselyLabs-back

./mvnw spring-boot:run         # arrancar (puerto 8080)
./mvnw clean compile           # solo compilar
./mvnw clean package           # construir el .jar
./mvnw test                    # tests (limitados de momento)

# Variables: ver ../02-getting-started/env-vars.md
```

---

## Convenciones

### Anatomía de una request

```
HTTP request
    │
    ▼
JwtAuthenticationFilter   ← extrae Bearer token, popula SecurityContext
    │
    ▼
@RestController            ← @PreAuthorize valida acceso
    │
    ▼
@Service @Transactional    ← lógica + transacción
    │
    ▼
JpaRepository              ← queries
    │
    ▼
PostgreSQL
```

### Reglas

- Los **controllers no tocan repositorios**. Siempre pasan por service.
- Los **services no devuelven entidades** a controllers. Devuelven DTOs (vía mapper).
- Los **DTOs son la única superficie de API**. Las entities son internas.
- Las **transacciones** se delimitan en el service con `@Transactional`. Lectura: `@Transactional(readOnly = true)`. Escritura: `@Transactional`.
- Los **errores de negocio** lanzan `BadRequestException` o `ResourceNotFoundException`; nunca `RuntimeException` genérico.
- El **handler global** (`GlobalExceptionHandler`) los traduce a JSON con `{message, status}`.
- Los **campos de auditoría** (`createdAt`, `updatedAt`) usan `@CreationTimestamp` / `@UpdateTimestamp` de Hibernate.
- Las **PKs UUID** usan `@GeneratedValue(strategy = GenerationType.UUID)`.
- Los **lazy fetchs** son la regla (`@ManyToOne(fetch = FetchType.LAZY)`). Eager solo cuando se justifica.

### Naming

- Clases: `XxxController`, `XxxService`, `XxxRepository`, `XxxEntity`, `XxxDTO`, `XxxMapper`.
- Métodos service: verbos imperativos (`createForBlock`, `findByLessonId`, `markCompleted`).
- Endpoints: `kebab-case` cuando hay separadores; recursos en plural (`/courses`, `/lessons`, `/blocks`).

---

## Cómo añadir una feature backend

1. **Migración Flyway** si toca schema → ver [migrations.md](migrations.md).
2. **Entidad** en `entity/` con anotaciones JPA.
3. **Repository** en `repository/` extendiendo `JpaRepository<Entity, UUID>`.
4. **DTO** de input y/o output en `dto/`.
5. **Mapper** en `mapper/` si la conversión es compleja (si no, en el propio service).
6. **Service** con lógica + `@Transactional`.
7. **Controller** con endpoints REST y `@PreAuthorize`.
8. **Security helper** en `CourseSecurityService` si hace falta una nueva regla de acceso.
9. **Tests** (Spring Boot Test + Testcontainers para integración).
10. **Documentar** en `05-features/xxx.md` y actualizar `01-overview/state.md`.

---

## Tests

Limitados de momento. Estructura recomendada:

```
src/test/java/com/courselylabs/courselylab/
├── service/      # @SpringBootTest @AutoConfigureMockMvc + Testcontainers
└── controller/   # @WebMvcTest con @MockBean del service
```

`Testcontainers` está disponible (versión `8.5.17`) pero subutilizado. Pendiente cubrir más servicios y controllers.
