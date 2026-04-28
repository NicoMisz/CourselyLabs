# Changelog

Todos los cambios relevantes del proyecto se documentan en este archivo. Cubre **frontend**, **backend** e **infraestructura**.

El formato se basa en [Keep a Changelog](https://keepachangelog.com/es/1.1.0/).

> Antes del 2026-04-28 este changelog vivía en `CourselyLabs-front/CHANGELOG.md` y solo cubría el frontend. Se ha elevado a la raíz y se documentan los cambios de toda la pila.

---

## [Sin versión] - 2026-04-28 — Fix: cursos no publicados no deben ser accesibles públicamente

### Corregido

- **`GET /api/courses/all`** devolvía cursos en cualquier estado (`draft`, `pending_review`, `rejected`, `published`). La home (`HomeView.vue` "Cursos destacados") y el composable `useCourses` los pintaban, y al intentar inscribirse el backend rechazaba con "No puedes inscribirte en un curso no publicado". Ahora el endpoint solo devuelve cursos con `is_published = true`.
  - [`CourseService.findAll()`](CourselyLabs-back/src/main/java/com/courselylabs/courselylab/service/CourseService.java) usa ahora `findByIsPublishedTrue()`.
- **`GET /api/courses/{id}` y `GET /api/courses/slug/{slug}`** devolvían el detalle de cualquier curso a cualquier caller, incluso anónimo, aunque estuviera en `draft`. Si alguien compartía el link de un curso sin publicar, era accesible. Ahora se aplica un filtro: si el curso no está publicado, solo lo ven el `owner` (creador), instructores co-asignados (`course_instructors`) y `admin`. Para cualquier otro caller se devuelve **404** (no 403, para no filtrar la existencia del recurso).
  - Helper privado nuevo: [`CourseService.assertCanViewCourse(entity, currentUser)`](CourselyLabs-back/src/main/java/com/courselylabs/courselylab/service/CourseService.java).
  - Aplicado en `findById(id, email)` y `findBySlug(slug, email)`.

### Sin cambios

- Los listados específicos del instructor (`GET /api/courses/instructor/{userId}`) y la cola admin (`GET /api/courses/pending-review`) **no se han tocado** — siguen mostrando todos los estados, que es lo correcto en esos contextos.
- El editor del curso (`GET /api/courses/{id}/edit`) tampoco se ha tocado — su `@PreAuthorize` ya restringe al owner/instructor/admin.

---

## [Sin versión] - 2026-04-28 — Reorganización completa de la documentación

### Cambiado

- **Estructura de `/docs/`**: ahora tiene 7 subcarpetas numéricas (`01-overview`, `02-getting-started`, `03-frontend`, `04-backend`, `05-features`, `06-roadmap`, `07-conventions`) con índice maestro en [`docs/README.md`](docs/README.md).
- **`README.md` raíz** reescrito como puerta de entrada concisa: pitch, quick start de 5 comandos, atajos a docs según rol/objetivo, estructura del repo. Sin duplicación con docs internos.
- **`CLAUDE.md`** simplificado a memorias y preferencias para Claude. Las especificaciones funcionales del producto se movieron a [`docs/01-overview/product-spec.md`](docs/01-overview/product-spec.md).
- **`CHANGELOG.md`** elevado a la raíz para cubrir front + back + infra (antes vivía solo en `CourselyLabs-front/`).

### Movido (con `git mv`, preserva historial)

- `branch-plan.md` → `docs/06-roadmap/branches.md`
- `DOCKER.md` → `docs/02-getting-started/docker.md`
- `servicios.md` → `docs/02-getting-started/services.md`
- `CourselyLabs-front/README.md` → `docs/03-frontend/README.md`
- `CourselyLabs-front/DESIGN.md` → `docs/03-frontend/design-system.md`
- `CourselyLabs-front/README-QUASAR.md` → `docs/03-frontend/quasar.md`
- `CourselyLabs-front/CHANGELOG.md` → `CHANGELOG.md` (raíz)
- `docs/i18n.md` → `docs/03-frontend/i18n.md`
- `docs/state-of-project.md` → `docs/01-overview/state.md`
- `docs/lesson-blocks-refactor.md` → `docs/05-features/lesson-blocks.md`
- `docs/grading-dashboard.md` → `docs/05-features/grading.md`
- `docs/payments-flow.md` → `docs/05-features/premium-stripe.md`
- `docs/downloadable-resources.md` → `docs/04-backend/storage.md`
- `docs/ux-improvements.md` → `docs/06-roadmap/ux-backlog.md`

### Agregado (documentación nueva)

- `docs/README.md` — índice navegable de toda la doc.
- `docs/01-overview/product-spec.md` — especificación funcional consolidada.
- `docs/01-overview/architecture.md` — capas, módulos, flujo de datos.
- `docs/01-overview/data-model.md` — entidades JPA, relaciones, migraciones.
- `docs/02-getting-started/setup.md` — instalación, comandos, troubleshooting.
- `docs/02-getting-started/env-vars.md` — todas las variables de entorno (front + back).
- `docs/02-getting-started/seeders.md` — datos de prueba y usuarios.
- `docs/03-frontend/routing.md` — mapa de rutas, guards, deep-links.
- `docs/03-frontend/state.md` — Pinia, patrones de estado.
- `docs/04-backend/README.md` — layout de paquetes y comandos.
- `docs/04-backend/api.md` — referencia REST completa.
- `docs/04-backend/security.md` — JWT, refresh, `@PreAuthorize`, `CourseSecurityService`.
- `docs/04-backend/migrations.md` — Flyway: cómo añadir migraciones.
- `docs/05-features/auth.md` — flujo de auth completo.
- `docs/05-features/courses-lessons.md` — modelo y flujo de cursos.
- `docs/05-features/assessments.md` — quiz, project, open_text.
- `docs/05-features/prerequisites.md` — prerequisitos entre cursos.
- `docs/05-features/progress.md` — tracking de progreso.
- `docs/05-features/reviews.md` — sistema de valoraciones.
- `docs/07-conventions/git-workflow.md` — ramas, commits, PRs.
- `docs/07-conventions/code-style.md` — estilo backend y frontend.
- `docs/07-conventions/i18n-policy.md` — política de strings castellano.

### Eliminado

- `CourselyLabs-back/HELP.md` — boilerplate de Spring Initializr sin valor.

### Corregido

- **`application.properties`**: `spring.flyway.baseline-versión=0` → `spring.flyway.baseline-version=0` (la clave de configuración había quedado tildada por el sweep ortográfico previo).
- **`router/routes.ts`**: paths `/cursos/:slug/lección/:id` → `/cursos/:slug/leccion/:id` y `/pago/éxito` → `/pago/exito` (las URLs no llevan tildes; el sweep las había tocado al merge).
- **Componentes que generan URLs**: `CourseNavSidebar.vue`, `CourseSectionList.vue`, `LessonNavBar.vue`, `LessonView.vue` — `/lección/` → `/leccion/`.

---

## [Sin versión] - 2026-04-28 — i18n + sweep ortográfico castellano

### Agregado

- **Dependencia**: `vue-i18n@^9.14.5` instalada y registrada en [main.ts](src/main.ts) como plugin global.
- **src/i18n/index.ts**: Configuración del plugin (`legacy: false`, locale por defecto `es`).
- **src/i18n/locales/es.json**: Diccionario completo con ~250 claves organizadas por dominio (`common`, `nav`, `auth`, `home`, `courses`, `course`, `lesson`, `assessment.*`, `instructor.wizard`, `instructor.grading`, `admin`, `premium`, `review`, `notFound`, `footer`). Incluye pluralización (Vue I18n format) en `lecciones`, `intentos`, `valoraciones`, etc.

### Migrado a `t()` (vue-i18n)

- **src/layouts/InstructorLayout.vue**: Sidebar instructor.
- **src/views/instructor/GradingDashboard.vue**: Vista completa de calificación.
- **src/components/AssessmentEditor.vue**: Configuración de evaluación, preguntas, opciones, banners y diálogos.

> El resto del frontend usa strings inline pero con ortografía corregida. Migración progresiva a `t()` queda pendiente como trabajo posterior; la infraestructura está lista.

### Sweep ortográfico (frontend + backend)

Reemplazos automatizados con `sed` aplicando corrección de tildes en strings UI y mensajes de excepción. Cubre ~150 palabras castellanas comunes:

- **Sustantivos `-ción/-sión`**: `evaluación`, `configuración`, `información`, `descripción`, `revisión`, `verificación`, `inscripción`, `corrección`, `administración`, `publicación`, `suscripción`, `decisión`, `calificación`, `creación`, `edición`, `validación`, `explicación`, `sección`, `acción`, `valoración`, `actualización`, `función`, `opción`, `lección`, `versión`, `puntuación`, `duración`, `presentación`, `comunicación`, `motivación`, `documentación`, `televisión`, `visión`, `misión`, `selección`…
- **Adjetivos**: `público/pública`, `máximo/máxima/máximos/máximas`, `mínimo/mínima`, `automático/automática`, `temático/temática`, `académico/académica`, `típico/típica`, `histórico/histórica`, `científico`, `rápido`, `último`, `físico`, `lógico`, `cómodo`, `próximo`…
- **Verbos en futuro/condicional**: `será/serán`, `estará/estarán`, `podrá/podrán/podrás`, `tendrá/tendrán`, `deberá/deberás`, `verá/verás/verán`, `iremos/irá/irán/irás`, `mantendrás`, `harán/haré/haría`, `comenzará/comenzaras`, `necesitarás`, `encontrará`…
- **Adverbios**: `también`, `después`, `aún`, `aquí`, `así`, `además`, `automáticamente`, `rápidamente`, `atrás`…
- **Geo y comunes**: `España`, `América`, `México`, `país/países`, `través`, `catálogo`, `útil`, `fácil`, `difícil`, `índice`, `límite`, `título`, `página`, `categoría`, `botón`, `envío`, `línea`, `época`, `ámbito`, `éxito`, `examen/exámenes`, `ningún`, `algún`, `común`…
- **Demostrativos protegidos**: revertí `está` → `esta` cuando va seguido de sustantivo (`esta lección`, `esta evaluación`, `esta sección`, `esta opción`, etc.) en frontend y backend.
- **Puntuación**: `Sí` con tilde como afirmación, signos `¿…?` añadidos en preguntas, `«…»` en lugar de `"..."` para citas en español.

### Corregido (errores TS preexistentes destapados por `npm run build`)

- **src/components/CourseNavSidebar.vue**, **src/components/CourseSectionList.vue**: `lesson.type` ahora es opcional → fallback `(lesson.type || 'text')`.
- **src/components/FileUploader.vue**: uso de `target.files?.[0]` con guard explícito en lugar de indexado con narrowing.
- **src/components/QuizRunner.vue**: `q-card v-if="currentQuestion"` para guardar contra `undefined`; `isMarked` con guard antes de `markedSet.has()`.
- **src/components/RichTextEditor.vue**: `setContent(val, false)` → `setContent(val, { emitUpdate: false })` (signatura correcta de TipTap v3).
- **src/views/ProfileView.vue**: eliminada la rama `case 'instructor'` del switch (no existe en `User['role']`).

### URLs y identificadores preservados

- **src/router/routes.ts**: path `/cursos/:slug/leccion/:lessonId` se mantiene SIN tilde (las URLs no llevan acentos).
- Imports de paquetes (p. ej. `@tiptap/extension-placeholder`) revertidos a su forma original tras quedar tocados por el sweep.

---

## [Sin version] - 2026-04-28 — Limpieza UX del editor de bloques + deep-link a calificación

### Cambiado

- **src/components/AssessmentEditor.vue**: Eliminada la sección "Entregas pendientes" embebida — ahora vive solo en `/instructor/calificar`. El editor de bloques queda enfocado únicamente en autoría. Se sustituye por un banner informativo con enlace a Calificar.
- **src/components/AssessmentEditor.vue**: Nuevo botón **"Eliminar evaluación"** + diálogo de confirmación. Permite des-configurar un bloque assessment-type sin borrar el bloque entero (útil para deshacer creaciones por error).
- **src/views/instructor/CourseWizard.vue**: Dropdown "Añadir bloque" reorganizado con dos secciones (`Contenido` / `Evaluaciones`) separadas por `q-separator` y headers. Reduce confusión entre `Texto` (contenido pasivo) y `Pregunta abierta` (evaluación). "Respuesta abierta" → "Pregunta abierta"; hints reescritos para diferenciarlos.
- **src/views/instructor/GradingDashboard.vue**: Añadido botón `open_in_new` por entrega que navega a `/instructor/cursos/:id/editar?lesson=...&block=...` (deep-link). Badge "Respuesta abierta" → "Pregunta abierta".

### Agregado

- **src/views/instructor/CourseWizard.vue**: `applyDeepLinkFromQuery()` lee `?lesson=...&block=...` al cargar — selecciona la lección correspondiente y resalta el bloque (scroll smooth + animación CSS `.block-highlight` durante 2.2 s). Cada `block-card` recibe `id="block-:id"` para soportar el scroll.

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
