# Plan de Ramas — CourselyLabs

Cada sección corresponde a una rama de git independiente desde `develop`. Las ramas con dependencias deben mergearse en orden.

---

## Estado de la base del proyecto

### Backend — Implementado ✅

- [x] **DB inicializada**: tabla `refresh_tokens`, constraint de roles (`admin/user/premium`)
- [x] **Autenticación completa**: login, register, refresh token, logout — `POST/GET /api/auth/*`
- [x] **CRUD de usuarios**: `UserController` — listado, creación (admin-only), actualización, desactivación, verificación
- [x] **CRUD de cursos**: `CourseController` — listado paginado, búsqueda, publicación, slug, filtrado por categoría y estado
- [x] **CRUD de categorías**: `CategoriaController` con slug
- [x] **Enrollments**: inscripción, desinscripción, verificación, paginación, tracking de último acceso
- [x] **Reviews**: valoraciones por curso con media, paginación, constraint de una review por usuario/curso
- [x] **Instructores multi-curso**: `CourseInstructorEntity` con soporte para múltiples instructores e instructor principal
- [x] **JWT + Spring Security**: access token (15 min) + refresh token (7 días en DB), filtro stateless, RBAC
- [x] **Control de acceso**: rutas admin-only (listar usuarios, verificar, eliminar), rutas públicas (cursos, categorías)
- [x] **GlobalExceptionHandler**: handlers para `ResourceNotFoundException`, `BadRequestException`, `UnauthorizedException`, validación Jakarta
- [x] **Flyway**: migración V1 con schema completo (7 tablas), `baseline-on-migrate=true`
- [x] **JWT secret**: fallback para dev, sobreescribir con `JWT_SECRET` en produccion

### Frontend — Implementado ✅

- [x] **Auth store (Pinia)** — `stores/auth.ts`: estado, acciones (login, register, logout), persistencia en localStorage
- [x] **Axios configurado** — `api/axios.ts`: baseURL desde `.env`, interceptor Bearer token, interceptor de refresh automático en 401
- [x] **Router guards** — `router/index.ts`: `meta.requiresAuth`, `beforeEach` con redirección a `/login?redirect=`, `afterEach` para `document.title`
- [x] **formLogin.vue**: conectado con `authStore.login()`, validación, manejo de errores, shake animation
- [x] **formRegister.vue**: conectado con `authStore.register()`, aceptación de T&C
- [x] **cardTerminosCondiciones.vue**: modal de términos y condiciones (8 secciones)
- [x] **AppHeader.vue**: nombre de usuario, menú con logout y perfil
- [x] **AppSidebar.vue**: navegación lateral (Home, Cursos, Carrito)
- [x] **AppFooter.vue**: footer con copyright
- [x] **MainLayout.vue / AltLayout.vue**: layouts con header + sidebar + footer
- [x] **ProfileView.vue**: vista de perfil con datos del usuario, badge de rol, formulario de edicion inline (PUT /api/users/{id})
- [x] **CoursesView.vue**: listado de cursos con `CourseCard`, `q-skeleton`, composable `useCourses`
- [x] **HomeView.vue**: landing real con hero, cursos destacados, categorias, CTA instructor, "Continuar aprendiendo"
- [x] **NotFoundView.vue**: pagina 404 con boton "Volver al inicio"
- [x] **CourseCard.vue**: card reutilizable con thumbnail fallback, LevelBadge, PriceBadge, rating, hover
- [x] **composables/useCourses.ts**: logica de fetch separada del componente
- [x] **Tipos TypeScript** — `types/auth.ts`: `User`, `AuthResponse`, `LoginRequest`, `RegisterRequest`
- [x] **Tipos TypeScript** — `types/course.ts`: `Course`, `CourseDetail`, `InstructorSummary`
- [x] **Imports**: todos migrados a alias `@` (sin `../`)
- [x] **API course** — `api/course.ts`: `getCourseBySlug()`, `getCourseInstructors()` usando axios configurado
- [x] **CourseDetailView.vue**: vista `/cursos/:slug` con skeleton, 404, error retry, tabs y sidebar
- [x] **CourseHero.vue**: thumbnail con fallback gradiente, badges, contador formateado, fecha
- [x] **CourseSidebar.vue**: resumen del curso con botón de inscripción (placeholder)
- [x] **LevelBadge.vue** / **PriceBadge.vue** / **CourseBreadcrumb.vue**: componentes reutilizables

### Bugs y mejoras pendientes de autenticacion/perfil

- [x] **FIX: localStorage sync en edicion de perfil** — Añadido `updateUser()` en auth store que sincroniza Pinia + localStorage. `ProfileView` ahora usa `authStore.updateUser(data)`.
- [x] **Endpoint de cambio de password** — `PATCH /api/auth/change-password` con `ChangePasswordDTO` (currentPassword + newPassword min 8). Ruta protegida en SecurityConfig (excluida del `permitAll` de `/api/auth/**`).
- [x] **Rediseño de ProfileView** — Layout profesional: header gradient con avatar+iniciales, info personal editable, seccion seguridad con cambio de contraseña (toggle visibility), estadisticas de cursos, fecha de registro.
- [x] **Verificacion de email** — Implementado con JavaMailSender + MailHog (dev). Flyway V4 (`verification_tokens`). `EmailService` genera token UUID con 24h de expiración, envía HTML con botón de verificación. Endpoints: `GET /api/auth/verify?token=`, `POST /api/auth/resend-verification?email=`. Frontend: `VerifyEmailView.vue` (ruta `/verificar-email?token=`), banner de warning en `ProfileView` con botón "Reenviar email". Registro envía email automáticamente (no bloquea si falla). Para producción: cambiar `MAIL_HOST`/`MAIL_PORT` a Gmail SMTP o servidor propio.
- [ ] **No hay logout de todos los dispositivos** — Solo se revoca el refresh token actual. Si el usuario tiene sesion en otro navegador, sigue activa. Fix: endpoint `POST /api/auth/logout-all` que revoque todos los refresh tokens del usuario.
- [ ] **El refresh token no rota** — Al refrescar, se devuelve el mismo token en vez de generar uno nuevo y revocar el anterior. Esto reduce la seguridad ante robo de refresh token. Fix: generar nuevo refresh token en cada refresh y revocar el anterior.

---

## 1. `fix/security-improvements` ✅ COMPLETADO

**Prioridad:** Crítica
**Dependencias:** Ninguna

### Descripción
Cierre de vulnerabilidades de seguridad en backend y mejora del feedback de errores de autenticación en frontend.

### Tareas

#### Backend
- [x] Añadir handler `BadCredentialsException` en `GlobalExceptionHandler` → 401 con mensaje "Email o contraseña incorrectos"
- [x] Añadir handler `AuthenticationException` genérico → 401 con mensaje "No autenticado"
- [x] Securizar `POST /api/users` con `@PreAuthorize("hasRole('ADMIN')")` — evita que cualquiera cree un admin
- [x] Eliminar `CorsConfig.java` (vacío, CORS configurado en `SecurityConfig`)
- [x] Eliminar `UserRepository.findByRoleAndIsActiveTrue()` (sin uso)

#### Frontend
- [x] `formLogin.vue`: detectar solo `status === 401` para "Email o contraseña incorrectos" (eliminar `|| status === 500`)
- [x] `formLogin.vue`: añadir animación `shake` al formulario cuando falla el login

---

## 2. `chore/codebase-cleanup` ✅ COMPLETADO

**Prioridad:** Alta — fundacional, debe ir antes que cualquier feature
**Dependencias:** `fix/security-improvements`

### Descripción
Limpieza del scaffold inicial de Vue, corrección de patrones de código y funcionalidades básicas de la app (HomeView real, 404, perfil editable) que no encajan en ninguna feature específica pero son necesarias antes de construir encima.

### Diagnóstico del estado actual

| Archivo | Estado |
|---|---|
| `CoursesView.vue` | ✅ Usa `useCourses` composable + `CourseCard` + `q-skeleton` |
| `HomeView.vue` | ✅ Landing real: hero, cursos destacados, categorias, CTA instructor |
| `router/routes.ts` | ✅ Lazy loading, `meta.title`, alias `@` en imports |
| `router/index.ts` | ✅ `afterEach` para `document.title`, redirect after login con `?redirect=` |
| Imports | ✅ Migrados a alias `@` en todo el proyecto |
| Archivos scaffold | ✅ Eliminados (HelloWorld, TheWelcome, WelcomeItem, EssentialLinks, headerComp, icons/, pruebaView, AboutView) |
| `ProfileView.vue` | ✅ Con formulario de edicion inline (PUT /api/users/{id}) |
| `application.properties` | ✅ JWT secret con fallback para dev (sobreescribir en produccion) |
| Flyway | ✅ Dependencia + V1 migration con schema completo |
| Spring Boot | ℹ️ `4.1.0-M1` — se actualizará cuando salga la GA |

### Tareas

