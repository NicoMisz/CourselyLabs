# Seguridad y autorización

> Cómo se autentican y autorizan las requests. Spring Security stateless con JWT + refresh tokens en BD.

---

## Stack

- **Spring Security 6** (vía Spring Boot 4.1.0-M1).
- **jjwt 0.12.6** para emisión y validación de JWT.
- **bcrypt** para hashing de passwords.
- Bean SpEL en `@PreAuthorize` para autorización fina por recurso.

---

## Flujo de autenticación

```
1. Cliente envía POST /api/auth/login {email, password}

2. AuthService:
   - Busca user por email
   - Valida password con bcrypt
   - Genera accessToken (15 min) firmado con JWT_SECRET
   - Genera refreshToken (7 días) y lo persiste en `refresh_tokens`
   - Devuelve { accessToken, refreshToken, user }

3. Cliente guarda los tokens y user en localStorage

4. Cliente añade `Authorization: Bearer <accessToken>` a cada request

5. JwtAuthenticationFilter:
   - Extrae el token del header
   - Valida firma y expiración
   - Carga el UserDetails (email + roles)
   - Lo pone en SecurityContext
   - Pasa al siguiente filtro
```

### Refresh

Cuando el accessToken expira (15 min):

```
1. axios recibe 401
2. Interceptor hace POST /api/auth/refresh { refreshToken }
3. Backend:
   - Verifica que el refreshToken existe en BD y no ha expirado
   - Genera nuevo accessToken
   - (opcional) rota el refreshToken: emite uno nuevo y revoca el viejo
   - Devuelve { accessToken, refreshToken }
4. axios reintenta la request original con el nuevo token
```

Si el refresh falla, axios llama a `clearSession()` y el usuario va a `/login`.

---

## Configuración

`security/SecurityConfig.java`:

- Endpoints públicos: `/api/auth/**`, `/api/courses/all`, `/api/courses/by-slug/**`, `/api/courses/search`, `/api/categories`, `/api/payments/webhook`, `/swagger-ui/**`.
- Resto: requieren autenticación (`authenticated()`).
- CSRF deshabilitado (stateless con JWT).
- Sesión: `STATELESS`.
- CORS: configurado vía bean `CorsConfigurationSource` que lee `cors.allowed-origins`.

---

## Autorización fina: `CourseSecurityService`

Para autorizar al nivel de recurso (no solo de endpoint), Spring Security permite usar bean SpEL en `@PreAuthorize`. La lógica vive en `CourseSecurityService`, un bean Spring inyectado.

### Ejemplo

```java
@PutMapping("/blocks/{id}")
@PreAuthorize("@courseSecurityService.canEditBlock(#id, authentication)")
public ResponseEntity<LessonBlockDTO> update(
    @PathVariable UUID id,
    @RequestBody LessonBlockDTO dto
) {
    return ResponseEntity.ok(blockService.update(id, dto));
}
```

`#id` es el path variable; `authentication` es el `Authentication` actual de Spring Security.

### Métodos disponibles

#### Identidad

| Método | Resuelve |
|---|---|
| `isOwnerOrInstructor(courseId, auth)` | Owner del curso o instructor co-asignado |
| `isOwnerOrAdmin(courseId, auth)` | Owner o admin |
| `isOwnerOrInstructorOrAdmin(courseId, auth)` | Cualquiera de los tres |

#### Edición (delegan en `isOwnerOrInstructorOrAdmin` resolviendo el curso)

| Método | Recibe |
|---|---|
| `canEditSection(sectionId, auth)` | sección → curso |
| `canEditLesson(lessonId, auth)` | lección → sección → curso |
| `canEditBlock(blockId, auth)` | bloque → lección → sección → curso |
| `canEditResource(resourceId, auth)` | recurso → lección → sección → curso |
| `canEditAssessment(assessmentId, auth)` | assessment → bloque o lección → curso |
| `canEditAssessmentByLesson(lessonId, auth)` | lección → curso (usado en endpoints legacy) |

#### Acceso (lectura)

