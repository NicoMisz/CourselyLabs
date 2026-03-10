# Plan de ramas — próximas features

Cada sección es una rama independiente desde `develop`. Se indica el orden sugerido de implementación.

---

## UX/UI — Patrones transversales

Convenciones de diseño que aplican a **todas** las features. Implementar progresivamente conforme se desarrollan las ramas.

### Sistema de feedback
- **Toasts** (`q-notify`): siempre para acciones exitosas (inscripción, review enviada, lección completada). Posición: bottom-right. Duración: 3s. Color: `$positive` para éxito, `$negative` para error, `$info` para info.
- **Dialogs de confirmación** (`q-dialog`): para acciones destructivas o irreversibles (eliminar, desmatricularse, enviar examen). Botón de acción en rojo con el verbo específico ("Eliminar", no "Aceptar").
- **Inline validation**: errores de formulario debajo del input con color `$negative` + icono de warning. Mostrar al perder foco (blur), no en tiempo real.
- **Loading states**: siempre `q-skeleton` (no `q-spinner`) para contenido que carga. Skeletons que imiten la forma del contenido real.
- **Empty states**: ilustración SVG minimalista + mensaje descriptivo + CTA principal. Nunca dejar una página vacía sin explicación.
- **Error states**: `q-banner` con icono + mensaje + botón "Reintentar" cuando falla una carga de API.

### Navegación
- **Breadcrumbs** (`q-breadcrumbs`): en todas las vistas de profundidad > 1 (detalle de curso, lección, foro, admin). Siempre con link al nivel superior.
- **Scroll to top**: botón flotante "↑" que aparece al hacer scroll > 300px en páginas largas.
- **Transiciones entre rutas**: `fade` de 200ms via `<router-view v-slot>` + `<transition>`.
- **Active link**: el item de sidebar/header que corresponde a la ruta actual debe tener borde izquierdo `$primary` + fondo sutil.

### Responsive
- **Breakpoints**: mobile (< 600px), tablet (600-1023px), desktop (≥ 1024px). Usar clases de Quasar (`col-12 col-sm-6 col-md-4`).
- **Sidebar**: visible en desktop, `q-drawer` con toggle en tablet/mobile.
- **Tablas**: en mobile se transforman en cards verticales (usar `grid` mode de `q-table`).
- **Touch**: soportar swipe en drawers y carruseles. Botones touch-friendly mínimo 44×44px.