#### Frontend — Limpieza de archivos sin uso
- [x] Eliminar `components/HelloWorld.vue`, `TheWelcome.vue`, `WelcomeItem.vue`, `EssentialLinks.vue`, `headerComp.vue`, carpeta `icons/`
- [x] Eliminar `views/pruebaView.vue` (importa componentes que no existen — error en runtime)
- [x] Reemplazar `views/AboutView.vue` con `views/NotFoundView.vue` — pagina 404 real con boton "Volver al inicio"

#### Frontend — Arquitectura de imports
- [x] Usar alias `@` en todos los imports del proyecto
- [x] Añadir interfaz `Course` a `src/types/course.ts` (extraida de la inline en `CoursesView.vue`)
- [x] Crear `src/composables/useCourses.ts` — logica de fetch separada del componente

#### Frontend — CoursesView refactor
- [x] Reemplazar `import axios from 'axios'` por composable `useCourses` (usa `api` de `@/api/axios`)
- [x] Sustituir `q-spinner` por 6 `q-skeleton` cards animadas mientras carga
- [x] Crear `components/CourseCard.vue` — componente reutilizable: thumbnail (con fallback), titulo, `LevelBadge`, `PriceBadge`, rating

#### Frontend — Router
- [x] Lazy loading con alias `@` en todas las rutas
- [x] Añadir `meta: { title: 'Cursos — CourselyLabs' }` a cada ruta
- [x] `router/index.ts`: `router.afterEach` que actualiza `document.title` con `to.meta.title`
- [x] Redirect after login: `beforeEach` redirige a `/login?redirect=${to.fullPath}`; `formLogin.vue` usa `route.query.redirect`

#### Frontend — HomeView real
- [x] **Hero section**: titulo "Aprende con CourselyLabs", subtitulo, boton "Explorar cursos" (`$accent`)
- [x] **Seccion "Cursos destacados"**: llamada a `/api/courses/all`, grid de `CourseCard` (primeros 4)
- [x] **Seccion "Categorias"**: llamada a `/api/categories`, chips clickables → `/cursos?cat=id`
- [x] **Seccion CTA instructor**: "¿Quieres enseñar?" + boton "Empieza aqui" → `/instructor`
- [x] **Si autenticado**: seccion "Continuar aprendiendo" con el ultimo curso accedido

#### Frontend — Perfil con edicion
- [x] `ProfileView.vue`: toggle "Editar perfil" / "Cancelar"
- [x] Formulario de edicion inline: `firstName`, `lastName`, `bio`
- [x] Llamada `PUT /api/users/{id}` (con el token del usuario logueado)
- [x] Validacion: nombre min 2 chars; `q-notify` "Perfil actualizado" al guardar exitoso

#### Backend
- [x] `application.properties`: JWT secret con fallback para dev, sobreescribir con `JWT_SECRET` en produccion
- [x] Crear `src/main/resources/db/migration/V1__initial_schema.sql` con el DDL de las 7 tablas
- [x] Añadir dependencia Flyway (`flyway-core` + `flyway-database-postgresql`) al `pom.xml` + `spring.flyway.enabled=true` + `baseline-on-migrate=true`

#### Archivos creados
- `views/NotFoundView.vue` ✅
- `components/CourseCard.vue` ✅
- `composables/useCourses.ts` ✅
- `src/main/resources/db/migration/V1__initial_schema.sql` ✅

---

## 3. `feature/course-detail-page` ✅ COMPLETADO

**Prioridad:** Alta — Página central del producto
**Dependencias:** Ninguna

### Descripción
Página individual de un curso en `/cursos/:slug` con toda su información organizada en pestañas y un sidebar sticky.

### Diagnóstico del estado actual

| Elemento | Estado |
|---|---|
| `GET /api/courses/slug/{slug}` | ✅ Existe — devuelve 404 si no existe |
| `GET /api/courses/{id}/instructors` | ✅ Creado |
| `CourseDetailDTO` con media de reviews, total de estudiantes e instructores | ✅ Implementado |
| Vista `/cursos/:slug` en frontend | ✅ Creada (`CourseDetailView.vue`) |
| `types/course.ts` con `CourseDetail` e `InstructorSummary` | ✅ Creado |
| Tabs como componentes separados | ✅ Extraidos (Description, Content, Instructors, Reviews) |
| Fecha de actualización relativa | ✅ "Actualizado hace 3 dias" |
| Scroll suave a tabs | ✅ `scrollIntoView({ behavior: 'smooth' })` |

### Tareas

#### Backend
- [x] Verificar o crear `GET /api/courses/{id}/instructors`
- [x] Añadir al DTO de curso: media de reviews, total de estudiantes inscritos, lista de instructores
- [x] `GET /api/courses/slug/{slug}` debe devolver 404 con mensaje claro si no existe

#### Frontend — Tipos
- [x] Crear `types/course.ts` con interfaz `CourseDetail` completa (id, slug, title, shortDescription, description, level, isFree, price, thumbnailUrl, categoryName, studentsCount, averageRating, updatedAt, instructors)

#### Frontend — Ruta y vista
- [x] Añadir `{ path: '/cursos/:slug', component: () => import('views/CourseDetailView.vue') }` en `routes.ts`
- [x] Crear `views/CourseDetailView.vue` — contenedor principal con llamada a API y manejo de loading/error/404

#### Frontend — Componentes
- [x] `CourseHero.vue` — thumbnail (con fallback gradiente), badges, contador formateado, fecha relativa
- [x] `CourseSidebar.vue` — nivel, nº estudiantes, duración, valoración, botón inscripción (placeholder)
- [x] `CourseTabDescription.vue` — descripcion completa
- [x] `CourseTabContent.vue` — placeholder (se completara en `feature/course-sections-lessons`)
- [x] `CourseTabInstructors.vue` — cards con avatar (iniciales), nombre, bio
- [x] `CourseTabReviews.vue` — placeholder (se completara en `feature/reviews-frontend`)
- [x] `LevelBadge.vue` — chip reutilizable
- [x] `PriceBadge.vue` — chip reutilizable
- [x] `CourseBreadcrumb.vue` — `Home > Cursos > [Categoría] > [Título]`
- [x] `api/course.ts` — `getCourseBySlug()` y `getCourseInstructors()` usando axios configurado

#### UX/UI
- [x] Skeleton loading con `q-skeleton` para hero, tabs y sidebar mientras carga la API
- [x] Breadcrumb con categoría
- [x] Scroll suave a las tabs con `scrollIntoView({ behavior: 'smooth' })` al hacer click
- [x] Meta tags OG dinámicos (`og:title`, `og:description`, `og:image`)
- [x] Fecha de última actualización relativa ("Actualizado hace 3 dias")
- [x] Contador de estudiantes formateado ("1.2K estudiantes" si > 1000)
- [x] Página "Curso no encontrado" con botón "Explorar cursos"

---

## 4. `feature/enrollment-flow` -- COMPLETADO

**Prioridad:** Alta
**Dependencias:** `feature/course-detail-page`

### Descripcion
Inscripcion de usuarios en cursos gratuitos, vista "Mis cursos" y conexion del axios configurado en CoursesView.

### Estado final

| Elemento | Estado |
|---|---|
| `POST /api/enrollments/me` | Hecho — endpoint + API client |
| `GET /api/enrollments/check` | Hecho — endpoint + API client |
| `GET /api/enrollments/me/courses` | Hecho — endpoint + API client (`getMyCourses`) |
| `PATCH /api/enrollments/{id}/access` | Hecho — API client (`updateEnrollmentLastAccess`) |
| `CoursesView.vue` — axios configurado | Hecho — ya estaba via composable |
| `CourseSidebar.vue` | Hecho — logica de inscripcion: guest (login redirect), free (enroll), paid (disabled "Proximamente"), enrolled (continuar) |
| `EnrollSuccessDialog.vue` | Hecho — dialog con checkmark gradient + "Seguir explorando" / "Ir al curso" |
| `CourseDetailView.vue` | Hecho — integra enrollment: check + create + banner error + dialog |
| `MyCoursesView.vue` | Hecho — vista `/mis-cursos` con tabs Todos/No iniciados/En curso/Completados + ordenacion |
| `CourseCardEnrolled.vue` | Hecho — thumbnail + progress bar superpuesta + badge estado + hover scale |
| `EmptyState.vue` | Hecho — componente reutilizable: icono + titulo + descripcion + CTA |
| Ruta `/mis-cursos` | Hecho — registrada con `requiresAuth: true` |
| Types `enrollment.ts` | Hecho — `CreateEnrollmentPayload`, `EnrollmentResponse`, `EnrolledCourse` |
| API `enrollment.ts` | Hecho — `checkEnrollment`, `createEnrollment`, `getMyCourses`, `updateEnrollmentLastAccess` |
| `HomeView.vue` — "Continuar aprendiendo" | Hecho — usa `CourseCardEnrolled` + `EnrolledCourse` + `getMyCourses` |
| Iconos Material Icons | Hecho — migrados de Line Awesome a Material Icons en Sidebar y HomeView |
| Sidebar modo mini | Hecho — tooltips en modo mini + header oculto cuando mini |
| `CourseDetail.isFree` | Hecho — renombrado de `free` a `isFree` para coincidir con el JSON del backend |
| Axios session sync | Hecho — interceptor sincroniza Pinia al refrescar/expirar token via callbacks |