| Método | Lógica |
|---|---|
| `canAccessLesson(lessonId, auth)` | Si la lección es `isFree=true`, cualquier autenticado. Si no, instructor/owner/admin **o** alumno inscrito. |
| `canAccessBlock(blockId, auth)` | Delega en `canAccessLesson`. |
| `canAccessAssessment(assessmentId, auth)` | Delega en `canAccessLesson`. |
| `canAccessResource(resourceId, auth)` | Delega en `canAccessLesson`. |
| `canAccessAttempt(attemptId, auth)` | Owner del attempt **o** instructor/owner/admin del curso. |
| `canGradeSubmission(submissionId, auth)` | Instructor/owner/admin del curso. |

---

## Roles

| Rol | Acceso | Notas |
|---|---|---|
| `admin` | Todo | Cualquier endpoint sin restricciones (se chequea con `requiresRole: 'admin'` en el frontend y `isAdmin(user)` en el backend) |
| `premium` | Todo lo de `user` + features Premium | Suscripción Stripe activa o concedida manualmente |
| `user` | Acceso básico | Puede crear cursos (instructor de facto) |

> "instructor" no es un rol explícito. Cualquier `user` puede crear cursos; al hacerlo se convierte en owner (`courses.created_by`). Adicionalmente, otros usuarios pueden ser asignados como co-instructores vía `course_instructors` (sin cambio de rol).

---

## Manejo de tokens (jjwt 0.12.x)

`security/JwtTokenProvider.java`:

- **Algoritmo**: HS256 (clave simétrica).
- **Claims**: `sub` (email), `iat`, `exp`, `roles` (no se usan en autorización fina, pero útiles para debug).
- **Validación**: lanza excepción si está expirado, manipulado o mal firmado. El filtro la captura y devuelve 401.

### Configuración

```properties
jwt.secret=<256+ bits hex>
jwt.access-expiration=900000      # 15 min
jwt.refresh-expiration=604800000  # 7 días
```

> ⚠️ **En producción**, `JWT_SECRET` debe generarse aleatoriamente y guardarse en un secret manager. La clave por defecto en `application.properties` es solo de desarrollo.

---

## Refresh tokens

Almacenados en tabla `refresh_tokens`:

| Columna | Tipo |
|---|---|
| `id` | UUID PK |
| `user_id` | UUID FK |
| `token` | VARCHAR (el JWT) |
| `expires_at` | TIMESTAMP |
| `revoked` | BOOLEAN |

**Logout** invalida el refresh token (DELETE o `revoked=true`). El access token sigue siendo válido hasta su expiración (max 15 min) — esto es aceptable porque es corto.

**Rotación**: idealmente cada refresh emite un token nuevo y revoca el viejo. Verificar en `AuthService.refresh()` si está implementado.

---

## bcrypt

Passwords se hashean con `BCryptPasswordEncoder` (Spring Security):

```java
String hash = passwordEncoder.encode(rawPassword);     // al registrar
boolean ok = passwordEncoder.matches(raw, hashed);     // al loguearse
```

Cost factor: el default de Spring (10).

> En seeders, los hashes vienen pregenerados (`$2b$12$...`). Si cambias el `BCryptVersion` en Spring puede que no validen — más detalle en [`02-getting-started/seeders.md`](../02-getting-started/seeders.md).

---

## Recomendaciones para producción

- **Rotar `JWT_SECRET` periódicamente** (con periodo de gracia).
- **HTTPS obligatorio** — sin TLS, los tokens van en claro.
- **CORS restrictivo**: en producción, `CORS_ALLOWED_ORIGINS` debe ser solo el dominio real, no `*`.
- **Rate limiting** en `/api/auth/login` (no implementado todavía).
- **Logs de auditoría** de login/logout/cambio de rol (no implementado).
- **Considera reducir** `jwt.access-expiration` a 5 min en producción (ya es corto: 15 min).
- **Añadir 2FA** (no implementado).

---

## Referencias en código

- `security/SecurityConfig.java` — bean de filtros + reglas de URL.
- `security/JwtAuthenticationFilter.java` — extracción y validación del Bearer.
- `security/JwtTokenProvider.java` — emisión y validación de tokens.
- `security/CourseSecurityService.java` — helpers de `@PreAuthorize`.
- `service/AuthService.java` — login, register, refresh, logout.
- `entity/RefreshTokenEntity.java` — modelo del refresh token.
