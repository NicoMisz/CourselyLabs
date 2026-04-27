# Changelog

Todos los cambios relevantes del proyecto se documentan en este archivo.

El formato se basa en [Keep a Changelog](https://keepachangelog.com/es/1.1.0/).

---

## [Sin version] - 2026-04-27 — Refactor multi-bloque + dashboard de calificacion

### Agregado

- **src/api/lessonBlock.ts**: Cliente CRUD + reorder de bloques de leccion (`/api/lessons/:id/blocks`, `/api/blocks/:id`, `/api/lessons/:id/blocks/reorder`).
- **src/api/grading.ts**: Cliente para `/api/grading/pending` con tipo `PendingSubmission` (incluye `courseId/courseTitle/lessonTitle/blockId/assessmentType`).
- **src/types/lesson.ts**: Tipo `LessonBlock` y `BlockType`. `Lesson` ahora incluye `blocks?: LessonBlock[]`.
- **src/views/instructor/GradingDashboard.vue**: Vista global de entregas pendientes con dropdown de filtro por curso, dialog de calificacion y descarga de archivos.
- **src/layouts/InstructorLayout.vue**: Item "Calificar" en sidebar con icono `assignment_turned_in`.
- **src/router/routes.ts**: Ruta `/instructor/calificar`.

### Cambiado

- **src/views/instructor/CourseWizard.vue**: Panel de leccion rediseñado — eliminado el selector de tipo unico; ahora es una **lista drag-and-drop de bloques** (text/video/pdf/quiz/project/open_text) con boton `+ Añadir bloque`. Cada bloque renderiza su editor (RichTextEditor, FileUploader o AssessmentEditor) y se guarda independientemente.
- **src/views/LessonView.vue**: Itera `lesson.blocks` en orden; mantiene fallback legacy para lecciones sin bloques migradas.
- **src/components/AssessmentEditor.vue**: Recibe `blockId` (no `lessonId`). Crea evaluacion via `POST /api/blocks/:blockId/assessment`. Emite evento `created` para sincronizar con el padre.
- **src/components/AssessmentLessonView.vue**: Recibe `blockId`. Carga evaluacion via `GET /api/blocks/:blockId/assessment`.
- **src/api/assessment.ts**: Endpoints reescritos a la API basada en bloque (`getAssessmentByBlock`, `getAssessmentForEditByBlock`, `createAssessmentForBlock`).
- **src/types/assessment.ts**: `Assessment` ahora incluye `blockId?`.

### Backend (CourselyLabs-back)

- **Flyway V11 `lesson_blocks.sql`**: Tabla `lesson_blocks`, columna `block_id` en `assessments` (1:1 unique), migracion de datos (cada leccion existente → 1 bloque), `lesson.type` y `assessments.lesson_id` ahora nullable.
- **Entidades**: Nueva `LessonBlockEntity`. `AssessmentEntity` cambia `lesson` de `@OneToOne` a `@ManyToOne` y añade `block` (`@OneToOne`).
- **Endpoints nuevos**: `GET/POST /api/lessons/:id/blocks`, `PUT/DELETE /api/blocks/:id`, `PATCH /api/lessons/:id/blocks/reorder`, `GET /api/grading/pending`, `GET/POST /api/blocks/:blockId/assessment`.
- **`CourseSecurityService`**: `canEditBlock` y `canAccessBlock`.
- **`LessonMapper`**: Hidrata `blocks[]` en cada leccion.

---

## [Sin version] - 2026-03-19

### Agregado

- **src/components/EmptyState.vue**: Componente reutilizable para estados vacios — icono + titulo + descripcion + CTA

### Corregido

- **src/views/HomeView.vue**: Seccion "Continuar aprendiendo" usa `CourseCardEnrolled` + tipo `EnrolledCourse` + API `getMyCourses` (antes usaba tipo `Course` incorrecto)
- **src/views/HomeView.vue**: Migrados iconos Line Awesome a Material Icons (`school`, `co_present`) — Line Awesome no estaba importado
- **src/layouts/AppSidebar.vue**: Migrados iconos Line Awesome a Material Icons (`home`, `school`, `shopping_cart`)
- **src/layouts/AppSidebar.vue**: Modo mini muestra tooltips con nombre del link y oculta header "Navegacion"
- **src/types/course.ts**: `CourseDetail.free` cambiado de opcional a requerido (backend siempre lo envia)
- **src/views/MyCoursesView.vue**: Descomentado import de `EmptyState`

### Eliminado

- **src/components/EnrollButton.vue**: Eliminado — logica duplicada ya integrada en `CourseSidebar.vue`

---

## [Sin version] - 2026-03-03

### Agregado

- **src/types/auth.ts**: Interfaces TypeScript `User`, `LoginRequest`, `RegisterRequest`, `AuthResponse`
- **src/api/axios.ts**: Instancia de Axios con interceptor de request (inyecta `Authorization: Bearer`) e interceptor de response (refresco automático de token en 401, logout y redirect si falla)
- **src/stores/auth.ts**: Store de Pinia con estado `accessToken`, `refreshToken`, `user`, computeds `isLoggedIn` y `userRole`, y acciones `login()`, `register()`, `logout()`. Persiste sesión en `localStorage`.

### Modificado

- **formLogin.vue**: Conectado a `authStore.login()`. Campo de usuario cambiado a email. Muestra errores de credenciales y de conexión. Redirige a `/` tras login exitoso.
- **formRegister.vue**: Simplificado para coincidir con la API (firstName, lastName, email, password, bio). Eliminados campos sin soporte en backend (birthday, age, city, phone). Conectado a `authStore.register()`.
- **AppHeader.vue**: Muestra nombre del usuario y botones de perfil/logout cuando está autenticado. Muestra botón de login cuando no lo está.
- **ProfileView.vue**: Implementada vista de perfil con nombre, email, rol (con badge) y bio.
- **routes.ts**: Añadida ruta `/profile` con `meta: { requiresAuth: true }`.
- **router/index.ts**: Añadido `router.beforeEach` que redirige a `/login` si la ruta requiere auth y el usuario no está autenticado.
- **main.ts**: Registrado el plugin `Notify` de Quasar para habilitar notificaciones globales.

---

## [Sin version] - 2026-03-02

### Agregado

- **AppHeader.vue**: Componente de header extraido de MainLayout, con prop `showMenuButton` para controlar visibilidad del boton hamburguesa
- **AppSidebar.vue**: Componente de sidebar/drawer extraido de MainLayout, usa `EssentialLinks.vue` para los links de navegacion, soporta v-model y estado mini
- **AppFooter.vue**: Componente de footer extraido de MainLayout
- **AltLayout.vue**: Layout alternativo sin sidebar, pensado para paginas como login, registro, etc.

### Modificado

- **MainLayout.vue**: Refactorizado para componer AppHeader, AppSidebar y AppFooter en vez de tener todo inline
- **routes.ts**: Ejemplo comentado de como usar AltLayout para futuras rutas de autenticacion

---

## [Sin version] - 2026-02-10

### Agregado

- **DESIGN.md**: Guia de diseno con paleta de colores (teal/naranja/grises), tipografia, especificaciones del logo y reglas de accesibilidad WCAG AA
- **CHANGELOG.md**: Archivo de registro de cambios del proyecto
- **README-QUASAR.md**: Documentacion completa de personalizacion de Quasar separada del README principal (plugins, iconos, modo oscuro, componentes, configuracion de Vite)
- **Fuentes personalizadas**: Configuracion de Archivo (variable font, 100-900, normal + italic) y Monda (variable font, 400-700) en `quasar-variables.sass`

### Modificado

- **quasar-variables.sass**: Nueva paleta de colores (primary teal `#0F766E`, accent naranja `#EA580C`, escala de grises), declaraciones `@font-face` para Archivo y Monda, variable `$typography-font-family` con Archivo como fuente principal
- **README.md**: Seccion de componentes Quasar reemplazada por referencia a README-QUASAR.md, agregada seccion "Quasar Framework" con quick reference