### Fixes aplicados
- [x] Creado `EmptyState.vue` — import descomentado en `MyCoursesView`
- [x] `HomeView.vue` — cambiado de `Course`/`CourseCard` a `EnrolledCourse`/`CourseCardEnrolled` para "Continuar aprendiendo", usa `getMyCourses` del API client
- [x] Migrados iconos Line Awesome a Material Icons en `AppSidebar` (`home`, `school`, `shopping_cart`) y `HomeView` (`school`, `co_present`)
- [x] `AppSidebar.vue` — tooltips con nombre del link en modo mini, header "Navegacion" oculto en mini
- [x] Eliminado `EnrollButton.vue` — no se usaba, logica duplicada en `CourseSidebar.vue`
- [x] `CourseDetail` renombrado campo `free` a `isFree` — el backend envia `"isFree"` en JSON, no `"free"`
- [x] `CourseSidebar.vue` — prop renombrada a `isFree`, cursos de pago muestran "Proximamente" deshabilitado (flujo de pago pendiente en `feature/payments-stripe`)
- [x] `MyCoursesView.vue` — añadida pestaña "No iniciados" (cursos con `progressPercent === 0`)
- [x] `api/axios.ts` — añadido sistema de callbacks (`setSessionCallbacks`) para sincronizar Pinia cuando el interceptor refresca o expira el token, evitando estado inconsistente entre localStorage y el store

---

## 5. `feature/course-sections-lessons` ✅ COMPLETADO

**Prioridad:** Alta
**Dependencias:** `feature/enrollment-flow`

### Descripcion
Entidades de secciones y lecciones en backend y vista de leccion en frontend con reproductor de video, visor de texto y PDF.

### Estado actual

| Elemento | Estado |
|---|---|
| Flyway V2 migration (sections, lessons) | Hecho |
| `SectionEntity` + `LessonEntity` | Hecho |
| `SectionDTO` + `LessonDTO` + `ReorderRequestDTO` | Hecho |
| `SectionRepository` + `LessonRepository` | Hecho |
| `SectionMapper` + `LessonMapper` | Hecho |
| `SectionService` + `LessonService` | Hecho — CRUD + reorder |
| `SectionController` + `LessonController` | Hecho — 9 endpoints |
| `CourseDetailDTO.sections` | Hecho — secciones incluidas en detalle del curso |
| Types `lesson.ts` | Hecho — `Section`, `Lesson`, `LessonType` |
| API `lesson.ts` | Hecho — `getCourseSections`, `getLessonById` |
| `LessonTypeIcon.vue` | Hecho — icono por tipo (video/text/pdf/audio) |
| `CourseSectionList.vue` | Hecho — expansion-items, duracion, badge Preview, candado |
| `CourseNavSidebar.vue` | Hecho — drawer 300px, leccion activa highlighted |
| `LessonVideoPlayer.vue` | Hecho — Video.js, velocidad guardada en localStorage, PiP |
| `LessonTextViewer.vue` | Hecho — marked + highlight.js |
| `LessonPdfViewer.vue` | Hecho — embed nativo + fallback descarga |
| `LessonNavBar.vue` | Hecho — barra inferior sticky con anterior/siguiente |
| `LessonView.vue` | Hecho — layout propio con sidebar + contenido + nav |
| Ruta `/cursos/:slug/leccion/:lessonId` | Hecho — requiresAuth |
| Tab "Contenido" en CourseDetailView | Hecho — usa `CourseSectionList` |
| Deps: video.js, marked, highlight.js | Hecho |

### UX/UI implementado
- [x] Mobile: sidebar como `q-drawer` con breakpoint 1024px + boton hamburguesa
- [x] Velocidad de video preferida guardada en localStorage
- [x] Fade-in del contenido al cambiar de leccion (CSS transition)
- [x] Skeleton del contenido principal mientras carga
- [x] Contador por seccion en el arbol ("N lecciones")

### Pendiente para futuras iteraciones
- [ ] Autoplay de siguiente leccion con countdown de 5s cancelable
- [ ] Atajos de teclado para video (espacio, flechas, F)
- [x] Porcentaje total de curso arriba del sidebar (implementado en `feature/student-progress`)
- [ ] Mini barra de progreso por seccion (pendiente)
- [ ] Autorizacion: solo instructor del curso puede crear/editar secciones y lecciones
- [x] Seed de datos de prueba — `seed_postgresql_course.sql` + `seed_flamenco_course.sql` + `seed.sh`

---

## 6. `feature/downloadable-resources` 🆕 PENDIENTE

**Prioridad:** Media
**Dependencias:** `feature/course-sections-lessons`

### Descripción
Archivos descargables adjuntos a lecciones (PDFs, código fuente, assets). Descarga protegida por inscripción.

### Tareas

#### Backend — Entidad nueva
- [ ] `LessonResourceEntity`: id, lessonId, fileName, fileUrl, fileSize, mimeType, downloadCount, position, createdAt
- [ ] Migración Flyway

#### Backend — Endpoints
- [ ] `GET /api/lessons/{lessonId}/resources` — listar recursos (público si la lección es free, protegido si no)
- [ ] `POST /api/lessons/{lessonId}/resources` — subir recurso (multipart, solo instructor del curso)
- [ ] `GET /api/resources/{id}/download` — descarga protegida (solo inscritos); incrementa `downloadCount`
- [ ] `DELETE /api/resources/{id}` — eliminar recurso (solo instructor)
- [ ] `PATCH /api/resources/reorder` — reordenar recursos de una lección

#### Backend — Almacenamiento (sistema de upload reutilizable)
- [ ] Servicio generico `FileStorageService` con interfaz comun: `upload(file, path)`, `delete(path)`, `getUrl(path)`
- [ ] Implementacion `LocalFileStorageService` para dev — guarda en `/uploads/` con URL relativa
- [ ] Implementacion `S3FileStorageService` para prod — AWS S3 con URLs firmadas (expiracion configurable, default 1h)
- [ ] MinIO en `docker-compose.yml` como S3 compatible para dev (alternativa a disco local)
- [ ] Endpoint generico `POST /api/upload` — recibe `multipart/form-data`, valida tipo y tamaño, devuelve URL. Reutilizable para thumbnails de cursos, contenido de lecciones (video/pdf) y recursos descargables
- [ ] Validación: tipos permitidos (pdf, zip, txt, código, imagenes, video), máximo configurable via `application.properties` (default 50 MB recursos, 500 MB videos)
- [ ] Conectar `thumbnailUrl` de cursos y `contentUrl` de lecciones a este mismo sistema de upload

#### Frontend — Componentes
- [ ] `LessonResources.vue` — card "Recursos descargables" con lista de archivos; icono por tipo (PDF rojo, ZIP amarillo, código azul, imagen verde), nombre, tamaño legible, botón descarga
- [ ] `ResourcePreviewModal.vue` — preview de PDFs e imágenes en modal antes de descargar
- [ ] `ResourceUploader.vue` — zona drag & drop, múltiples archivos, barra de progreso individual, reordenación
- [ ] `ResourceItem.vue` — item individual de recurso con hover sutil y acción de eliminar (instructor)

#### UX/UI
- [ ] Posición en la vista de lección: debajo del contenido, antes de "Marcar como completada"
- [ ] En el árbol de contenido: icono de clip al lado del título si la lección tiene recursos
- [ ] Click en nombre de PDF/imagen → abre `ResourcePreviewModal` con opción de descargar
- [ ] Click en otros tipos → descarga directa

---

## 7. `feature/student-progress` ✅ COMPLETADO

**Prioridad:** Alta
**Dependencias:** `feature/course-sections-lessons`

### Descripcion
Tracking del progreso del estudiante: marcar lecciones como completadas, guardar posicion de video, progreso por curso.

### Estado actual

| Elemento | Estado |
|---|---|
| Flyway V3 migration (lesson_progress) | Hecho |
| `LessonProgressEntity` (unique user+lesson) | Hecho |
| `LessonProgressDTO` + `CourseProgressDTO` + `PositionUpdateDTO` | Hecho |
| `LessonProgressRepository` (queries por user+course) | Hecho |
| `LessonProgressService` (toggle, position, courseProgress) | Hecho |
| `LessonProgressController` (4 endpoints) | Hecho |
| `EnrollmentService.toEnrolledCourseDTO` — progreso real | Hecho — calcula % desde lesson_progress |
| Types `progress.ts` | Hecho |
| API `progress.ts` | Hecho — toggle, getCourse, getLesson, updatePosition |
| Boton "Marcar como completada" en LessonView | Hecho — toggle con color positive/grey |
| Auto-completar video al 90% visto | Hecho — timeUpdate event |
| `CourseNavSidebar` — barra de progreso + checks | Hecho — linear-progress + "N/M lecciones (X%)" + check icons |
| Video position save (throttled 10s) | Hecho — setInterval + updateLessonPosition |
| Resume banner "Continuar desde MM:SS?" | Hecho — banner con Si/Empezar de nuevo |
| `LessonVideoPlayer` — expose getCurrentTime/seekTo | Hecho — defineExpose + timeUpdate emit |
| Dialog de celebracion al 100% | Hecho — emoji_events gradient + "Has completado el curso!" |
| "Mis cursos" — progreso real del backend | Hecho — EnrollmentService calcula desde lesson_progress |

