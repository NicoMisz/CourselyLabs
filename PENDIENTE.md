# Pendiente de implementar

## BACKEND

### Inmediato (antes de arrancar el servidor)
- [x] **Reinicializar la DB**: ejecutar el nuevo `init.sql` (añade tabla `refresh_tokens`, corrige constraint de roles a `admin/user/premium`)
  ```bash
  # Borrar y recrear la DB, o ejecutar manualmente:
  # ALTER TABLE users DROP CONSTRAINT users_role_check;
  # ALTER TABLE users ADD CONSTRAINT users_role_check CHECK (role IN ('admin', 'user', 'premium'));
  # CREATE TABLE refresh_tokens (...) -- ver init.sql
  ```

### Mejoras de seguridad
- [ ] **Manejar `BadCredentialsException`**: cuando el login falla (email o password incorrectos), Spring Security lanza `BadCredentialsException` que llega al handler genérico devolviendo 500. Añadir handler específico en `GlobalExceptionHandler` que devuelva 401.
- [ ] **Securizar `POST /api/users`**: el endpoint `UserController.create()` acepta un campo `role` libre — cualquiera podría crear un admin. Opciones: eliminarlo (usar solo `/api/auth/register`), o restringirlo a `hasRole("ADMIN")`.
- [ ] **Limpiar `CorsConfig.java`**: el archivo quedó vacío (solo un comentario). Se puede eliminar.

### Opcional / Futuro
- [ ] Limpiar `UserRepository.findByRoleAndIsActiveTrue()` si no se usa en ningún otro lugar
- [ ] Endpoint para cambiar contraseña (`PATCH /api/users/{id}/password`)
- [ ] Limpieza periódica de refresh tokens expirados (Spring Scheduler)

---

## FRONTEND

### Auth store (Pinia) — `stores/auth.ts`
- [x] Estado: `accessToken`, `refreshToken`, `user`, computed `isLoggedIn`, `userRole`
- [x] Acciones: `login()`, `register()`, `logout()`
- [x] Persistencia: guardar tokens en `localStorage`

### Axios configurado — `src/api/axios.ts`
- [x] Instancia con `baseURL = import.meta.env.VITE_API_BASE_URL`
- [x] Interceptor de request: inyectar `Authorization: Bearer <token>` en cada petición
- [x] Interceptor de response: si recibe 401 → intentar refresh token automático → si falla → logout y redirigir a login

### Router guards — `src/router/index.ts`
- [x] Añadir `meta: { requiresAuth: true }` a rutas protegidas
- [x] `router.beforeEach`: si la ruta requiere auth y no hay token → redirigir a `/login`

### Vistas y componentes
- [x] **formLogin.vue**: conectado con `authStore.login()`, maneja errores de credenciales
- [x] **formRegister.vue**: simplificado y conectado con `authStore.register()`
- [x] **AppHeader.vue**: muestra nombre de usuario si está logueado + botón de logout
- [x] **ProfileView.vue**: implementada vista básica de perfil

### Tipos TypeScript
- [x] Crear tipos/interfaces: `User`, `AuthResponse`, `LoginRequest`, `RegisterRequest`

### Opcional / Futuro
- [ ] Añadir redirect after login (guardar ruta de destino antes de redirigir a `/login`)
- [ ] Mejorar ProfileView con edición de perfil (`PUT /api/users/{id}`)
- [ ] Conectar `CoursesView.vue` al axios configurado (actualmente usa axios directo sin token)

---

## PRÓXIMOS PASOS SUGERIDOS (en orden)
1. Reinicializar la DB
2. Probar los endpoints de auth con Postman/curl
3. Arreglar `BadCredentialsException` en el backend (devuelve 500 en vez de 401)
4. Securizar `POST /api/users`
