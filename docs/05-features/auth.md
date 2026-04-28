# Auth — Registro, login, JWT, refresh, verificación de email

> Flujo completo de autenticación. Detalles técnicos de Spring Security en [`04-backend/security.md`](../04-backend/security.md).

---

## Flujos

### Registro

```
1. Usuario en /register llena formulario {firstName, lastName, email, password}
2. POST /api/auth/register
3. Backend (AuthService.register):
   - Valida email único, contraseña con reglas mínimas (longitud)
   - Hash bcrypt
   - Inserta UserEntity con role='user', email_verified=false, is_active=true
   - Genera VerificationTokenEntity (UUID con expiración a 24h)
   - Envía email con link: {FRONTEND_URL}/verificar-email?token=xxx
   - Genera accessToken + refreshToken y devuelve sesión
4. Frontend guarda sesión y redirige a / (puede usar la app inmediatamente)
5. El usuario debe verificar email para acceder a ciertas features
```

### Verificación de email

```
1. Usuario hace click en el link del email
2. Frontend en /verificar-email lee ?token= y hace POST /api/auth/verify-email {token}
3. Backend:
   - Busca token, valida no caducado, no usado
   - Marca user.email_verified = true
   - Borra el token
4. Frontend muestra "Verificado correctamente"
```

Para reenviar:
- Botón en /profile o /verificar-email → POST /api/auth/resend-verification (autenticado)
- Backend invalida tokens previos del usuario y emite uno nuevo

### Login

```
1. POST /api/auth/login {email, password}
2. AuthService:
   - Busca user por email (case-insensitive)
   - Si no existe o contraseña no coincide → 400 BadRequest "Credenciales inválidas"
   - Si user.is_active = false → 403 "Cuenta desactivada"
   - Genera accessToken (15 min) + refreshToken (7 días)
   - Persiste refreshToken en BD
3. Devuelve {accessToken, refreshToken, user}
4. Frontend guarda en localStorage y store
```

### Refresh

```
1. Cliente axios recibe 401 de cualquier endpoint
2. Interceptor:
   - Lee refreshToken del store
   - POST /api/auth/refresh {refreshToken}
3. Backend:
   - Busca el token en BD
   - Si está revocado o expirado → 401
   - Genera nuevo accessToken (y, opcional, rota refresh)
   - Actualiza last_used_at
4. Devuelve nueva sesión
5. Interceptor reintenta la request original con el nuevo accessToken
```

Si el refresh falla:
- Frontend llama `clearSession()` (limpia localStorage + Pinia)
- Redirige a /login con `?redirect=<ruta original>`

### Logout

```
1. POST /api/auth/logout {refreshToken} (autenticado)
2. Backend invalida el refreshToken (DELETE o revoked=true)
3. Frontend limpia localStorage y store, redirige a /
```

> El accessToken NO se invalida en BD (es stateless). Sigue siendo válido hasta su expiración (max 15 min). Para casos críticos (ej. cuenta comprometida), conviene reducir la duración o mantener una blacklist.

---

## Backend

### Endpoints

| Método | Path | Auth | Descripción |
|---|---|---|---|
| `POST` | `/api/auth/register` | público | Crear cuenta |
| `POST` | `/api/auth/login` | público | Login |
| `POST` | `/api/auth/refresh` | público | Renovar accessToken |
| `POST` | `/api/auth/logout` | autenticado | Invalidar refreshToken |
| `POST` | `/api/auth/verify-email` | público | Verificar email con token |
| `POST` | `/api/auth/resend-verification` | autenticado | Reenviar email de verificación |

### DTOs

```java
RegisterRequest  { String firstName, lastName, email, password; }
LoginRequest     { String email, password; }
RefreshRequest   { String refreshToken; }
VerifyRequest    { String token; }
AuthResponse     { String accessToken, refreshToken; UserDTO user; }
UserDTO          { UUID id; String firstName, lastName, email, role; boolean emailVerified; ... }
```

### Tablas implicadas

- `users` — datos del usuario
- `verification_tokens` — tokens de verificación de email (V4)
- `refresh_tokens` — refresh tokens persistentes

---

## Frontend

### Archivos

- **Store**: `stores/auth.ts` (ver [`03-frontend/state.md`](../03-frontend/state.md)).
- **API**: `api/auth.ts` (`login`, `register`, `refresh`, `logout`, `verifyEmail`, `resendVerification`).
- **Vistas**: `views/formView.vue` (login), `components/formRegister.vue` (registro), `views/VerifyEmailView.vue`.
- **Interceptor**: `api/axios.ts` añade `Authorization: Bearer` y maneja refresh automático en 401.

### Persistencia

Tras login exitoso, el store guarda en `localStorage`:
- `accessToken`
- `refreshToken`
- `user` (JSON con datos)

Al recargar la página (`main.ts` → `authStore.checkSession()`):
- Si hay `refreshToken` y `user`, intenta refrescar.
- Si refresca, sesión activa.
- Si falla, limpia todo.

### Guards de ruta

`router/index.ts`:
- `requiresAuth: true` → redirige a `/login?redirect=...` si no logueado.
- `requiresRole: 'admin'` → redirige a `/` si no es admin.

---

## Validaciones de negocio

### Registro
- Email no debe existir ya.
- Password mínimo 8 caracteres.
- Email con formato válido.

### Login
- Email + password coincidente.
- Cuenta activa.

### Verificación
- Token no caducado (24h).
- Token no usado.
- Cuenta no ya verificada (404 si lo está).

---

## Limitaciones conocidas

- **Sin reset de password** — feature pendiente. Sería un flujo similar al de verificación con `password_reset_tokens`.
- **Sin rate limiting** en `/api/auth/login` — vulnerable a fuerza bruta. Idealmente con un filtro o gateway.
- **Sin 2FA**.
- **Sin login social** (OAuth2 Google) — está en `CLAUDE.md` como visión futura.
- **El email de verificación llega solo en local** vía MailHog. En producción necesita SMTP real (SendGrid, AWS SES, etc.).
- **Bcrypt cost = 10** (default Spring). Considerar 12 en producción si la latencia lo permite.

---

## Referencias en código

- `service/AuthService.java`
- `service/EmailService.java`
- `controller/AuthController.java`
- `security/SecurityConfig.java`
- `security/JwtTokenProvider.java`
- `security/JwtAuthenticationFilter.java`
- `entity/RefreshTokenEntity.java`, `entity/VerificationTokenEntity.java`
- Frontend: `stores/auth.ts`, `api/auth.ts`, `api/axios.ts`