### Pendiente para futuras iteraciones
- [ ] `q-circular-progress` en cards de "Mis cursos" (40px, porcentaje dentro)
- [ ] Dot pulsante en la primera leccion no completada en `CourseNavSidebar`
- [ ] Dialog de confirmacion al desmarcar leccion completada
- [ ] Animacion confeti CSS en dialog de celebracion

---

## 8. `feature/course-prerequisites` 🆕 PENDIENTE

**Prioridad:** Media
**Dependencias:** `feature/student-progress`

### Descripción
Un curso puede requerir haber completado otro al 100% antes de permitir la inscripción. Validación en backend y feedback visual en frontend.

### Tareas

#### Backend — Entidad nueva
- [ ] `CoursePrerequisiteEntity`: id, courseId, prerequisiteCourseId, createdAt
- [ ] Constraint unique `(courseId, prerequisiteCourseId)`
- [ ] Migración Flyway

#### Backend — Endpoints
- [ ] `GET /api/courses/{id}/prerequisites` — listar prerequisitos con datos del curso y progreso del usuario autenticado
- [ ] `POST /api/courses/{id}/prerequisites` — añadir prerequisito (solo instructor del curso)
- [ ] `DELETE /api/courses/{id}/prerequisites/{prereqId}` — eliminar prerequisito

#### Backend — Lógica
- [ ] En `POST /api/enrollments`: verificar que el usuario tiene progreso 100% en todos los prerequisitos; si no, devolver 409 con lista de cursos bloqueantes
- [ ] Detección de ciclos al añadir un prerequisito (DFS sobre el grafo de dependencias)

#### Frontend — Componentes
- [ ] `CoursePrerequisites.vue` — sección "Antes de empezar" en pestaña Descripción; mini-card por prerequisito con thumbnail, título, nivel badge y estado (completado / en curso / no inscrito)
- [ ] `PrerequisiteBlockBanner.vue` — `q-banner` bajo el botón de inscripción cuando faltan prerequisitos
- [ ] `PrerequisiteSelector.vue` — `q-select` con búsqueda y chips para el editor de curso del instructor

#### UX/UI
- [ ] Botón de inscripción deshabilitado con tooltip "Completa los cursos requeridos primero"
- [ ] Icono de candado en el hero si el usuario no cumple los prerequisitos
- [ ] Aviso si la cadena de prerequisitos tiene N cursos ("Este curso requiere completar una cadena de 3 cursos")
- [ ] No mostrar la sección si el curso no tiene prerequisitos

---

## 9. `feature/reviews-frontend` 🆕 PENDIENTE

**Prioridad:** Media
**Dependencias:** `feature/course-detail-page`, `feature/enrollment-flow`

### Descripción
Interfaz completa para el sistema de reviews (backend ya implementado en `ReviewController`).

### Diagnóstico del estado actual

| Elemento | Estado |
|---|---|
| `ReviewController` en backend | ✅ Implementado (media, paginación, constraint único) |
| UI de reviews en frontend | No existe |

### Tareas

#### Frontend — Componentes
- [ ] `StarRating.vue` — props: `modelValue`, `readonly`, `size` (`sm/md/lg`); hover progresivo con tooltip ("Malo"…"Excelente"); estrellas parciales en modo readonly
- [ ] `RatingDistribution.vue` — resumen: número grande + estrellas + "X valoraciones"; barras horizontales 5★→1★ con `q-linear-progress` color `$warning`
- [ ] `ReviewForm.vue` — visible solo si: inscrito + sin review propia + ha completado ≥1 lección; textarea mín 20 chars; contador de caracteres; botón deshabilitado hasta tener rating + texto mínimo
- [ ] `ReviewItem.vue` — avatar (iniciales si no hay foto) + nombre + fecha relativa + estrellas + texto; si es la propia → badge "Tu valoración" + botones editar/eliminar
- [ ] `ReviewList.vue` — "Cargar más" en vez de paginación; tu review primero; ordenar por más recientes / mejor / peor
- [ ] `UserAvatar.vue` — componente reutilizable: foto o iniciales con color generado a partir del nombre
- [ ] Completar `CourseTabReviews.vue` con todos los componentes anteriores

#### UX/UI
- [ ] Click en estrella: animación de escala (1.0 → 1.2 → 1.0)
- [ ] Edición de review propia in-place (el item se convierte en formulario, sin modal)
- [ ] Toast `q-notify` "Valoración enviada ✓" tras enviar

---

## 10. `feature/assessments` 🆕 PENDIENTE

**Prioridad:** Media
**Dependencias:** `feature/course-sections-lessons`, `feature/student-progress`

### Descripción
Sistema de evaluación con cuestionarios autocorregidos y entregas de proyectos. Límite de intentos y tiempo. Panel de corrección para instructores.

### Tareas

#### Backend — Entidades nuevas
- [ ] `AssessmentEntity`: id, courseId, sectionId (nullable), title, description, type (`quiz/project`), maxAttempts, timeLimitMinutes (nullable), passingScore, isPublished, position, createdAt
- [ ] `QuizQuestionEntity`: id, assessmentId, questionText, position, points
- [ ] `QuizOptionEntity`: id, questionId, optionText, isCorrect, position
- [ ] `AssessmentAttemptEntity`: id, assessmentId, userId, startedAt, submittedAt, score, passed, attemptNumber
- [ ] `QuizAnswerEntity`: id, attemptId, questionId, selectedOptionId
- [ ] `ProjectSubmissionEntity`: id, attemptId, fileUrl, fileName, fileSize, instructorFeedback, gradedAt, gradedBy
- [ ] Migraciones Flyway para todas las tablas

#### Backend — Endpoints
- [ ] `GET /api/courses/{courseId}/assessments` — listar evaluaciones del curso
- [ ] `GET /api/assessments/{id}` — detalle de evaluación (sin `isCorrect` en opciones)
- [ ] `POST /api/assessments/{id}/start` — iniciar intento (valida límite de intentos); devuelve preguntas con opciones sin marcar la correcta
- [ ] `POST /api/assessments/{id}/submit` — enviar respuestas (autocorrige quiz) o guardar archivo de proyecto
- [ ] `GET /api/assessments/{id}/attempts` — historial de intentos del usuario
- [ ] `GET /api/assessments/{id}/results/{attemptId}` — resultado con respuestas correctas y explicaciones
- [ ] CRUD de evaluaciones y preguntas (instructor): `POST/PUT/DELETE /api/instructor/assessments`
- [ ] `GET /api/instructor/assessments/{id}/submissions` — entregas pendientes de corrección
- [ ] `PATCH /api/instructor/submissions/{id}/grade` — calificar proyecto con puntuación y feedback

#### Backend — Lógica
- [ ] Autocorrección de quizzes al enviar — calcular puntuación y `passed`
- [ ] Validar tiempo límite: `startedAt + timeLimitMinutes < now` → rechazar envío tardío
- [ ] Validar intentos máximos antes de iniciar
- [ ] Las respuestas correctas (`isCorrect`) nunca se incluyen en `GET /assessments/{id}` — solo en resultados post-envío

#### Frontend — Componentes
- [ ] `AssessmentIntro.vue` — card con: título, descripción, nº preguntas, tiempo límite, intentos restantes, puntuación mínima; botón "Comenzar" con dialog de confirmación
- [ ] `QuizView.vue` — layout limpio sin header/sidebar durante el quiz
- [ ] `QuizNavPanel.vue` — fila de números de pregunta coloreados por estado (sin responder / respondida / actual / marcada para revisar)
- [ ] `QuizTimer.vue` — barra superior que se reduce + MM:SS; colores: `$primary` → `$warning` (5 min) → `$negative` parpadeante (1 min)
- [ ] `QuizQuestion.vue` — pregunta + opciones con `q-option-group`; opción seleccionada con fondo `$primary` al 10%
- [ ] `QuizSubmitSummary.vue` — resumen antes de enviar: respondidas/sin responder/marcadas; dialog de confirmación si hay sin responder
- [ ] `AssessmentResults.vue` — puntuación con counter-up animado, aprobado/suspenso, lista de preguntas con corrección; confeti CSS si aprueba
- [ ] `ProjectUpload.vue` — instrucciones markdown + zona drag & drop + barra de progreso de subida + estado de la entrega
- [ ] `InstructorSubmissionList.vue` — lista de entregas pendientes con nombre, fecha, archivo descargable
- [ ] `InstructorGrading.vue` — puntuación numérica + textarea feedback + botones "Aprobar" / "Suspender"

