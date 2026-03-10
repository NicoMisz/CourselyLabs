# Pendiente de implementar

## BACKEND

### Implementado
- [x] **Reinicializar la DB**: tabla `refresh_tokens`, constraint de roles
- [x] **Autenticación completa**: login, register, refresh token, logout (`/api/auth/*`)
- [x] **CRUD de usuarios**: `UserController` con endpoints de listado, creación, actualización, desactivación, verificación
- [x] **CRUD de cursos**: `CourseController` con listado paginado, búsqueda, publicación, slug, filtrado por categoría y estado
- [x] **CRUD de categorías**: `CategoriaController` con slug
- [x] **Enrollments**: inscripción, desinscripción, verificación, paginación, tracking de último acceso
- [x] **Reviews**: valoraciones por curso con media, paginación, constraint de una review por usuario/curso
- [x] **Instructores por curso**: `CourseInstructorEntity` con soporte multi-instructor e instructor principal
- [x] **JWT + Spring Security**: access token (15min) + refresh token (7d en DB), filtro stateless, RBAC
- [x] **Control de acceso por roles**: rutas admin-only (listar usuarios, verificar, eliminar), rutas públicas (cursos, categorías)
- [x] **GlobalExceptionHandler**: handlers para ResourceNotFound, BadRequest, Unauthorized, validación

### Mejoras de seguridad
- [ ] **Manejar `BadCredentialsException`**: login fallido devuelve 500 en vez de 401. Añadir handler específico en `GlobalExceptionHandler`.
- [ ] **Securizar `POST /api/users`**: `UserController.create()` acepta campo `role` libre — cualquiera podría crear un admin. Restringir a `hasRole("ADMIN")` o eliminarlo.
- [ ] **Limpiar `CorsConfig.java`**: archivo vacío, la config CORS está en `SecurityConfig`. Se puede eliminar.

### Opcional / Futuro
- [ ] Limpiar `UserRepository.findByRoleAndIsActiveTrue()` si no se usa
- [ ] Endpoint para cambiar contraseña (`PATCH /api/users/{id}/password`)
- [ ] Limpieza periódica de refresh tokens expirados (Spring Scheduler)
- [ ] Secciones y lecciones de cursos (entidades + CRUD)
- [ ] Sistema de foros por curso
- [ ] Mensajería privada entre usuarios
- [ ] Sistema de notificaciones (WebSocket/SSE)
- [ ] Pasarela de pagos (Stripe)
- [ ] Sistema de cupones de descuento
- [ ] Programa de afiliados

---

## FRONTEND

### Implementado
- [x] **Auth store (Pinia)** — `stores/auth.ts`: estado, acciones (login, register, logout), persistencia en localStorage
- [x] **Axios configurado** — `src/api/axios.ts`: baseURL, interceptor de request (Bearer token), interceptor de response (refresh automático)
- [x] **Router guards** — `src/router/index.ts`: `meta.requiresAuth`, `beforeEach` con redirección a login
- [x] **formLogin.vue**: conectado con `authStore.login()`, validación, manejo de errores
- [x] **formRegister.vue**: conectado con `authStore.register()`, aceptación de T&C
- [x] **cardTerminosCondiciones.vue**: modal de términos y condiciones
- [x] **AppHeader.vue**: nombre de usuario + menú con logout y perfil
- [x] **AppSidebar.vue**: navegación lateral (Home, Cursos, Carrito)
- [x] **AppFooter.vue**: footer con copyright
- [x] **MainLayout.vue / AltLayout.vue**: layouts con header + sidebar + footer
- [x] **ProfileView.vue**: vista de perfil con datos del usuario y badge de rol
- [x] **CoursesView.vue**: listado de cursos en grid responsivo con carga desde API
- [x] **Tipos TypeScript** — `types/auth.ts`: `User`, `AuthResponse`, `LoginRequest`, `RegisterRequest`

### Pendiente
- [ ] Añadir redirect after login (guardar ruta de destino antes de redirigir a `/login`)
- [ ] Mejorar ProfileView con edición de perfil (`PUT /api/users/{id}`)
- [ ] Conectar `CoursesView.vue` al axios configurado (actualmente usa axios directo sin token)
- [ ] Página de detalle de curso individual (`/cursos/:id`)
- [ ] Componente de inscripción/compra de curso
- [ ] Vista de contenido del curso (lecciones, secciones)
- [ ] Reproductor de vídeo (Video.js + HLS)
- [ ] Dashboard de instructor (crear/editar cursos)
- [ ] Carrito de compra (ruta existe en sidebar pero sin componente)
- [ ] Búsqueda y filtros de cursos
- [ ] Sistema de foros por curso
- [ ] Mensajería privada
- [ ] Limpiar componentes demo no usados (HelloWorld, TheWelcome, pruebaView, etc.)

---

## PRÓXIMOS PASOS SUGERIDOS (en orden)
1. Arreglar `BadCredentialsException` en el backend (devuelve 500 en vez de 401)
2. Securizar `POST /api/users`
3. Conectar `CoursesView.vue` al axios configurado
4. Implementar página de detalle de curso
5. Crear entidades de secciones/lecciones en backend
6. Implementar vista de contenido de curso