### Tipografía y colores
- **Títulos**: fuente Monda (h1-h3). **Body/UI**: fuente Archivo.
- **Colores semánticos**: `$primary` (#0F766E) para navegación y acciones principales. `$accent` (#EA580C) para CTAs y destacados. Escala de grises para texto secundario.
- **Chips/badges de estado**: siempre con color consistente. Borrador → gris, En revisión → `$warning`, Publicado → `$positive`, Rechazado → `$negative`.

### Accesibilidad
- **Contraste**: todos los textos cumplen WCAG AA (ya definido en DESIGN.md).
- **Focus visible**: outline visible en todos los elementos interactivos al navegar con teclado.
- **aria-labels**: en iconos sin texto y botones de solo icono.
- **Roles ARIA**: en componentes custom (tabs, dialogs, dropdowns).

---

## 1. `fix/security-improvements`

**Objetivo**: Cerrar las vulnerabilidades de seguridad pendientes.

**Tareas**:
- Añadir handler de `BadCredentialsException` en `GlobalExceptionHandler` → devolver 401
- Añadir handler de `AuthenticationException` genérico → 401
- Securizar `POST /api/users` con `@PreAuthorize("hasRole('ADMIN')")` o eliminarlo
- Eliminar `CorsConfig.java` (vacío, CORS ya está en `SecurityConfig`)
- Limpiar `findByRoleAndIsActiveTrue()` si no se usa

**UX/UI — Mensajes de error**:
- Mensajes de error claros y en castellano para errores de auth: "Email o contraseña incorrectos" (no "Bad credentials")
- Diferenciar visualmente entre errores de campo (inline bajo el input) y errores generales (q-banner en la parte superior del formulario)
- Añadir `q-banner` con icono de advertencia y color `$negative` para errores 401/403
- En formLogin: shake animation en el formulario cuando falla el login (feedback háptico visual)
- Rate limiting visible: si el usuario falla 5 intentos, mostrar mensaje "Demasiados intentos, espera X segundos" con countdown

**Archivos afectados**:
- `GlobalExceptionHandler.java`
- `SecurityConfig.java`
- `UserController.java`
- `CorsConfig.java`
- `UserRepository.java`
- `formLogin.vue`

---

## 2. `feature/course-detail-page`

**Objetivo**: Página individual de un curso con toda su información.

**Backend**:
- (ya existe) `GET /api/courses/{id}` y `GET /api/courses/slug/{slug}`
- Añadir endpoint `GET /api/courses/{id}/instructors` si no se expone ya
- Añadir DTO de respuesta enriquecido con instructores, media de reviews, total de estudiantes

**Frontend — `/cursos/:slug`**:
- **Hero section**: thumbnail, título, descripción corta, nivel, precio/gratis, botón de inscripción
- **Pestañas (q-tabs)**:
  - **Descripción**: descripción completa, prerequisitos, lo que aprenderás
  - **Contenido**: árbol de secciones y lecciones (colapsable) — placeholder hasta que existan secciones
  - **Instructores**: tarjetas con foto, nombre, bio
  - **Valoraciones**: listado de reviews + media + formulario para dejar review
- **Sidebar lateral**: resumen (precio, nivel, duración, nº estudiantes), botón de inscripción sticky
- **Tipo**: `Course` actualizado en `types/course.ts` con todos los campos

**UX/UI**:
- **Skeleton loading**: placeholder animado con `q-skeleton` para hero (thumbnail rectangular + líneas de texto), tabs y sidebar mientras carga la API. No usar spinner.
- **Breadcrumb**: `Home > Cursos > [Categoría] > [Título del curso]` con `q-breadcrumbs`, cada nivel clickable
- **Badge de nivel**: chip con colores semánticos (Principiante → `$positive`, Intermedio → `$warning`, Avanzado → `$negative`)
- **Badge gratis/precio**: si gratis → chip verde "Gratis"; si de pago → precio tachado con descuento + precio final en grande
- **Thumbnail fallback**: si no hay imagen, mostrar placeholder con gradiente `$primary` → `$accent` y el icono de la categoría centrado
- **Sidebar sticky**: `position: sticky; top: 80px` para que siga al scroll en desktop. En mobile (< 768px) la sidebar se colapsa bajo el hero como sección normal
- **Scroll suave a tabs**: al hacer click en una pestaña, `scrollIntoView({ behavior: 'smooth' })` si la sección de tabs no es visible
- **Meta tags OG**: `<meta property="og:title/description/image">` dinámicos para que al compartir el link en redes sociales se vea el thumbnail y título
- **Última actualización**: mostrar fecha relativa ("Actualizado hace 3 días") bajo el título con `q-icon` de reloj
- **Contador de estudiantes**: icono de personas + número formateado ("1.2K estudiantes" si > 1000)
- **Responsive hero**: en mobile el thumbnail ocupa ancho completo, título debajo; en desktop layout lado a lado
- **404 de curso**: si el slug no existe, mostrar página "Curso no encontrado" con botón "Explorar cursos" en vez de error genérico

**Componentes nuevos**:
- `CourseHero.vue`
- `CourseTabDescription.vue`
- `CourseTabContent.vue`
- `CourseTabInstructors.vue`
- `CourseTabReviews.vue`
- `CourseSidebar.vue`
- `CourseBreadcrumb.vue`
- `LevelBadge.vue` (reutilizable)
- `PriceBadge.vue` (reutilizable)

**Ruta**: añadir `{ path: '/cursos/:slug', component: CourseDetailView }` en `routes.ts`

---

## 3. `feature/enrollment-flow`

**Objetivo**: Permitir que un usuario se inscriba en un curso (gratis o de pago).

**Backend**:
- (ya existe) `POST /api/enrollments`, `GET /api/enrollments/check`
- Añadir validación: no permitir doble inscripción (ya hay constraint unique)
- Endpoint `GET /api/enrollments/user/{userId}/courses` para obtener cursos inscritos con datos del curso

**Frontend**:
- Botón "Inscribirme" en la página de detalle del curso
  - Si el curso es gratis → inscripción directa
  - Si es de pago → redirigir a checkout (futuro) o mostrar mensaje
- Indicador visual si el usuario ya está inscrito
- Vista "Mis cursos" (`/mis-cursos`) con listado de cursos inscritos y progreso
- Conectar `CoursesView.vue` al axios configurado (con token)

**UX/UI**:
- **Botón de inscripción con estados**:
  - No logueado → "Inicia sesión para inscribirte" (outline, redirige a login con `redirect` query param)
  - Logueado, no inscrito → "Inscribirme gratis" (`$positive`, icono de check) o "Comprar curso — 29,99€" (`$accent`)
  - Inscribiéndose → botón deshabilitado con `q-spinner` inline
  - Ya inscrito → "Continuar curso" (`$primary`, icono de play) + "Inscrito ✓" como chip
- **Feedback de inscripción exitosa**: dialog de confirmación con animación de checkmark (Lottie o CSS), mensaje "¡Te has inscrito!" y botón "Ir al curso"
- **"Mis cursos" — empty state**: si no hay cursos inscritos, mostrar ilustración SVG + "Aún no te has inscrito en ningún curso" + botón "Explorar cursos" con `$accent`
- **"Mis cursos" — cards**:
  - Thumbnail + barra de progreso (`q-linear-progress`, color `$primary`) superpuesta en la parte inferior de la imagen
  - Título, último acceso como tiempo relativo ("Hace 2 horas"), porcentaje completado
  - Hover: elevación sutil (`shadow-4` → `shadow-8`) + ligero scale (1.02)
  - Badge de estado: "Nuevo" (si progreso = 0%), "En curso" (1-99%), "Completado" (100% con icono de trofeo)
- **Ordenar/filtrar "Mis cursos"**: tabs "Todos", "En curso", "Completados" + ordenar por último acceso/nombre/progreso
- **"Continuar donde lo dejé"**: en la home, si el usuario tiene cursos activos, mostrar sección destacada con el último curso accedido y botón "Continuar"
- **Responsive**: en mobile las cards de "Mis cursos" ocupan ancho completo en columna; en desktop grid de 3 columnas

**Componentes nuevos**:
- `EnrollButton.vue`
- `EnrollSuccessDialog.vue`
- `MyCoursesView.vue`
- `CourseCardEnrolled.vue` (con último acceso y progreso)
- `EmptyState.vue` (reutilizable: ilustración + mensaje + CTA)

---

## 4. `feature/course-sections-lessons`

**Objetivo**: Estructura interna del curso — secciones y lecciones.

**Backend — Entidades nuevas**:
- `SectionEntity`: id, courseId, title, description, position (orden), createdAt
- `LessonEntity`: id, sectionId, title, description, type (video/text/pdf/audio), contentUrl, duration, position, isFree (preview), createdAt

**Backend — Endpoints**:
- `GET /api/courses/{courseId}/sections` — listar secciones con lecciones
- `POST /api/courses/{courseId}/sections` — crear sección
- `PUT /api/sections/{id}` — actualizar sección
- `DELETE /api/sections/{id}` — eliminar sección
- `PATCH /api/sections/reorder` — reordenar secciones
- `POST /api/sections/{sectionId}/lessons` — crear lección
- `PUT /api/lessons/{id}` — actualizar lección
- `DELETE /api/lessons/{id}` — eliminar lección
- `PATCH /api/lessons/reorder` — reordenar lecciones

**Frontend**:
- Pestaña "Contenido" en detalle de curso: árbol colapsable de secciones/lecciones
- Vista de lección (`/cursos/:slug/leccion/:lessonId`)
- Sidebar de navegación del curso (lista de secciones/lecciones con estado completado)
- Reproductor de vídeo (Video.js) para lecciones tipo video
- Visor de texto/markdown para lecciones tipo texto
- Visor de PDF embebido para lecciones tipo pdf

**UX/UI**:
- **Árbol de contenido** (pestaña en detalle de curso):
  - `q-expansion-item` por sección con transición suave
  - Cada lección: icono por tipo (video → `sym_o_play_circle`, texto → `sym_o_article`, PDF → `sym_o_picture_as_pdf`, audio → `sym_o_headphones`)
  - Duración al lado del título ("12:34" para vídeos, "5 min lectura" para texto)
  - Lecciones gratuitas: badge "Preview" clickable que abre la lección sin inscripción
  - Lecciones bloqueadas (no inscrito): icono de candado + opacidad reducida
  - Contador por sección: "3/8 lecciones" con mini barra de progreso
- **Vista de lección — layout**:
  - Desktop: sidebar izquierda (250px, colapsable con botón hamburguesa) + contenido principal
  - Mobile: sidebar como `q-drawer` que se abre con swipe o botón
  - Navegación inferior fija: botones "← Anterior" y "Siguiente →" con título de la siguiente lección
  - Breadcrumb condensado: `[Curso] > [Sección] > [Lección]`
- **Reproductor de vídeo**:
  - Controles: play/pause, volumen, velocidad (0.5x, 1x, 1.25x, 1.5x, 2x), fullscreen, picture-in-picture
  - Atajos de teclado: espacio (play/pause), ← → (±10s), ↑ ↓ (volumen), F (fullscreen)
  - Barra de progreso con preview del timestamp al hacer hover
  - Autoplay de la siguiente lección con countdown de 5s ("Siguiente lección en 5... 4... [Cancelar]")
  - Recordar velocidad preferida del usuario en localStorage
- **Visor de texto/markdown**: renderizado con estilos de tipografía del proyecto (Archivo), code blocks con syntax highlighting, imágenes responsive
- **Visor de PDF**: embed nativo con fallback a descarga directa si el navegador no soporta embed
- **Sidebar de navegación del curso**:
  - Sección activa expandida automáticamente, lección activa highlighted con borde izquierdo `$primary`
  - Icono de check verde para lecciones completadas
  - Scroll automático a la lección activa en el sidebar
  - Porcentaje total del curso en la parte superior del sidebar
- **Transiciones**: al cambiar de lección, fade-in del contenido (no recarga completa de página)
- **Loading**: skeleton del contenido principal (rectángulo para vídeo, líneas para texto) mientras carga

**Componentes nuevos**:
- `CourseSectionList.vue`
- `LessonView.vue`
- `LessonVideoPlayer.vue`
- `LessonTextViewer.vue`
- `LessonPdfViewer.vue`
- `CourseNavSidebar.vue`
- `LessonNavBar.vue` (anterior/siguiente)
- `LessonTypeIcon.vue` (reutilizable)

---

## 5. `feature/student-progress`

**Objetivo**: Tracking del progreso del estudiante dentro de un curso.

**Backend — Entidad nueva**:
- `LessonProgressEntity`: id, userId, lessonId, isCompleted, completedAt, lastPosition (para vídeos)

**Backend — Endpoints**:
- `POST /api/progress/lessons/{lessonId}/complete` — marcar lección como completada
- `GET /api/progress/courses/{courseId}` — progreso del curso (% completado, lecciones completadas)
- `PATCH /api/progress/lessons/{lessonId}/position` — guardar posición del vídeo

**Frontend**:
- Barra de progreso en "Mis cursos"
- Indicadores de completado en sidebar de navegación del curso
- Botón "Marcar como completada" en cada lección
- Guardado automático de posición de vídeo

**UX/UI**:
- **Botón "Marcar como completada"**:
  - Posición: debajo del contenido de la lección, ancho completo, prominente
  - Estado inicial: `q-btn` outline con icono de check vacío + "Marcar como completada"
  - Al hacer click: animación de check (fill de izquierda a derecha), cambia a filled verde + "Completada ✓"
  - Click de nuevo para desmarcar (toggle con confirmación)
  - Auto-completar: para vídeos, marcar automáticamente cuando se ve el 90%+
- **Barra de progreso del curso**:
  - `q-linear-progress` con gradiente (`$primary` → `$positive` al acercarse al 100%)
  - Texto superpuesto centrado: "12 de 30 lecciones (40%)"
  - Animación suave al incrementar (`transition: width 0.5s ease`)
- **Progreso circular en "Mis cursos"**: `q-circular-progress` en la esquina de cada card, tamaño pequeño (40px), con porcentaje dentro
- **Celebración al completar curso**:
  - Dialog modal con animación de confeti (particles CSS, no librería pesada)
  - Mensaje "¡Has completado [nombre del curso]!" con icono de trofeo
  - Botones: "Dejar valoración" (→ tab de reviews) + "Explorar más cursos"
- **"Continuar donde lo dejé"** en sidebar de navegación: la primera lección no completada tiene un indicador pulsante (dot con animación `pulse`)
- **Guardado de posición de vídeo**: guardar cada 10 segundos via `PATCH`, al reabrir mostrar `q-banner` "Continuar desde 12:34?" con botón "Sí" / "Empezar de nuevo"

---

## 6. `feature/instructor-dashboard`

**Objetivo**: Panel para que los instructores creen y gestionen sus cursos.

**Frontend — `/instructor`**:
- **Lista de mis cursos**: cursos creados por el instructor con estado (borrador, en revisión, publicado)
- **Editor de curso**: formulario para crear/editar curso (título, descripción, categoría, nivel, precio, thumbnail)
- **Editor de contenido**: gestión de secciones y lecciones (drag & drop para reordenar)
- **Subida de archivos**: upload de vídeos, PDFs, imágenes
- **Estadísticas básicas**: nº de estudiantes, media de valoraciones por curso

**UX/UI**:
- **Creación de curso — wizard por pasos** (`q-stepper`):
  1. Información básica (título, descripción corta, categoría, nivel)
  2. Detalles (descripción completa, thumbnail, prerequisitos)
  3. Precio (gratis/pago, monto, moneda)
  4. Contenido (secciones y lecciones — se puede completar después)
  5. Revisión y publicar
  - Cada paso se valida antes de avanzar; se puede navegar entre pasos completados
  - Auto-guardado como borrador cada 30s con indicador "Guardado ✓" sutil en la esquina
  - Slug auto-generado a partir del título (editable manualmente)
- **Lista de mis cursos**:
  - Cards con thumbnail, título, estado (chip con color: borrador → gris, en revisión → `$warning`, publicado → `$positive`, rechazado → `$negative`)
  - Métricas inline: estudiantes, rating medio, ingresos
  - Menú contextual (tres puntos): editar, duplicar, archivar, eliminar
  - Empty state si no tiene cursos: "Crea tu primer curso" con ilustración
- **Editor de contenido**:
  - Drag & drop para reordenar secciones y lecciones (usar `vuedraggable` / Sortable.js)
  - Handle de arrastre visible (icono de 6 puntos) al lado izquierdo
  - Edición inline del título: click para editar, Enter para confirmar, Esc para cancelar
  - Botón "+" para añadir sección/lección con animación de expansión
  - Confirmación antes de eliminar con nombre del item ("¿Eliminar la sección 'Introducción'?")
- **Upload de archivos**:
  - Zona de drag & drop con borde discontinuo y texto "Arrastra archivos aquí o haz click"
  - Barra de progreso por archivo con porcentaje y velocidad de subida
  - Preview del thumbnail antes de confirmar (crop/resize con aspect ratio 16:9)
  - Validación de formato y tamaño con mensaje claro ("Máximo 500MB para vídeos, 50MB para otros")
  - Para vídeos: indicador de procesamiento post-upload ("Procesando vídeo...")
- **Vista previa**: botón "Vista previa" que abre el curso tal como lo vería un estudiante, en nueva pestaña
- **Estadísticas**: mini gráficas sparkline de inscripciones últimos 30 días por curso

**Componentes nuevos**:
- `InstructorLayout.vue`
- `InstructorCourseList.vue`
- `CourseWizard.vue` (stepper)
- `CourseEditor.vue`
- `SectionEditor.vue`
- `LessonEditor.vue`
- `DragHandle.vue`
- `FileUploader.vue`
- `ThumbnailCropper.vue`
- `InstructorStats.vue`

---

## 7. `feature/reviews-frontend`

**Objetivo**: Sistema de valoraciones visible en la página del curso.

**Backend**: ya implementado (`ReviewController`)

**Frontend**:
- Pestaña "Valoraciones" en detalle de curso
- Componente de estrellas (1-5)
- Formulario para dejar review (solo si está inscrito y no ha dejado una)
- Listado de reviews con paginación
- Media de valoraciones con distribución (5★: 60%, 4★: 25%, etc.)

**UX/UI**:
- **Componente de estrellas**:
  - Hover: las estrellas se llenan progresivamente con color `$warning` (#D97706) + tooltip con texto ("Malo", "Regular", "Bueno", "Muy bueno", "Excelente")
  - Click: transición de escala (1.0 → 1.2 → 1.0) en la estrella seleccionada
  - Display (solo lectura): estrellas parciales (ej. 4.3 → 4 llenas + 1 al 30%) con el número al lado
  - Tamaños: `sm` (16px, para listados), `md` (24px, para cards), `lg` (32px, para formulario)
- **Resumen de valoraciones** (parte superior de la pestaña):
  - Lado izquierdo: número grande (ej. "4.3") + estrellas + "basado en 128 valoraciones"
  - Lado derecho: distribución con barras horizontales (5★ ████████ 64%, 4★ ████ 21%, ...) usando `q-linear-progress` con color `$warning`
- **Formulario de review**:
  - Solo visible si: inscrito + no ha dejado review + ha completado al menos 1 lección
  - Estrellas seleccionables (obligatorio) + textarea con placeholder "¿Qué te pareció este curso?" (mín 20 caracteres)
  - Contador de caracteres en la esquina inferior derecha del textarea
  - Botón enviar deshabilitado hasta que haya rating + texto mínimo
  - Feedback: toast `q-notify` "Valoración enviada" con icono de check
- **Lista de reviews**:
  - Cada review: avatar (iniciales si no hay foto) + nombre + fecha relativa + estrellas + texto
  - Ordenar por: más recientes (default), mejor valoradas, peor valoradas
  - "Cargar más" en vez de paginación numérica (más natural para reviews)
  - Tu propia review aparece primero con badge "Tu valoración" y botones editar/eliminar
- **Review propia — edición**: al hacer click en "Editar", el item se transforma in-place en formulario editable (no modal)

**Componentes nuevos**:
- `StarRating.vue` (con props: `modelValue`, `readonly`, `size`)
- `ReviewForm.vue`
- `ReviewList.vue`
- `ReviewItem.vue`
- `RatingDistribution.vue`
- `UserAvatar.vue` (reutilizable: foto o iniciales con color generado del nombre)

---

## 8. `feature/search-filters`

**Objetivo**: Búsqueda y filtrado avanzado de cursos.

**Backend**: ya existe `GET /api/courses/search?keyword=`

**Frontend — Mejoras en `/cursos`**:
- Barra de búsqueda con debounce
- Filtros laterales: categoría, nivel, precio (gratis/pago), valoración mínima
- Ordenar por: más recientes, mejor valorados, más populares, precio
- Paginación (infinite scroll o botón "cargar más")

**UX/UI**:
- **Barra de búsqueda**:
  - Input prominente con icono de lupa y placeholder "Buscar cursos..."
  - Debounce de 300ms para no saturar la API
  - Sugerencias mientras escribes (últimas 5 búsquedas del usuario en localStorage + resultados de API)
  - Botón X para limpiar el input
  - Atajo de teclado: `/` para enfocar la búsqueda (como GitHub)
- **Panel de filtros**:
  - Desktop: sidebar izquierda (240px) siempre visible
  - Mobile: botón "Filtros" que abre `q-drawer` desde la derecha con los filtros
  - Categorías: `q-option-group` con checkboxes
  - Nivel: chips seleccionables (Principiante, Intermedio, Avanzado)
  - Precio: toggle "Gratis / Pago / Todos"
  - Valoración: estrellas mínimas clickables ("4★ y más")
  - Botón "Limpiar filtros" que aparece solo cuando hay algún filtro activo
  - Chips resumen activos encima de los resultados: "Categoría: Programación × | Nivel: Principiante × | Limpiar todo"
- **Filtros sincronizados con URL**: los filtros se reflejan en query params (`/cursos?q=vue&cat=prog&nivel=principiante`) para que las búsquedas sean compartibles y el back button funcione
- **Resultados**:
  - Contador: "124 cursos encontrados" (o "Sin resultados para 'xyz'" con sugerencia de cambiar filtros)
  - Toggle grid/lista con `q-btn-toggle` (grid = cards, lista = filas horizontales compactas)
  - Guardar preferencia de vista en localStorage
  - Ordenar: `q-select` con opciones (Relevancia, Más recientes, Mejor valorados, Más populares, Precio ↑, Precio ↓)
- **Paginación**: infinite scroll con `q-infinite-scroll` + indicador "Cargando más cursos..." al llegar al final. Fallback a botón "Cargar más" si falla IntersectionObserver
- **Empty state**: ilustración + "No se encontraron cursos con estos filtros" + botón "Limpiar filtros"
- **Cards de curso mejoradas**: hover con elevación + reveal de descripción corta truncada. Rating, nº estudiantes, nivel badge, precio — todo visible sin hover

---

## 9. `feature/forums`

**Objetivo**: Foro de discusión por curso.

**Backend — Entidades nuevas**:
- `ForumThreadEntity`: id, courseId, userId, title, content, isPinned, isArchived, createdAt
- `ForumReplyEntity`: id, threadId, userId, content, createdAt

**Backend — Endpoints**:
- CRUD de hilos: `GET/POST /api/courses/{courseId}/forum`
- CRUD de respuestas: `GET/POST /api/forum/{threadId}/replies`
- `PATCH /api/forum/{threadId}/pin` — fijar hilo
- `PATCH /api/forum/{threadId}/archive` — archivar hilo

**Frontend**:
- Pestaña "Foro" en detalle de curso (o ruta `/cursos/:slug/foro`)
- Listado de hilos con paginación
- Vista de hilo con respuestas
- Formulario para crear hilo/respuesta
- Indicadores de hilo fijado/archivado

**UX/UI**:
- **Listado de hilos**:
  - Hilos fijados siempre arriba con icono de pin y fondo sutil (`$primary` al 5% opacidad)
  - Hilos archivados: texto gris + badge "Archivado"
  - Cada hilo: avatar del autor + título + preview del contenido (2 líneas truncadas) + nº respuestas + última actividad relativa
  - Ordenar: "Más recientes" / "Más respondidos" / "Sin respuesta"
  - Botón "Nuevo hilo" prominente con `$accent`
- **Vista de hilo**:
  - Post original destacado con fondo sutil
  - Respuestas en lista con línea vertical de conexión a la izquierda (estilo timeline)
  - Cada respuesta: avatar + nombre + rol badge (Instructor → `$primary`, Estudiante → gris) + fecha relativa + contenido
  - Editor de respuesta fijo en la parte inferior de la página con `q-editor` (toolbar: bold, italic, código, link, lista)
  - Botón "Citar" en cada respuesta que inserta el texto en blockquote en el editor
- **Formulario de nuevo hilo**:
  - Título (input) + contenido (`q-editor` con toolbar rica)
  - Preview en tiempo real del markdown renderizado (tab "Editar" / "Vista previa")
  - Botón cancelar + crear con validación (título mín 5 chars, contenido mín 10)
- **Empty state del foro**: "Aún no hay discusiones en este curso. ¡Sé el primero!" con ilustración
- **Mobile**: el listado de hilos usa cards compactas; el editor de respuesta se abre como bottom sheet

---

## 10. `feature/messaging`

**Objetivo**: Mensajería privada entre usuarios.

**Backend — Entidades nuevas**:
- `ConversationEntity`: id, createdAt
- `ConversationParticipantEntity`: id, conversationId, userId
- `MessageEntity`: id, conversationId, senderId, content, isRead, createdAt

**Backend — Endpoints**:
- `GET /api/messages/conversations` — listar conversaciones del usuario
- `GET /api/messages/conversations/{id}` — mensajes de una conversación
- `POST /api/messages/conversations` — iniciar conversación
- `POST /api/messages/conversations/{id}` — enviar mensaje
- WebSocket (STOMP) para mensajes en tiempo real

**Frontend**:
- Vista de mensajes (`/mensajes`)
- Lista de conversaciones
- Chat de conversación
- Indicador de mensajes no leídos en header

**UX/UI**:
- **Layout de mensajes** (desktop): panel izquierdo (320px) con lista de conversaciones + panel derecho con el chat activo (estilo WhatsApp Web/Slack)
- **Layout de mensajes** (mobile): lista de conversaciones a pantalla completa; al seleccionar una, navega al chat con botón "← Volver"
- **Lista de conversaciones**:
  - Avatar del otro usuario + nombre + preview del último mensaje (1 línea truncada) + fecha relativa
  - Conversaciones con mensajes no leídos: nombre en bold + dot azul (`$info`) + fondo sutil
  - Ordenadas por último mensaje (más reciente arriba)
  - Búsqueda de conversaciones por nombre de usuario
- **Chat de conversación**:
  - Header: avatar + nombre + estado online/offline (dot verde/gris)
  - Burbujas de mensaje: propias a la derecha (fondo `$primary` con texto blanco), ajenas a la izquierda (fondo gris claro)
  - Timestamp visible al hacer hover sobre la burbuja (o agrupados por fecha: "Hoy", "Ayer", "12 marzo")
  - Indicador de "escribiendo..." con tres puntos animados cuando el otro usuario está escribiendo (via WebSocket)
  - Read receipts: doble check azul si el mensaje fue leído
  - Input de mensaje: textarea autoexpandible + botón enviar con `$primary`. Enter para enviar, Shift+Enter para nueva línea
  - Scroll automático al nuevo mensaje; si el usuario ha hecho scroll arriba, mostrar botón flotante "↓ Nuevos mensajes"
- **Iniciar conversación**: desde el perfil de un usuario (en instructores, foros) con botón "Enviar mensaje"
- **Badge en header**: `q-badge` con número de conversaciones no leídas sobre el icono de mensaje en `AppHeader.vue`
- **Empty state**: "No tienes mensajes. Inicia una conversación con un instructor o compañero de curso."

---

## 11. `feature/admin-dashboard`

**Objetivo**: Panel de administración completo para gestionar la plataforma.

**Backend — Endpoints nuevos**:
- `GET /api/admin/stats` — estadísticas globales (total usuarios, cursos, inscripciones, ingresos)
- `GET /api/admin/users` — listado de usuarios con filtros (rol, estado, fecha de registro)
- `PATCH /api/admin/users/{id}/role` — cambiar rol de un usuario (user/premium/admin/instructor)
- `PATCH /api/admin/users/{id}/ban` — banear/desbanear usuario
- `GET /api/admin/courses/pending` — cursos pendientes de revisión
- `PATCH /api/admin/courses/{id}/approve` — aprobar curso para publicación
- `PATCH /api/admin/courses/{id}/reject` — rechazar curso (con motivo)
- `GET /api/admin/reports` — reportes de contenido/usuarios
- `DELETE /api/admin/reviews/{id}` — eliminar reviews inapropiadas
- `GET /api/admin/activity-log` — historial de actividad de la plataforma

**Backend — Entidades nuevas**:
- `ActivityLogEntity`: id, actorId, action, targetType, targetId, details (JSON), createdAt
- `CourseReviewRequestEntity`: id, courseId, reviewerId (admin), status (pending/approved/rejected), rejectionReason, reviewedAt, createdAt
- `UserReportEntity`: id, reporterId, reportedUserId, reason, status (open/resolved/dismissed), createdAt

**Backend — Seguridad**:
- Todos los endpoints bajo `@PreAuthorize("hasRole('ADMIN')")`
- Auditoría: registrar cada acción administrativa en `ActivityLogEntity`

**Frontend — `/admin`**:
- **Dashboard principal**: tarjetas con métricas clave (usuarios activos, cursos publicados, inscripciones del mes, ingresos)
- **Gestión de usuarios** (`/admin/usuarios`):
  - Tabla con búsqueda, filtros por rol/estado, paginación
  - Acciones: ver perfil, cambiar rol, verificar, banear/desbanear, eliminar
  - Modal de confirmación para acciones destructivas
- **Moderación de cursos** (`/admin/cursos`):
  - Cola de cursos pendientes de revisión
  - Vista previa del curso antes de aprobar
  - Botones aprobar/rechazar con campo de motivo
  - Listado de todos los cursos con filtros por estado
- **Moderación de contenido** (`/admin/reportes`):
  - Listado de reportes de usuarios/contenido
  - Acciones: resolver, descartar, banear usuario
  - Reviews reportadas con opción de eliminar
- **Gestión de categorías** (`/admin/categorias`):
  - CRUD de categorías (ya existe en backend)
  - Reordenar categorías
- **Historial de actividad** (`/admin/actividad`):
  - Log de acciones administrativas con filtros por tipo/fecha/admin
- **Gestión de cupones** (`/admin/cupones`) — placeholder para integración futura con Stripe:
  - Crear/editar/desactivar cupones de descuento
  - Códigos promocionales con fecha de expiración y límite de usos

**UX/UI**:
- **Dashboard — tarjetas de métricas**:
  - 4 tarjetas en fila: Usuarios totales, Cursos publicados, Inscripciones (mes), Ingresos (mes)
  - Cada tarjeta: icono grande + número animado (counter-up al cargar) + variación respecto al mes anterior ("↑ 12%" en verde o "↓ 3%" en rojo)
  - Mini gráfica sparkline debajo de cada número (últimos 7 días)
  - Responsive: 2x2 grid en tablet, columna en mobile
- **Gestión de usuarios — tabla**:
  - `q-table` con server-side pagination y sorting
  - Columnas: avatar+nombre, email, rol (chip con color por rol), estado (activo/baneado), fecha registro, acciones
  - Búsqueda global con debounce en la parte superior
  - Filtros rápidos como tabs: "Todos", "Activos", "Baneados", "Pendientes de verificar"
  - Acciones en cada fila: menú de 3 puntos con opciones contextuales (el menú cambia según el estado)
  - Acciones destructivas (banear, eliminar): `ConfirmActionDialog` con el nombre del usuario y la acción en rojo. Input de motivo obligatorio para baneos.
  - Selección múltiple con checkboxes para acciones en lote (verificar varios, cambiar rol)
- **Moderación de cursos — cola**:
  - Cards en lista vertical con: thumbnail, título, instructor, fecha de envío, tiempo en cola
  - Orden por antigüedad (los más viejos arriba)
  - Badge de prioridad si lleva más de 48h en cola (rojo "Urgente")
  - Al hacer click: vista previa completa del curso (reutilizar CourseDetailView en modo solo lectura)
  - Botones en la parte inferior de la preview: "Aprobar" (`$positive`) + "Rechazar" (abre textarea de motivo, `$negative`)
  - Feedback: toast de confirmación + el curso desaparece de la cola con animación slide-out
- **Historial de actividad**:
  - Timeline vertical (`q-timeline`) con iconos por tipo de acción
  - Filtros: tipo de acción (dropdown), fecha (rango con `q-date`), admin (dropdown)
  - Cada entrada: icono + "[Admin] realizó [acción] sobre [target]" + timestamp
  - Click para expandir detalles (JSON del cambio)
- **Sidebar admin**: siempre visible en desktop con iconos + texto, colapsable a solo iconos. Badge con contador en "Cursos pendientes" y "Reportes abiertos"
- **Responsive**: todas las tablas cambian a formato card-list en mobile (cada fila se convierte en una card con labels)

**Componentes nuevos**:
- `AdminLayout.vue` — layout con sidebar de navegación admin
- `AdminDashboard.vue` — vista principal con métricas
- `AdminMetricCard.vue` — tarjeta de métrica reutilizable (número + variación + sparkline)
- `AdminUserTable.vue` — tabla de usuarios con acciones
- `AdminCourseQueue.vue` — cola de moderación de cursos
- `AdminCoursePreview.vue` — vista previa para revisión
- `AdminReportList.vue` — listado de reportes
- `AdminCategoryManager.vue` — gestión de categorías
- `AdminActivityLog.vue` — historial de actividad
- `ConfirmActionDialog.vue` — modal reutilizable de confirmación

**Router**:
- Ruta `/admin` con `meta: { requiresAuth: true, requiresRole: 'admin' }`
- Guard adicional que verifica `userRole === 'admin'` y redirige si no lo es

---

## 12. `feature/payments-stripe`

**Objetivo**: Sistema completo de pagos con Stripe — pago único por curso, suscripciones mensuales y cupones de descuento.

**Backend — Dependencias**:
- `stripe-java` SDK

**Backend — Entidades nuevas**:
- `PaymentEntity`: id, userId, courseId (nullable), type (one_time/subscription), stripePaymentIntentId, stripeSessionId, amount, currency, status (pending/completed/failed/refunded), createdAt
- `SubscriptionEntity`: id, userId, stripeSubscriptionId, stripeCustomerId, plan (monthly/annual), status (active/cancelled/past_due), currentPeriodStart, currentPeriodEnd, cancelledAt, createdAt
- `CouponEntity`: id, code (unique), discountType (percentage/fixed), discountValue, maxUses, currentUses, expiresAt, isActive, applicableTo (all/specific_courses), createdAt
- `CouponCourseEntity`: id, couponId, courseId — relación para cupones de cursos específicos

**Backend — Endpoints**:
- `POST /api/payments/checkout` — crear sesión de Stripe Checkout para compra de curso
- `POST /api/payments/subscribe` — crear suscripción mensual/anual
- `POST /api/payments/webhook` — receptor de webhooks de Stripe (checkout.session.completed, invoice.paid, customer.subscription.updated, etc.)
- `GET /api/payments/history` — historial de pagos del usuario
- `POST /api/payments/cancel-subscription` — cancelar suscripción
- `GET /api/payments/subscription` — estado actual de la suscripción del usuario
- `POST /api/coupons/validate` — validar código de cupón
- CRUD de cupones (admin): `GET/POST/PUT/DELETE /api/admin/coupons`

**Backend — Lógica de negocio**:
- Al completar pago → crear enrollment automáticamente
- Suscripción activa → acceso a todos los cursos de pago
- Webhook handler robusto (idempotencia, verificación de firma)
- Aplicación de cupones: validar expiración, límite de usos, cursos aplicables

**Frontend**:
- **Botón de compra** en página de detalle del curso (reemplaza "Inscribirme" para cursos de pago)
- **Campo de cupón**: input para código de descuento con validación en tiempo real
- **Página de suscripción** (`/suscripcion`): planes disponibles, comparativa, botón de suscribirse
- **Historial de pagos** (`/perfil/pagos`): tabla con transacciones
- **Gestión de suscripción** (`/perfil/suscripcion`): estado, próxima factura, botón de cancelar
- Redirección a Stripe Checkout y manejo de retorno (success/cancel URLs)

**UX/UI**:
- **Precio en detalle del curso**:
  - Precio grande y claro: "29,99 €" en `$dark`, fuente Monda, tamaño 28px
  - Con cupón aplicado: precio original tachado en gris + precio con descuento en `$accent` + badge "−20%"
  - Suscriptor: badge "Incluido en tu suscripción" con icono de corona
- **Campo de cupón**:
  - Input colapsable (link "¿Tienes un cupón?" que expande el input)
  - Al escribir el código y hacer click en "Aplicar": spinner inline → si válido: check verde + "Cupón aplicado: -20%" + precio actualizado; si inválido: X rojo + "Cupón no válido o expirado"
- **Página de suscripción** — pricing table:
  - 2-3 columnas (Mensual, Anual, o Free/Premium/Pro)
  - Plan recomendado con borde `$accent` y badge "Más popular"
  - Comparativa de features con checks/crosses por plan
  - Toggle mensual/anual con ahorro ("Ahorra 20% con el plan anual")
  - Botones "Empezar" que redirigen a Stripe Checkout
- **Página de retorno** (`/pago/exito` y `/pago/cancelado`):
  - Éxito: checkmark animado + "¡Pago completado!" + resumen (curso/plan, importe) + botón "Ir al curso" / "Explorar cursos"
  - Cancelado: icono de info + "El pago fue cancelado" + "Puedes intentarlo de nuevo" + botón volver al curso
- **Historial de pagos**: tabla con columnas fecha, concepto, importe, estado (chip: completado verde, pendiente amarillo, fallido rojo), botón de descarga de factura (PDF)
- **Gestión de suscripción**:
  - Card con: plan actual, precio, próxima fecha de cobro, método de pago (últimos 4 dígitos)
  - Botón "Cancelar suscripción" con dialog de confirmación que explica qué pierde y hasta cuándo tiene acceso
  - Si está cancelada: banner "Tu suscripción finaliza el [fecha]. Renueva para mantener tu acceso."
- **Iconos de seguridad**: badge "Pago seguro" con icono de candado + logos de tarjetas (Visa, MC) junto al botón de compra

**Componentes nuevos**:
- `CheckoutButton.vue`
- `CouponInput.vue`
- `SubscriptionPlans.vue`
- `PricingToggle.vue` (mensual/anual)
- `PaymentHistory.vue`
- `PaymentSuccessView.vue`
- `PaymentCancelledView.vue`
- `SubscriptionManager.vue`
- `SecurePaymentBadge.vue`

**Variables de entorno**:
- `STRIPE_SECRET_KEY`, `STRIPE_PUBLIC_KEY`, `STRIPE_WEBHOOK_SECRET`
- `VITE_STRIPE_PUBLIC_KEY` (frontend)

---

## 13. `feature/assessments`

**Objetivo**: Sistema de evaluación con cuestionarios de respuesta múltiple y entrega de proyectos. Límite de intentos y tiempo.

**Backend — Entidades nuevas**:
- `AssessmentEntity`: id, courseId, sectionId (nullable), title, description, type (quiz/project), maxAttempts, timeLimitMinutes (nullable), passingScore, isPublished, position, createdAt
- `QuizQuestionEntity`: id, assessmentId, questionText, position, points
- `QuizOptionEntity`: id, questionId, optionText, isCorrect, position
- `AssessmentAttemptEntity`: id, assessmentId, userId, startedAt, submittedAt, score, passed, attemptNumber
- `QuizAnswerEntity`: id, attemptId, questionId, selectedOptionId
- `ProjectSubmissionEntity`: id, attemptId, fileUrl, fileName, fileSize, instructorFeedback, gradedAt, gradedBy

**Backend — Endpoints**:
- `GET /api/courses/{courseId}/assessments` — listar evaluaciones del curso
- `GET /api/assessments/{id}` — detalle de evaluación (sin respuestas correctas)
- `POST /api/assessments/{id}/start` — iniciar intento (valida límite de intentos, devuelve preguntas)
- `POST /api/assessments/{id}/submit` — enviar respuestas (autocorrige quiz, guarda proyecto)
- `GET /api/assessments/{id}/attempts` — intentos del usuario
- `GET /api/assessments/{id}/results/{attemptId}` — resultado de un intento
- CRUD de evaluaciones (instructor): crear/editar/eliminar assessments y preguntas
- `GET /api/instructor/assessments/{id}/submissions` — ver entregas de proyectos pendientes
- `PATCH /api/instructor/submissions/{id}/grade` — calificar proyecto

**Backend — Lógica**:
- Autocorrección de quizzes al enviar
- Validación de tiempo límite (startedAt + timeLimitMinutes < now)
- Validación de intentos máximos
- Las respuestas correctas nunca se envían al frontend hasta después de enviar

**Frontend**:
- **Vista de quiz** (`/cursos/:slug/evaluacion/:id`): preguntas con opciones, temporizador, botón enviar
- **Vista de proyecto** (`/cursos/:slug/proyecto/:id`): instrucciones + upload de archivo
- **Resultados**: puntuación, respuestas correctas/incorrectas, feedback
- **Panel de instructor**: lista de entregas pendientes, formulario de calificación

**UX/UI**:
- **Pantalla de inicio del quiz**:
  - Card centrada con: título, descripción, nº de preguntas, tiempo límite, intentos restantes ("2 de 3 intentos restantes"), puntuación mínima para aprobar
  - Botón "Comenzar" prominente. Al hacer click: dialog de confirmación "Una vez comenzado, el temporizador no se puede pausar. ¿Continuar?"
  - Si no quedan intentos: botón deshabilitado + mensaje "Has agotado tus intentos"
- **Vista de quiz en curso**:
  - Layout limpio: solo la pregunta, opciones y navegación. Sin distracciones (ocultar header y sidebar)
  - **Panel de navegación de preguntas**: fila de números (1, 2, 3...) en la parte superior. Color por estado: gris (sin responder), `$primary` (respondida), `$accent` (actual), `$warning` (marcada para revisar)
  - **Temporizador**: barra superior que se reduce de izquierda a derecha + tiempo restante en formato MM:SS. Cambia de `$primary` → `$warning` (últimos 5 min) → `$negative` (último minuto, con parpadeo)
  - **Cada pregunta**: texto de la pregunta + opciones como `q-radio` o `q-option-group`. Opción seleccionada con fondo sutil `$primary` al 10%
  - Botón "Marcar para revisar" (flag) que permite volver después
  - Navegación: "← Anterior" / "Siguiente →", sin obligar a responder en orden
  - **Antes de enviar**: resumen de preguntas respondidas/sin responder/marcadas. "Tienes 2 preguntas sin responder, ¿enviar de todas formas?"
  - **Timeout**: auto-envío al expirar el tiempo con dialog "Se ha acabado el tiempo. Tu examen ha sido enviado automáticamente."
- **Resultados del quiz**:
  - Puntuación grande con animación counter-up + resultado (Aprobado → check verde y confeti / Suspenso → X roja)
  - Barra visual: porcentaje obtenido vs mínimo requerido
  - Lista de preguntas con: tu respuesta, respuesta correcta (si incorrecta), explicación si la hay
  - Respuestas correctas en verde, incorrectas en rojo con la correcta indicada
  - Botón "Reintentar" (si quedan intentos) + "Volver al curso"
- **Entrega de proyecto**:
  - Instrucciones del proyecto renderizadas en markdown
  - Zona de drag & drop para subir archivo con validación de formato/tamaño
  - Barra de progreso de subida
  - Estado del proyecto: "Pendiente de corrección" (reloj amarillo) / "Calificado" (check verde o X roja)
  - Si calificado: puntuación + feedback del instructor en card destacada
- **Panel del instructor** (corrección):
  - Lista de entregas pendientes con: nombre del estudiante, fecha de entrega, archivo (botón descargar)
  - Formulario de calificación: puntuación numérica + textarea de feedback
  - Preview del archivo entregado si es PDF/imagen
  - Botones: "Aprobar" / "Suspender" que pre-rellenan la puntuación

**Componentes nuevos**:
- `AssessmentIntro.vue` (pantalla de inicio)
- `QuizView.vue`
- `QuizQuestion.vue`
- `QuizNavPanel.vue` (panel de navegación de preguntas)
- `QuizTimer.vue`
- `QuizSubmitSummary.vue` (resumen antes de enviar)
- `ProjectUpload.vue`
- `AssessmentResults.vue`
- `InstructorGrading.vue`
- `InstructorSubmissionList.vue`

---

## 14. `feature/notifications`

**Objetivo**: Sistema de notificaciones en tiempo real via WebSocket (STOMP).

**Backend — Dependencias**:
- `spring-boot-starter-websocket`

**Backend — Entidad nueva**:
- `NotificationEntity`: id, userId, type (forum_reply/message/enrollment/course_approved/assessment_graded), title, body, referenceType, referenceId, isRead, createdAt

**Backend — Configuración**:
- `WebSocketConfig.java`: configurar STOMP broker con endpoint `/ws`, prefijo `/topic` y `/queue`
- Canal privado: `/queue/notifications/{userId}`

**Backend — Endpoints**:
- `GET /api/notifications` — listar notificaciones del usuario (paginado)
- `GET /api/notifications/unread-count` — contador de no leídas
- `PATCH /api/notifications/{id}/read` — marcar como leída
- `PATCH /api/notifications/read-all` — marcar todas como leídas
- `DELETE /api/notifications/{id}` — eliminar notificación

**Backend — Eventos que disparan notificación**:
- Respuesta en un hilo de foro que el usuario sigue
- Nuevo mensaje directo recibido
- Curso aprobado/rechazado (para instructor)
- Proyecto calificado (para estudiante)
- Nueva inscripción en tu curso (para instructor)

**Frontend**:
- **Icono de campana** en `AppHeader.vue` con badge de contador
- **Dropdown de notificaciones**: últimas notificaciones con link al recurso
- **Vista completa** (`/notificaciones`): listado paginado con filtros por tipo
- Conexión WebSocket al montar la app, reconexión automática

**UX/UI**:
- **Icono de campana**:
  - `q-badge` rojo con número (máx "9+") sobre el icono. Animación de bounce sutil al recibir nueva notificación
  - Sin notificaciones: campana normal sin badge
- **Dropdown** (al hacer click en la campana):
  - Ancho 360px, max-height 400px con scroll
  - Header: "Notificaciones" + link "Marcar todas como leídas" (solo si hay no leídas)
  - Cada notificación: icono por tipo (foro → `sym_o_forum`, mensaje → `sym_o_chat`, curso → `sym_o_school`, evaluación → `sym_o_quiz`), título en bold si no leída, texto truncado 2 líneas, tiempo relativo
  - Hover: fondo gris sutil
  - Click: navega al recurso correspondiente + marca como leída
  - Footer: "Ver todas las notificaciones →"
  - Animación de entrada: slide-down + fade-in
- **Notificación nueva en tiempo real**: al recibirla via WebSocket, el item aparece en la parte superior del dropdown con animación slide-in + el badge se actualiza. Opcionalmente `q-notify` toast en la esquina inferior derecha con la notificación (configurable por el usuario)
- **Vista completa** (`/notificaciones`):
  - Tabs de filtro: "Todas", "Foros", "Mensajes", "Cursos", "Evaluaciones"
  - Cada notificación más detallada que en el dropdown: icono + avatar del actor + texto completo + timestamp
  - Botón de eliminar (X) en hover
  - Paginación con "Cargar más" al final
  - Empty state por tipo: "No tienes notificaciones de foros"
- **Agrupación por fecha**: "Hoy", "Ayer", "Esta semana", "Anteriores" como separadores
- **Preferencias de notificaciones** (en perfil → ajustes):
  - Toggles por tipo: activar/desactivar notificaciones de foros, mensajes, cursos
  - Toggle de toast en pantalla (on/off)

**Componentes nuevos**:
- `NotificationBell.vue`
- `NotificationDropdown.vue`
- `NotificationList.vue`
- `NotificationItem.vue`
- `NotificationPreferences.vue`
- Composable `useWebSocket.ts` para gestión de conexión STOMP
- Composable `useNotifications.ts` para lógica de notificaciones (count, mark read, etc.)

---

## 15. `feature/downloadable-resources`

**Objetivo**: Archivos descargables adjuntos a lecciones (PDFs, código fuente, assets).

**Backend — Entidad nueva**:
- `LessonResourceEntity`: id, lessonId, fileName, fileUrl, fileSize, mimeType, downloadCount, createdAt

**Backend — Endpoints**:
- `GET /api/lessons/{lessonId}/resources` — listar recursos de una lección
- `POST /api/lessons/{lessonId}/resources` — subir recurso (multipart, solo instructor)
- `GET /api/resources/{id}/download` — descargar recurso (solo usuarios inscritos)
- `DELETE /api/resources/{id}` — eliminar recurso (solo instructor)

**Backend — Almacenamiento**:
- Subida a disco local (dev) / AWS S3 (prod)
- Validación de tipo y tamaño de archivo (máx 50MB configurable)
- URLs firmadas para descarga segura (S3 signed URLs)

**Frontend**:
- Sección "Recursos" en la vista de lección
- Lista de archivos con icono por tipo, nombre, tamaño
- Botón de descarga
- Upload de recursos en el editor de lección del instructor

**UX/UI**:
- **Sección "Recursos"** en vista de lección:
  - Posición: debajo del contenido principal, antes del botón "Marcar como completada"
  - Card con header "Recursos descargables" + icono de carpeta
  - Lista de archivos con: icono por tipo (PDF → rojo, ZIP → amarillo, código → azul, imagen → verde), nombre del archivo, tamaño legible ("2.3 MB"), botón de descarga con icono
  - Hover en cada recurso: fondo sutil + subrayado del nombre
  - Click en el nombre = descarga directa; para PDFs e imágenes, click abre preview en modal antes de descargar
- **Upload de recursos** (instructor):
  - Zona de drag & drop compacta debajo de la lista existente
  - Múltiples archivos a la vez con barra de progreso individual
  - Botón X para eliminar recurso con confirmación
  - Orden de recursos: drag & drop para reordenar
- **Indicador en el árbol de contenido**: si una lección tiene recursos, mostrar icono pequeño de clip/adjunto al lado del título

**Componentes nuevos**:
- `LessonResources.vue`
- `ResourceUploader.vue`
- `ResourceItem.vue`
- `ResourcePreviewModal.vue` (preview de PDFs/imágenes)

---

## 16. `feature/course-prerequisites`

**Objetivo**: Prerequisitos entre cursos — un curso puede requerir haber completado otro antes de inscribirse.

**Backend — Entidad nueva**:
- `CoursePrerequisiteEntity`: id, courseId, prerequisiteCourseId, createdAt
- Constraint unique en (courseId, prerequisiteCourseId)
- Validación: no permitir prerequisitos circulares

**Backend — Endpoints**:
- `GET /api/courses/{id}/prerequisites` — listar prerequisitos de un curso
- `POST /api/courses/{id}/prerequisites` — añadir prerequisito (solo instructor del curso)
- `DELETE /api/courses/{id}/prerequisites/{prereqId}` — eliminar prerequisito

**Backend — Lógica**:
- Al inscribirse: verificar que el usuario ha completado todos los prerequisitos (progreso 100%)
- Endpoint de enrollment devuelve error detallado con los prerequisitos faltantes

**Frontend**:
- Sección "Prerequisitos" en la pestaña Descripción del curso
- Tarjetas de los cursos prerequisito con link y estado (completado / no completado)
- Mensaje de bloqueo en botón de inscripción si faltan prerequisitos
- Selector de prerequisitos en el editor de curso del instructor

**UX/UI**:
- **Sección "Prerequisitos"** en pestaña Descripción:
  - Header: "Antes de empezar" con icono de cadena
  - Cada prerequisito como mini-card horizontal: thumbnail pequeño + título del curso + nivel badge
  - Estado por usuario: check verde "Completado" / barra de progreso "En curso (60%)" / candado gris "No inscrito"
  - Click en la card → navega al curso prerequisito
  - Si no hay prerequisitos: no mostrar la sección (no mostrar "Sin prerequisitos")
- **Bloqueo de inscripción**:
  - Si faltan prerequisitos: el botón de inscripción cambia a gris deshabilitado + tooltip "Completa los cursos requeridos primero"
  - Banner `q-banner` debajo del botón: "Este curso requiere haber completado: [lista de cursos como links]"
  - Icono de candado en el hero del curso si el usuario no cumple prerequisitos
- **Selector de prerequisitos** (instructor — editor de curso):
  - `q-select` con búsqueda y chips para seleccionar cursos
  - Solo muestra cursos publicados (excluye el propio curso)
  - Cada chip muestra thumbnail mini + título
  - Validación: aviso si se crea una dependencia circular ("El curso X ya requiere este curso")
- **Cadena de prerequisitos**: si un prerequisito tiene a su vez prerequisitos, mostrar aviso "Este curso requiere completar una cadena de N cursos"

**Componentes nuevos**:
- `CoursePrerequisites.vue`
- `PrerequisiteCard.vue`
- `PrerequisiteSelector.vue` (para instructor)
- `PrerequisiteBlockBanner.vue`

---

## Orden recomendado de implementación

```
1.  fix/security-improvements        ← cerrar vulnerabilidades
2.  feature/course-detail-page       ← página central del producto
3.  feature/enrollment-flow          ← inscripciones
4.  feature/course-sections-lessons  ← contenido del curso
5.  feature/downloadable-resources   ← recursos por lección (depende de 4)
6.  feature/student-progress         ← tracking de progreso
7.  feature/course-prerequisites     ← prerequisitos (depende de 6)
8.  feature/reviews-frontend         ← valoraciones
9.  feature/assessments              ← evaluaciones (depende de 4 y 6)
10. feature/payments-stripe          ← pagos (depende de 3)
11. feature/instructor-dashboard     ← creación de cursos
12. feature/search-filters           ← UX de búsqueda
13. feature/forums                   ← interacción social
14. feature/messaging                ← mensajería privada
15. feature/notifications            ← notificaciones (depende de 13 y 14)
16. feature/admin-dashboard          ← panel de administración
```

Cada rama se mergea a `develop` tras revisión. Releases periódicas de `develop` → `main`.