#### UX/UI
- [ ] Timeout → auto-envío con dialog "Se ha acabado el tiempo. Tu examen ha sido enviado automáticamente."
- [ ] Botón "Marcar para revisar" (flag) en cada pregunta para revisión posterior
- [ ] Preview de PDF/imagen del archivo entregado en el panel del instructor

---

## 11. `feature/payments-stripe` 🆕 PENDIENTE

**Prioridad:** Alta
**Dependencias:** `feature/enrollment-flow`

### Descripción
Integración completa con Stripe: pago único por curso, suscripciones mensuales/anuales y cupones de descuento. Al completar el pago se crea el enrollment automáticamente.

### Configuración inicial — Stripe test mode

Pasos para tener Stripe funcionando desde cero, antes de escribir ningún código.

#### 1. Crear cuenta y obtener claves test
1. Registrarse en [dashboard.stripe.com](https://dashboard.stripe.com)
2. Activar **modo test** (toggle en el dashboard, esquina superior derecha)
3. Ir a *Developers → API keys* y copiar:
   - `pk_test_...` — Publishable key (va al frontend, puede ser pública)
   - `sk_test_...` — Secret key (va al backend, **nunca** al frontend ni al repositorio)

#### 2. Backend — Añadir dependencia al `pom.xml`
```xml
<dependency>
    <groupId>com.stripe</groupId>
    <artifactId>stripe-java</artifactId>
    <version>26.3.0</version>
</dependency>
```

#### 3. Backend — Variables de entorno (sin valores por defecto)
```properties
# application.properties — sin fallbacks; la app falla al arrancar si falta alguna
stripe.secret-key=${STRIPE_SECRET_KEY}
stripe.public-key=${STRIPE_PUBLIC_KEY}
stripe.webhook-secret=${STRIPE_WEBHOOK_SECRET}
stripe.monthly-price-id=${STRIPE_MONTHLY_PRICE_ID}
stripe.annual-price-id=${STRIPE_ANNUAL_PRICE_ID}
```
Crear `.env.local` (en `.gitignore`) o exportar en el shell:
```bash
export STRIPE_SECRET_KEY=sk_test_...
export STRIPE_PUBLIC_KEY=pk_test_...
export STRIPE_WEBHOOK_SECRET=whsec_...   # se obtiene en el paso 5
```

#### 4. Backend — `StripeConfig.java`
```java
@Configuration
public class StripeConfig {
    @Value("${stripe.secret-key}")
    private String secretKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = secretKey;
    }
}
```

#### 5. Webhooks locales con Stripe CLI
Necesario para probar el flujo completo (el evento `checkout.session.completed` que crea el enrollment).
```bash
# Instalar en Fedora
sudo dnf install stripe
# o descargar el binario de github.com/stripe/stripe-cli/releases

# Autenticarse (abre el navegador)
stripe login

# Reenviar webhooks al backend local → imprime el webhook secret local
stripe listen --forward-to localhost:8080/api/payments/webhook
# → Copiar el  whsec_... que imprime y usarlo como STRIPE_WEBHOOK_SECRET
```

#### 6. Frontend — Variable de entorno
```bash
# CourselyLabs-front/.env.local
VITE_STRIPE_PUBLIC_KEY=pk_test_xxxxx
```
> Con Stripe Checkout (redirect), el frontend solo redirige a la URL devuelta por el backend. No necesita el SDK de Stripe.js.

#### 7. Flujo mínimo end-to-end para verificar que funciona
```
1. "Comprar curso"  →  POST /api/payments/checkout  →  recibe { url }
2. Frontend redirige a url (página hosted de Stripe)
3. Pagar con tarjeta de prueba (ver paso 8)
4. Stripe → webhook checkout.session.completed → backend crea enrollment
5. Stripe redirige a success_url (/pago/exito?session_id=...)
```

#### 8. Tarjetas de prueba
| Escenario | Número |
|---|---|
| Pago correcto | `4242 4242 4242 4242` |
| Requiere autenticación 3DS | `4000 0025 0000 3155` |
| Tarjeta rechazada | `4000 0000 0000 9995` |

> Fecha: cualquier futura · CVV: cualquier 3 dígitos · CP: cualquier 5 dígitos

---

### Tareas

#### Backend — Dependencias
- [ ] Añadir `stripe-java` SDK al `pom.xml`

#### Backend — Entidades nuevas
- [ ] `PaymentEntity`: id, userId, courseId (nullable), type (`one_time/subscription`), stripePaymentIntentId, stripeSessionId, amount, currency, status (`pending/completed/failed/refunded`), createdAt
- [ ] `SubscriptionEntity`: id, userId, stripeSubscriptionId, stripeCustomerId, plan (`monthly/annual`), status (`active/cancelled/past_due`), currentPeriodStart, currentPeriodEnd, cancelledAt, createdAt
- [ ] `CouponEntity`: id, code (unique), discountType (`percentage/fixed`), discountValue, maxUses, currentUses, expiresAt, isActive, applicableTo (`all/specific_courses`), createdAt
- [ ] `CouponCourseEntity`: id, couponId, courseId
- [ ] Migraciones Flyway

#### Backend — Endpoints
- [ ] `POST /api/payments/checkout` — crear sesión Stripe Checkout para compra de curso; acepta `couponCode` opcional
- [ ] `POST /api/payments/subscribe` — crear suscripción mensual/anual
- [ ] `POST /api/payments/webhook` — receptor de webhooks Stripe; verificación de firma `STRIPE_WEBHOOK_SECRET`; idempotencia por `stripeSessionId`
- [ ] `GET /api/payments/history` — historial de pagos del usuario autenticado
- [ ] `POST /api/payments/cancel-subscription` — cancelar suscripción (cancela al final del período)
- [ ] `GET /api/payments/subscription` — estado actual de suscripción del usuario
- [ ] `POST /api/coupons/validate` — validar código: activo, no expirado, usos < maxUsos, aplicable al curso
- [ ] CRUD de cupones (admin): `GET/POST/PUT/DELETE /api/admin/coupons`

#### Backend — Lógica
- [ ] Webhook `checkout.session.completed` → crear enrollment automáticamente + marcar pago como `completed`
- [ ] Webhook `invoice.paid` → renovar suscripción, extender `currentPeriodEnd`
- [ ] Webhook `customer.subscription.updated/deleted` → actualizar estado de suscripción
- [ ] Suscriptor activo → acceso a todos los cursos de pago (verificar en `EnrollmentService`)

#### Backend — Variables de entorno
- [ ] `STRIPE_SECRET_KEY`, `STRIPE_PUBLIC_KEY`, `STRIPE_WEBHOOK_SECRET`
- [ ] `STRIPE_MONTHLY_PRICE_ID`, `STRIPE_ANNUAL_PRICE_ID`

#### Frontend — Componentes
- [ ] `CheckoutButton.vue` — reemplaza `EnrollButton` para cursos de pago; incluye badge "Pago seguro 🔒" + logos Visa/MC
- [ ] `CouponInput.vue` — link "¿Tienes un cupón?" que expande input; al aplicar: spinner → check verde "−20%" o X roja "No válido"
- [ ] `SubscriptionPlans.vue` — pricing table con toggle mensual/anual, plan recomendado destacado, comparativa de features
- [ ] `PricingToggle.vue` — toggle mensual/anual con badge "Ahorra 20%"
- [ ] `PaymentSuccessView.vue` — checkmark animado + resumen + botón "Ir al curso"
- [ ] `PaymentCancelledView.vue` — icono info + "Pago cancelado" + botón volver
- [ ] `PaymentHistory.vue` — tabla fecha/concepto/importe/estado + botón descargar factura
- [ ] `SubscriptionManager.vue` — plan actual, próxima fecha cobro, método pago, cancelar suscripción

#### Frontend — Configuración
- [ ] Añadir `VITE_STRIPE_PUBLIC_KEY` a `.env`
- [ ] Rutas: `/suscripcion`, `/pago/exito?session_id=`, `/pago/cancelado`, `/perfil/pagos`, `/perfil/suscripcion`

#### UX/UI
- [ ] Precio en el sidebar del curso: fuente Monda 28px; con cupón → precio original tachado + descuento badge
- [ ] Suscriptor activo → badge "Incluido en tu suscripción" con icono de corona en lugar del precio

---

## 12. `feature/course-creation` ✅ COMPLETADO

**Prioridad:** Alta
**Dependencias:** `feature/course-sections-lessons`, `feature/student-progress`

### Descripcion
Flujo completo para que cualquier usuario pueda crear y gestionar sus propios cursos. Incluye ownership, limites por rol, dashboard de instructor y verificacion admin. Gestion de instructores movida a rama aparte.

### Auditoria del estado actual

#### Lo que YA existe en backend (endpoints listos, sin frontend)

| Elemento | Estado | Problema |
|---|---|---|
| `POST /api/courses` | Existe | Cualquier autenticado puede crear, sin ownership |
| `PUT /api/courses/{id}` | Existe | Cualquier autenticado puede editar CUALQUIER curso |
| `DELETE /api/courses/{id}` | Existe | Cualquier autenticado puede borrar CUALQUIER curso |
| `PATCH /api/courses/{id}/publish` | Existe | Sin verificacion admin previa |
| `PATCH /api/courses/{id}/submit-review` | Existe | Solo cambia status, no hay aprobacion real |
| `PATCH /api/courses/{id}/unpublish` | Existe | Sin check de ownership |
| CRUD de secciones | Existe | Sin check de ownership del curso |
| CRUD de lecciones | Existe | Sin check de ownership del curso |
| Reorder secciones/lecciones | Existe | Sin check de ownership |
| `CourseInstructorEntity` + Repository | Existe | Tiene metodos completos pero NO hay controller |
| Busqueda avanzada (filtros, sort) | Existe | No se usa desde frontend |

#### Lo que FALTA (critico)

| Elemento | Por que es necesario |
|---|---|
| Campo `created_by` en `courses` | Sin esto no se sabe quien creo un curso. No se puede validar permisos de edicion ni contar cursos por usuario |
| Limite de cursos por rol | user=2, premium=10, admin=ilimitado. Sin esto cualquiera crea infinitos cursos |
| Validacion de ownership en endpoints | Ahora mismo el usuario A puede editar/borrar cursos del usuario B. Grave problema de seguridad |
| Controller de gestion de instructores | El repository existe con metodos completos (add, remove, findMain, etc.) pero no hay endpoints. No se pueden añadir co-instructores |
| Verificacion admin antes de publicar | `submitForReview()` cambia el status a `pending_review` pero no hay endpoint de aprobacion/rechazo admin. Los cursos se pueden publicar directamente sin revision |
| Upload de archivos | `thumbnailUrl` es un string que se rellena a mano. No hay endpoint para subir imagenes ni videos |

#### Codigo muerto (existe pero no se usa)

| Elemento | Detalle |
|---|---|
| Endpoints CRUD de secciones | Backend completo, frontend no tiene editor |
| Endpoints CRUD de lecciones | Backend completo, frontend no tiene editor |
| `CourseInstructorRepository` | 6 metodos (findByCourseId, findMainInstructor, exists, delete, etc.) que nadie llama |
| `submitForReview()` en CourseService | Cambia status pero no hay flujo de aprobacion |
| Flujo de estados (draft/pending_review/published) | 3 endpoints PATCH que nadie usa desde frontend |

### Estado actual

| Elemento | Estado |
|---|---|
| Flyway V5 — `created_by` en courses | Hecho — migra cursos existentes al instructor principal o admin |
| `CourseEntity.createdBy` | Hecho — ManyToOne a UserEntity, NOT NULL |
| `CourseDTO.createdById` | Hecho — incluido en mapper |
| `CourseService.create()` con ownership | Hecho — asigna `createdBy` + auto-crea `course_instructors` con `is_main=true` |
| Limites por rol (user=2, premium=10, admin=∞) | Hecho — `countByCreatedById` + `BadRequestException` |
| `GET /api/courses/mine` | Hecho — devuelve cursos del usuario autenticado |
| `GET /api/courses/mine/limits` | Hecho — `currentCourses` + `maxCourses` |
| `CourseSecurityService` (bean `@PreAuthorize`) | Hecho — `isOwnerOrInstructor`, `isOwnerOrAdmin`, `isOwnerOrInstructorOrAdmin`, `canEditSection`, `canEditLesson` |
| `@PreAuthorize` en CourseController | Hecho — update/publish/unpublish/submitForReview (owner+instructor+admin), delete (owner+admin) |
| `@PreAuthorize` en SectionController | Hecho — create (courseId), update/delete (resuelve via section→course) |
| `@PreAuthorize` en LessonController | Hecho — create (sectionId), update/delete (resuelve via lesson→section→course) |
| SecurityConfig — rutas publicas | Hecho — GET lessons, courses/mine autenticado |
| `api/instructor.ts` | Hecho — CRUD cursos + limits + submitForReview |
| `api/sectionEditor.ts` | Hecho — CRUD secciones + reorder |
| `api/lessonEditor.ts` | Hecho — CRUD lecciones + reorder |
| `api/course.ts` — getCategories | Hecho |
| `InstructorLayout.vue` | Hecho — sidebar con "Mis cursos" y "Crear curso" |
| Rutas `/instructor/*` | Hecho — cursos, nuevo, editar, contenido |
| `InstructorCourseList.vue` | Hecho — cards con thumbnail/status chip, limites, empty state, delete dialog, submit review |
| `CourseWizard.vue` | Hecho — stepper 4 pasos: info basica, detalles (TipTap rich editor), contenido (link a editor), revision |
| `CourseContentEditor.vue` | Hecho — secciones CRUD inline, lecciones CRUD via dialog (texto/video/pdf), delete confirmation |
| `RichTextEditor.vue` | Hecho — TipTap con toolbar (bold, italic, code, listas, headings, code blocks, blockquote) |
| Enlace "Crear curso" en AppSidebar | Hecho |
| Slug auto-generado | Hecho — desde titulo, normalizado sin acentos |

| Drag & drop secciones/lecciones | Hecho — `vuedraggable` con handles, llama a `reorderSections`/`reorderLessons` |
| Checklist antes de enviar a revision | Hecho — dialog verifica titulo + al menos 1 seccion + al menos 1 leccion |
| `rejectionReason` en CourseEntity/DTO | Hecho — Flyway V6, campo TEXT |
| Banner de rechazo en dashboard | Hecho — banner rojo con motivo + boton "Editar y reenviar" |
| "Volver al sitio" en InstructorLayout | Hecho — enlace en sidebar |

### Pendiente para futuras ramas

- [ ] **Gestion de instructores** (rama aparte): `InstructorManager.vue`, controller de co-instructores, add/remove/change main
- [ ] **Verificacion admin**: approve/reject endpoint (en `feature/admin-dashboard`)
- [ ] **Drag & drop** para reordenar secciones/lecciones (requiere `vuedraggable`)
- [ ] **Banner de rechazo** con motivo + boton "Editar y reenviar"
- [ ] **Checklist de requisitos** antes de enviar a revision

---

## 13. `feature/search-filters` ✅ COMPLETADO

**Prioridad:** Media
**Dependencias:** Ninguna

### Descripcion
Busqueda con filtros, endpoint avanzado y API client en frontend.

### Estado final

| Elemento | Estado |
|---|---|
| `GET /api/courses/search/advanced` | Hecho — keyword, categoryId, level, isFree, minRating, sortBy, page, size |
| API `courseSearch.ts` | Hecho — `searchCourses()` con tipado |
| `CoursesView.vue` — busqueda y filtros | Hecho |

### Pendiente para futuras iteraciones
- [ ] Debounce 300ms + historial de busquedas en localStorage
- [ ] Panel de filtros lateral (sidebar 240px / drawer en mobile)
- [ ] Chips de filtros activos encima de los resultados
- [ ] Sincronizacion de filtros con URL query params
- [ ] Toggle grid/lista con persistencia en localStorage
- [ ] Infinite scroll (`q-infinite-scroll`)
- [ ] Empty state de "No se encontraron cursos" + boton limpiar filtros

---

## 14. `feature/forums` 🆕 PENDIENTE

**Prioridad:** Media
**Dependencias:** `feature/course-detail-page`

### Descripción
Foro de discusión por curso. Un foro por curso, hilos con respuestas, posibilidad de fijar y archivar hilos.

### Tareas

#### Backend — Entidades nuevas
- [ ] `ForumThreadEntity`: id, courseId, userId, title, content, isPinned, isArchived, createdAt, updatedAt
- [ ] `ForumReplyEntity`: id, threadId, userId, content, createdAt, updatedAt
- [ ] Migraciones Flyway

#### Backend — Endpoints
- [ ] `GET /api/courses/{courseId}/forum` — listar hilos (paginado); hilos fijados siempre primero
- [ ] `POST /api/courses/{courseId}/forum` — crear hilo (solo inscritos o instructor)
- [ ] `GET /api/forum/{threadId}` — hilo con sus respuestas (paginadas)
- [ ] `POST /api/forum/{threadId}/replies` — añadir respuesta
- [ ] `PUT /api/forum/threads/{id}` — editar hilo (solo autor o admin)
- [ ] `DELETE /api/forum/threads/{id}` — eliminar hilo (solo autor o admin)
- [ ] `PUT /api/forum/replies/{id}` — editar respuesta
- [ ] `DELETE /api/forum/replies/{id}` — eliminar respuesta
- [ ] `PATCH /api/forum/threads/{id}/pin` — fijar/desfijar (solo instructor o admin)
- [ ] `PATCH /api/forum/threads/{id}/archive` — archivar/desarchivar (solo instructor o admin)

#### Frontend — Componentes
- [ ] Pestaña "Foro" en `CourseDetailView` o ruta `/cursos/:slug/foro`
- [ ] `ForumThreadList.vue` — hilos fijados arriba (fondo `$primary` 5% opacidad + icono pin), hilos archivados (gris + badge), preview 2 líneas, nº respuestas, última actividad relativa; ordenar por recientes/más respondidos/sin respuesta
- [ ] `ForumThreadView.vue` — post original destacado + respuestas en timeline con línea vertical; editor fijo en la parte inferior
- [ ] `ForumEditor.vue` — `q-editor` con toolbar (bold, italic, código, link, lista); tabs "Editar" / "Vista previa"; botón "Citar" que inserta blockquote
- [ ] `ForumNewThreadDialog.vue` — dialog con título + `ForumEditor` + validación (título ≥ 5 chars, contenido ≥ 10)

#### UX/UI
- [ ] Rol badge en respuestas: Instructor → `$primary`, Estudiante → gris
- [ ] Empty state del foro: "Aún no hay discusiones. ¡Sé el primero!" con ilustración
- [ ] Mobile: editor de respuesta como bottom sheet
- [ ] Botón "Nuevo hilo" con color `$accent`, visible y prominente

---

## 15. `feature/messaging` 🆕 PENDIENTE

**Prioridad:** Media
**Dependencias:** Ninguna

### Descripción
Mensajería privada entre usuarios. Conversaciones bilaterales, mensajes en tiempo real via WebSocket.

### Tareas

#### Backend — Entidades nuevas
- [ ] `ConversationEntity`: id, createdAt
- [ ] `ConversationParticipantEntity`: id, conversationId, userId
- [ ] `MessageEntity`: id, conversationId, senderId, content, isRead, createdAt
- [ ] Constraint: no duplicar conversación entre los mismos dos usuarios
- [ ] Migraciones Flyway

#### Backend — Endpoints REST
- [ ] `GET /api/messages/conversations` — conversaciones del usuario con preview del último mensaje y contador de no leídos
- [ ] `GET /api/messages/conversations/{id}` — mensajes de una conversación (paginado, más recientes al final)
- [ ] `POST /api/messages/conversations` — iniciar conversación con otro usuario (`{ recipientId }`)
- [ ] `POST /api/messages/conversations/{id}` — enviar mensaje
- [ ] `PATCH /api/messages/conversations/{id}/read` — marcar mensajes como leídos

#### Backend — WebSocket (STOMP)
- [ ] `WebSocketConfig.java` — endpoint `/ws`, prefijo `/topic` y `/queue`
- [ ] Canal privado: `/queue/messages/{userId}` para recibir mensajes en tiempo real
- [ ] Al enviar mensaje via REST → publicar en el canal del destinatario

#### Frontend — Componentes
- [ ] Vista `/mensajes` — layout split (desktop): panel izquierdo 320px conversaciones + panel derecho chat
- [ ] Mobile: lista de conversaciones pantalla completa → seleccionar navega al chat
- [ ] `ConversationList.vue` — avatar + nombre + preview último mensaje + fecha relativa; no leídas en bold + dot `$info`
- [ ] `ChatView.vue` — burbujas propias derecha (`$primary`), ajenas izquierda (gris); timestamp en hover; scroll automático al nuevo mensaje; botón "↓ Nuevos mensajes" si el usuario ha hecho scroll arriba
- [ ] `MessageInput.vue` — textarea autoexpandible; Enter envía, Shift+Enter nueva línea
- [ ] Indicador "escribiendo..." con tres puntos animados via WebSocket
- [ ] `q-badge` con número de conversaciones no leídas en `AppHeader.vue`

#### UX/UI
- [ ] Read receipts: doble check azul cuando el mensaje fue leído
- [ ] Online/offline del interlocutor (dot verde/gris en el header del chat)
- [ ] Agrupar mensajes por fecha: separadores "Hoy", "Ayer", "12 marzo"
- [ ] Botón "Enviar mensaje" en el perfil de instructores y desde el foro
- [ ] Empty state: "No tienes mensajes. Inicia una conversación desde el perfil de un instructor o compañero de curso."

---

## 16. `feature/notifications` 🆕 PENDIENTE

**Prioridad:** Media
**Dependencias:** `feature/forums`, `feature/messaging`

### Descripción
Sistema de notificaciones en tiempo real via WebSocket (STOMP). Campana en el header, dropdown y vista completa. Preferencias por tipo.

### Tareas

#### Backend — Dependencias
- [ ] Añadir `spring-boot-starter-websocket` al `pom.xml`

#### Backend — Entidad nueva
- [ ] `NotificationEntity`: id, userId, type (`forum_reply/message/enrollment/course_approved/course_rejected/assessment_graded/new_submission`), title, body, referenceType, referenceId, isRead, createdAt
- [ ] Migración Flyway

#### Backend — WebSocket
- [ ] `WebSocketConfig.java` (si no existe de `feature/messaging`) — configurar STOMP broker
- [ ] Canal privado: `/queue/notifications/{userId}`

#### Backend — Endpoints
- [ ] `GET /api/notifications` — listar notificaciones del usuario (paginado)
- [ ] `GET /api/notifications/unread-count` — contador de no leídas (para polling inicial)
- [ ] `PATCH /api/notifications/{id}/read` — marcar como leída
- [ ] `PATCH /api/notifications/read-all` — marcar todas como leídas
- [ ] `DELETE /api/notifications/{id}` — eliminar notificación

#### Backend — Eventos que disparan notificación
- [ ] Respuesta en hilo de foro del usuario → `forum_reply`
- [ ] Nuevo mensaje directo recibido → `message`
- [ ] Curso aprobado/rechazado → `course_approved` / `course_rejected` (para instructor)
- [ ] Proyecto calificado → `assessment_graded` (para estudiante)
- [ ] Nueva inscripción en curso del instructor → `enrollment`
- [ ] Nueva entrega de proyecto pendiente → `new_submission` (para instructor)

#### Frontend — Componentes
- [ ] `NotificationBell.vue` — icono campana + `q-badge` rojo (máx "9+"); animación bounce al recibir nueva
- [ ] `NotificationDropdown.vue` — 360px, max-height 400px con scroll; header con "Marcar todas como leídas"; footer "Ver todas →"; animación slide-down al abrir
- [ ] `NotificationItem.vue` — icono por tipo + título bold si no leída + texto truncado 2 líneas + tiempo relativo; click navega al recurso + marca leída
- [ ] `NotificationList.vue` — vista completa `/notificaciones` con tabs por tipo + agrupación por fecha ("Hoy", "Ayer", "Esta semana") + "Cargar más"
- [ ] `NotificationPreferences.vue` — toggles por tipo en Perfil → Ajustes; toggle de toast en pantalla
- [ ] Composable `useWebSocket.ts` — conexión STOMP, reconexión automática, suscripción a canal privado
- [ ] Composable `useNotifications.ts` — count, mark read, lista

#### UX/UI
- [ ] Nuevas notificaciones en tiempo real: aparecen en la parte superior del dropdown con slide-in + badge actualizado
- [ ] Toast opcional `q-notify` bottom-right al recibir notificación (configurable en preferencias)
- [ ] Swipe-to-dismiss en mobile en la vista completa

---

## 17. `feature/admin-dashboard` ✅ COMPLETADO

**Prioridad:** Media
**Dependencias:** Ninguna técnica (puede hacerse en paralelo con otras features)

### Descripción
Panel de administración accesible solo para rol `ADMIN`. Gestión de usuarios y moderación de cursos.

### Estado actual

| Elemento | Estado |
|---|---|
| `AdminService` | Hecho — stats, getUsers (paginado), changeRole, ban, unban, getPendingCourses, approveCourse, rejectCourse |
| `AdminController` con `@PreAuthorize("hasRole('ADMIN')")` | Hecho — todos los endpoints bajo proteccion de rol |
| `GET /api/admin/stats` | Hecho — totalUsers, publishedCourses, pendingCourses, totalEnrollments |
| `GET /api/admin/users` | Hecho — paginado con Spring Pageable |
| `PATCH /api/admin/users/{id}/role` | Hecho — valida roles (user/premium/admin) |
| `PATCH /api/admin/users/{id}/ban` + `/unban` | Hecho — no permite banear admins |
| `GET /api/admin/courses/pending` | Hecho — cursos con status `pending_review` |
| `PATCH /api/admin/courses/{id}/approve` | Hecho — auto-publica (status=published, isPublished=true, publishedAt=now) |
| `PATCH /api/admin/courses/{id}/reject` | Hecho — acumula rejectionReason ("1: motivo; 2: motivo") |
| `api/admin.ts` | Hecho — todas las llamadas API |
| `AdminLayout.vue` | Hecho — sidebar con badge de pendientes |
| Rutas `/admin/*` con guard `requiresRole: 'admin'` | Hecho |
| `AdminDashboard.vue` | Hecho — 4 tarjetas de metricas + quick links |
| `AdminCourseQueue.vue` | Hecho — cards con preview, aprobar y rechazar con motivo obligatorio |
| `AdminUserTable.vue` | Hecho — q-table server-side, cambiar rol, ban/unban |
| Enlace "Administracion" en AppSidebar (solo admin) | Hecho |
| Fix duplicado "Mi perfil" en sidebar | Hecho — eliminado de authLinks, queda solo el avatar item |
| Fix pom.xml dependencias fuera de `<dependencies>` | Hecho |
| Modal enviar a revision con aviso publicacion automatica | Hecho |

### Pendiente para futuras iteraciones

- [ ] `ActivityLogEntity` + historial de acciones admin
- [ ] `AdminCategoryManager.vue` — CRUD de categorias (backend ya existe)
- [ ] Reportes de usuarios
- [ ] Eliminar reviews inapropiadas
- [ ] Filtros avanzados en tabla de usuarios (por rol, estado, fecha)

---

## 18. `chore/deployment` 🆕 PENDIENTE

**Prioridad:** Media — para cuando la app esté lista para mostrar
**Dependencias:** Ninguna técnica (puede prepararse en paralelo)

### Descripción
Configuración para desplegar toda la stack en un VPS usando Docker Compose. Un único comando para levantar backend, frontend, base de datos y proxy inverso en producción.

### Stack de despliegue propuesto

| Componente | Tecnología |
|---|---|
| Servidor | VPS Linux — Hetzner CX21 (~5€/mes) o DigitalOcean Droplet |
| Contenedores | Docker + Docker Compose |
| Proxy inverso | Nginx (SSL termination, assets estáticos) |
| SSL | Let's Encrypt con Certbot (gratuito, renovación automática) |
| CI/CD | GitHub Actions (build → push imagen → deploy) |

### Tareas

#### Docker
- [ ] `CourselyLabs-back/Dockerfile` — multi-stage: build con Maven + imagen final `eclipse-temurin:21-jre-alpine`
- [ ] `CourselyLabs-front/Dockerfile` — multi-stage: build con Node + imagen final `nginx:alpine` con los estáticos
- [ ] `docker-compose.prod.yml` en la raíz del monorepo con servicios:
  - `db` (PostgreSQL 15)
  - `redis` (Redis 7)
  - `backend` (Spring Boot)
  - `frontend` (Nginx sirviendo los estáticos del build de Vue)
  - `nginx` (proxy inverso: 80→443, `/api/` → backend, `/` → frontend)
- [ ] `.env.prod.example` — plantilla documentada con todas las variables de producción
- [ ] `docker-compose.override.yml` para desarrollo local (hot-reload, puertos expuestos)

#### Nginx
- [ ] `nginx/nginx.conf` — proxy inverso con SSL, gzip, cache de assets estáticos, headers de seguridad (HSTS, X-Frame-Options, CSP)
- [ ] `nginx/default.conf` — vhosts: `api.courselylabs.com → backend:8080`, `courselylabs.com → frontend:80`

#### GitHub Actions
- [ ] `.github/workflows/deploy.yml` — en push a `main`:
  1. Build y test del backend (`mvn test`)
  2. Build del frontend (`npm run build`)
  3. Build de imágenes Docker y push a GHCR o Docker Hub
  4. SSH al servidor y `docker compose pull && docker compose up -d`
- [ ] Secrets en GitHub: `SERVER_HOST`, `SERVER_USER`, `SERVER_SSH_KEY`, `DOCKER_USERNAME`, `DOCKER_PASSWORD`

#### Guía de primer despliegue
- [ ] `DEPLOYMENT.md` con pasos completos:
  1. Provisionar servidor VPS (Ubuntu 22.04 LTS)
  2. Instalar Docker + Docker Compose
  3. Clonar repositorio
  4. Copiar `.env.prod.example` a `.env.prod` y rellenar variables
  5. `docker compose -f docker-compose.prod.yml up -d`
  6. Configurar DNS apuntando al servidor
  7. Obtener SSL con Certbot: `certbot --nginx -d courselylabs.com`

---

## Orden de merge sugerido

```
develop
 ├── fix/security-improvements        ✅ Completado
 ├── chore/codebase-cleanup           ✅ Completado
 ├── feature/course-detail-page       ✅ Completado
 ├── feature/enrollment-flow          ✅ Completado
 ├── feature/course-sections-lessons  ✅ Completado
 ├── feature/student-progress         ✅ Completado
 ├── feature/search-filters           ✅ Completado
 │
 │   --- Proximas ramas ---
 │
 ├── feature/reviews-frontend         (tras enrollment-flow) ← SUGERIDO SIGUIENTE
 ├── feature/downloadable-resources   (tras course-sections-lessons)
 ├── feature/course-prerequisites     (tras student-progress)
 ├── feature/assessments              (tras student-progress)
 ├── feature/payments-stripe          (tras enrollment-flow)
 ├── feature/course-creation          (tras course-sections-lessons + student-progress) ← ALTA PRIORIDAD
 ├── feature/forums                   (tras course-detail-page)
 ├── feature/messaging                (independiente)
 ├── feature/notifications            (tras forums + messaging)
 ├── feature/admin-dashboard          (independiente)
 └── chore/deployment                 (en cualquier momento)
```

---

## Infraestructura de datos — init_db + seeders ✅

### Descripcion
Sistema de inicializacion de DB para que `docker compose up -d` con volumen limpio deje la DB completamente lista (schema + datos de prueba).

### Estructura de `init_db/`

| Archivo | Contenido |
|---|---|
| `01_schema.sql` | Schema completo (V1+V2+V3): 10 tablas, triggers, vistas, funciones |
| `02_seed_base.sql` | 11 usuarios, 6 categorias, 8 cursos, enrollments, reviews |
| `03_seed_postgresql.sql` | 4 secciones, 10 lecciones para curso PostgreSQL |
| `04_seed_flamenco.sql` | 5 secciones, 14 lecciones, enrollments, reviews, lesson_progress |

### Uso

```bash
# Inicializacion limpia (cualquier maquina)
docker compose down -v && docker compose up -d

# Seeders individuales (DB existente)
./seed.sh              # todos los seeds
./seed.sh flamenco     # solo flamenco
./seed.sh postgresql   # solo postgresql
```

### Usuarios de prueba

| Email | Password | Rol |
|---|---|---|
| admin@cursos.com | admin123 | admin |
| instructor@cursos.com | admin123 | user (instructor) |
| student@cursos.com | admin123 | user (estudiante) |

---

## UX/UI — Patrones transversales

Convenciones de diseño que aplican a **todas** las features. Implementar progresivamente.

### Sistema de feedback
- **Toasts** (`q-notify`): acciones exitosas (inscripción, review, lección completada). Bottom-right, 3s. `$positive` éxito / `$negative` error / `$info` info.
- **Dialogs de confirmación**: acciones destructivas o irreversibles. Botón en `$negative` con el verbo específico ("Eliminar", no "Aceptar").
- **Inline validation**: errores debajo del input al perder foco (blur), no en tiempo real.
- **Loading states**: siempre `q-skeleton`, nunca `q-spinner` para contenido que carga. Skeletons que imiten la forma del contenido real.
- **Empty states**: ilustración SVG + mensaje descriptivo + CTA. Nunca página vacía.
- **Error states**: `q-banner` con icono + mensaje + botón "Reintentar" cuando falla la API.

### Navegación
- **Breadcrumbs** (`q-breadcrumbs`): en vistas de profundidad > 1 (detalle de curso, lección, foro, admin).
- **Active link**: item de sidebar/header activo con borde izquierdo `$primary` + fondo sutil.
- **Transiciones entre rutas**: `fade` 200ms via `<router-view v-slot>` + `<transition>`.
- **Scroll to top**: botón flotante "↑" visible tras scroll > 300px en páginas largas.

### Responsive
- **Breakpoints**: mobile (< 600px), tablet (600–1023px), desktop (≥ 1024px). Clases Quasar (`col-12 col-sm-6 col-md-4`).
- **Sidebars**: visibles en desktop, `q-drawer` en tablet/mobile.
- **Tablas**: modo `grid` de `q-table` en mobile (cada fila como card).
- **Touch**: mínimo 44×44px en elementos interactivos.

### Tipografía y colores
- **Títulos**: fuente Monda (h1–h3). **UI/body**: fuente Archivo.
- **Colores semánticos**: `$primary` (#0F766E) → navegación/acciones principales. `$accent` (#EA580C) → CTAs/destacados.
- **Chips de estado**: Borrador → gris, En revisión → `$warning`, Publicado → `$positive`, Rechazado → `$negative`.

### Accesibilidad
- Contraste WCAG AA en todos los textos (ver DESIGN.md).
- `focus-visible` outline en todos los elementos interactivos.
- `aria-label` en iconos sin texto y botones de solo icono.
