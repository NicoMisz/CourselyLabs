# Changelog

Todos los cambios relevantes del proyecto se documentan en este archivo. Cubre **frontend**, **backend** e **infraestructura**.

El formato se basa en [Keep a Changelog](https://keepachangelog.com/es/1.1.0/).

> Antes del 2026-04-28 este changelog vivía en `CourselyLabs-front/CHANGELOG.md` y solo cubría el frontend. Se ha elevado a la raíz y se documentan los cambios de toda la pila.

---

## [Sin versión] - 2026-05-05 — Integración con echo lab `feature/echolab-labs`

Nuevo bloque tipo **`lab`** que arranca máquinas virtuales sobre Proxmox vía la API de [echo](https://github.com/...) directamente desde una lección. Cada alumno conecta su cuenta echo personal una vez (token cifrado en BD), y desde el bloque puede iniciar/detener su VM y abrir la consola noVNC sin salir de la plataforma.

### Backend

- **Migración [V13__echo_lab_integration.sql](CourselyLabs-back/src/main/resources/db/migration/V13__echo_lab_integration.sql)**:
  - `users.echo_token_encrypted` (TEXT) y `users.echo_token_updated_at`.
  - Nuevo tipo `lab` en `lesson_blocks.type` + columnas `lab_provider`, `lab_template_id`, `lab_instructions`.
  - Nueva tabla `lab_session_events` con índice por user/block/created_at — log de start/stop/console/error para auditoría.
- **Cifrado at-rest** del token: nuevo [`SecretEncryptor`](CourselyLabs-back/src/main/java/com/courselylabs/courselylab/security/SecretEncryptor.java) (Spring `Encryptors.delux`, AES-256 con PBKDF2). Configurable con `APP_ENCRYPTION_SECRET` y `APP_ENCRYPTION_SALT` (env vars).
- **Cliente HTTP** [`EchoLabClient`](CourselyLabs-back/src/main/java/com/courselylabs/courselylab/integration/echo/EchoLabClient.java) con métodos `me`, `listVms`, `getVmStatus`, `setVmAction`, `listClones`, `getConsoleTicket`. Endpoint base configurable con `ECHO_BASE_URL` (default `http://localhost`).
- **Servicio [`LabService`](CourselyLabs-back/src/main/java/com/courselylabs/courselylab/service/LabService.java)** que orquesta cifrado + cliente echo + log de eventos. El frontend nunca recibe el token; el backend hace de proxy autenticado.
- **Endpoints** (`LabController`):
  - `GET /api/me/echo` — estado de conexión.
  - `PUT /api/me/echo` — guardar token (validado contra `me.php` antes de persistir).
  - `DELETE /api/me/echo` — desconectar.
  - `GET /api/labs/{blockId}/status` — estado del lab (NO_TOKEN / NO_VM / STOPPED / RUNNING / ERROR).
  - `POST /api/labs/{blockId}/start` y `/stop`.
  - `POST /api/labs/{blockId}/console` — ticket VNC corto.

### Cambios en echo (`/home/nmiszczak/echolab/api/`)

Hasta ahora solo `me.php` y `vms.php` aceptaban Bearer token; el resto requería sesión cookie y no era utilizable desde otro servicio. Hemos extendido tres archivos para que acepten **token o sesión** uniformemente, siguiendo el mismo patrón que ya tenían los otros dos:

- `vm_status.php` — start/stop/heartbeat ahora vía API.
- `vm_console.php` — ticket VNC ahora vía API.
- `vm_clones.php` — listar clones de plantilla ahora vía API. **Cambio funcional menor**: el check inicial `role < TEACHER` se ha relajado a "autenticado"; el filtrado fino se mantiene en GET/POST (solo ves/operas tu propio clone).

### Frontend

- Cliente API [`api/lab.ts`](CourselyLabs-front/src/api/lab.ts) con todas las operaciones.
- Componente [`EchoConnectionCard`](CourselyLabs-front/src/components/EchoConnectionCard.vue) en `/profile`: pegas el token, se valida, queda conectado. Botón para desconectar y refrescar.
- Componente [`LessonLabBlock`](CourselyLabs-front/src/components/LessonLabBlock.vue) que se renderiza dentro de `LessonView` cuando un bloque es de tipo `lab`. Estados claros: sin token → CTA al perfil; sin VM asignada → mensaje al instructor; VM parada → botón Iniciar; VM corriendo → consola noVNC en iframe + Detener. Polling suave cada 10s.
- Editor de bloque `lab` integrado en [`CourseWizard`](CourselyLabs-front/src/views/instructor/CourseWizard.vue): campos para `template_id` e instrucciones markdown.
- Tipo `BlockType` extendido con `lab`.

### Seed

- El curso de Python suma un bloque `lab` (placeholder con `template_id=1`) al final del proyecto, con instrucciones markdown que guían al alumno por la consola.

### Configuración (env vars)

```
ECHO_BASE_URL=http://localhost
ECHO_TIMEOUT_SECONDS=8
APP_ENCRYPTION_SECRET=<openssl rand -hex 32>
APP_ENCRYPTION_SALT=<hex>
```

Si no se proporcionan, se usan defaults solo aptos para dev (no para producción).

### Pendiente para una próxima rama

- **Auto-aprovisionamiento** de VMs por alumno (hoy el alumno necesita un clone ya asignado en echo).
- **Heartbeat / shutdown** automático por inactividad.
- **Vista admin/instructor** de los `lab_session_events` (auditoría desde la app).
- **Selector de plantilla** en el wizard con autocomplete desde echo (en lugar de ID a mano).

---

## [Sin versión] - 2026-05-02 — Recta final: seeds, terminos, recuperacion de contraseña `fix/loose-ends-pass`

### Seeds en castellano

- **`init_db/02_seed_base.sql`** reescrito a castellano correcto. Antes mezclaba catalán (`Programacio`, `disseny`, `Apren PostgreSQL des de zero`, etc.). Ahora todo en castellano con tildes y signos correctos.
  - **Categorías**: `Programación`, `Diseño`, `Negocios`, `Marketing`, `Idiomas`, `Música`. Slugs ASCII: `programacion`, `diseno`, `negocios`, `marketing`, `idiomas`, `musica`.
  - **Slugs de cursos** renombrados: `introduccio-postgresql` → `introduccion-postgresql`, `python-principiants` → `python-principiantes`, `vue3-desenvolupament-web` → `vue3-desarrollo-web`, `algorismes-estructures-dades` → `algoritmos-estructuras-datos`, `disseny-ui-ux` → `diseno-ui-ux`, `marketing-digital-negocis` → `marketing-digital-negocios`, `introduccio-ia` → `introduccion-ia`.
  - Bios, descripciones, reviews — todos al castellano.
- Actualizadas las referencias a los slugs nuevos en [`init_db/03_seed_postgresql.sql`](init_db/03_seed_postgresql.sql), [`init_db/05_seed_prerequisites.sql`](init_db/05_seed_prerequisites.sql) y [`CourselyLabs-back/src/main/resources/db/seed/seed_prerequisites_courses.sql`](CourselyLabs-back/src/main/resources/db/seed/seed_prerequisites_courses.sql).

### Path `/terminos`

- Renombrado `/terminios` → `/terminos` en [`routes.ts`](CourselyLabs-front/src/router/routes.ts). El path antiguo redirige al nuevo para no romper enlaces externos.
- Actualizado el footer y la doc de routing.

### Seeds limpios y curso Python completo

- **Reescritos los seeds** para tener un punto de partida limpio:
  - 1 solo usuario: `instructor@cursos.com` / `instructor123` (hash bcrypt `$2y$10` válido para Spring Security; el hash anterior estaba roto y no permitía login).
  - 5 cursos en cadena de aprendizaje: **Python (entry, gratis) → PostgreSQL (gratis) → {Vue 3, Algoritmos, Diseño UI/UX} (Premium)**, todos asignados al instructor.
  - Sin enrollments ni reviews preconfigurados — el flujo real lo prueban los alumnos al registrarse.
- **Nuevo seed [`init_db/04_seed_python.sql`](init_db/04_seed_python.sql)** que pobla el curso de Python con **un bloque de cada tipo** (text, video, pdf, quiz, project, open_text), incluyendo cuestionario funcional con 3 preguntas y dos assessments (proyecto y open text) para demos completas del modelo multi-bloque.
- **Seed de flamenco desactivado** (renombrado a `04_seed_flamenco.sql.disabled`); se puede reactivar renombrándolo cuando convenga.
- Cadena de prerequisitos reflejada también en el seed homólogo del backend.

### Placeholder de thumbnails

- Nuevo componente reutilizable [`CourseThumbnail.vue`](CourselyLabs-front/src/components/CourseThumbnail.vue) que centraliza el render del thumbnail: si hay imagen, la muestra; si no, fallback con gradiente de marca (teal → orange) + emoji representativo de la categoría (💻 / 🎨 / 📈 / 📢 / 🌍 / 🎵 / 📚).
- Reemplazado el patrón duplicado en [`CourseCard`](CourselyLabs-front/src/components/CourseCard.vue), [`CourseCardEnrolled`](CourselyLabs-front/src/components/CourseCardEnrolled.vue), [`CourseHero`](CourselyLabs-front/src/components/CourseHero.vue), [`InstructorCourseList`](CourselyLabs-front/src/views/instructor/InstructorCourseList.vue) y [`AdminCourseQueue`](CourselyLabs-front/src/views/admin/AdminCourseQueue.vue).

### Sticky footer

- [`MainLayout`](CourselyLabs-front/src/layouts/MainLayout.vue) fuerza `min-height: calc(100vh - 64px)` en `q-page-container` para que en páginas con poco contenido el footer no destaque (queda fuera de la viewport y solo aparece al hacer scroll de forma natural).

### UX de la ficha del curso

**Prerequisitos visibles para visitantes anónimos**:

- [`CourseService.findBySlug`](CourselyLabs-back/src/main/java/com/courselylabs/courselylab/service/CourseService.java) ya no oculta los prerequisitos a usuarios no autenticados. La lista de cursos requeridos es información pública de la ficha (igual que la descripción o los instructores). Antes el visitante veía «Este curso no tiene prerequisitos» aunque sí los tuviera.
- [`CoursePrerequisitesTab`](CourselyLabs-front/src/components/CoursePrerequisitesTab.vue) ahora acepta también la lista plana `prerequisites` y la renderiza cuando no hay `statuses` (es decir, cuando el visitante no está logueado o no está inscrito). Sin progreso, solo el listado con el umbral requerido.
- Si el visitante no está logueado, aparece un banner con CTA a `/login` para «ver tu progreso en estos cursos».
- Cuando no hay ningún tipo de relación (ni prerequisitos ni cursos que desbloquea), el mensaje pasa de «Este curso no tiene prerequisitos o no hay estado disponible» a «Este curso no tiene cursos relacionados» (más claro).

**Mensaje guía en valoraciones**:

- [`CourseTabReviews`](CourselyLabs-front/src/components/CourseTabReviews.vue) ahora explica las condiciones para poder valorar el curso cuando el formulario no aparece. Tres niveles según en qué condición falle el usuario:
  - No logueado → «Inicia sesión e inscríbete al curso para poder valorarlo.»
  - Logueado pero no inscrito → «Inscríbete al curso y completa al menos una lección para poder valorarlo.»
  - Inscrito pero sin lecciones completadas → «Completa al menos una lección para poder valorar el curso.»

### Gestión de categorías (admin)

**Backend**:

- [`CategoriaController`](CourselyLabs-back/src/main/java/com/courselylabs/courselylab/controller/CategoriaController.java): los endpoints `POST`, `PUT` y `DELETE` ahora requieren `@PreAuthorize("hasRole('admin')")`. Antes cualquier usuario autenticado podía crear/editar/borrar categorías.
- [`CategoriaService.delete`](CourselyLabs-back/src/main/java/com/courselylabs/courselylab/service/CategoriaService.java) ahora bloquea el borrado si hay cursos asociados (en lugar de cascadear a NULL o romper la BD). Mensaje claro al admin con el número de cursos que la usan.
- Nuevo método `CourseRepository.countByCategoryId(...)` para soportar la verificación.

**Frontend**:

- Nuevo cliente API [`api/category.ts`](CourselyLabs-front/src/api/category.ts) con `listCategories`, `createCategory`, `updateCategory`, `deleteCategory`.
- Nueva vista [`AdminCategoryTable.vue`](CourselyLabs-front/src/views/admin/AdminCategoryTable.vue) en `/admin/categorias` — tabla con nombre, slug, descripción y acciones; diálogo único para crear/editar (autocompleta el slug a partir del nombre, con validación `^[a-z0-9-]+$`); confirmación de borrado.
- Enlace «Categorías» en [`AdminLayout`](CourselyLabs-front/src/layouts/AdminLayout.vue).

### Recuperación de contraseña (`feature/forgot-password`)

**Backend**:

- **Migración V12** [`V12__create_password_reset_tokens.sql`](CourselyLabs-back/src/main/resources/db/migration/V12__create_password_reset_tokens.sql) — tabla `password_reset_tokens` siguiendo el mismo patrón que `verification_tokens`.
- Nueva entidad [`PasswordResetTokenEntity`](CourselyLabs-back/src/main/java/com/courselylabs/courselylab/entity/PasswordResetTokenEntity.java) y repositorio.
- [`EmailService.sendPasswordResetEmail(user)`](CourselyLabs-back/src/main/java/com/courselylabs/courselylab/service/EmailService.java) genera token (UUID, 1 hora de validez) y envía email HTML con el enlace `/restablecer-contrasena?token=...`.
- [`AuthService.requestPasswordReset(email)`](CourselyLabs-back/src/main/java/com/courselylabs/courselylab/service/AuthService.java): no revela si el email existe (siempre 204) por seguridad.
- [`AuthService.resetPassword(token, newPassword)`](CourselyLabs-back/src/main/java/com/courselylabs/courselylab/service/AuthService.java): valida token, actualiza contraseña y **revoca todas las sesiones existentes** del usuario.
- Endpoints `POST /api/auth/request-password-reset` y `POST /api/auth/reset-password` ([`AuthController`](CourselyLabs-back/src/main/java/com/courselylabs/courselylab/controller/AuthController.java)).

**Frontend**:

- Link «¿Olvidaste tu contraseña?» en [`formLogin.vue`](CourselyLabs-front/src/components/formLogin.vue).
- Nueva vista [`RequestPasswordResetView.vue`](CourselyLabs-front/src/views/RequestPasswordResetView.vue) en `/recuperar-contrasena` — formulario para introducir email; muestra confirmación neutral tras envío (no revela existencia del email).
- Nueva vista [`ResetPasswordView.vue`](CourselyLabs-front/src/views/ResetPasswordView.vue) en `/restablecer-contrasena?token=…` — formulario de nueva contraseña con confirmación.

---

## [Sin versión] - 2026-04-30 — Modo oscuro completo `feature/dark-mode-polish`

### Sistema de temas

- **Paleta semántica de variables CSS** en [`src/css/app.scss`](CourselyLabs-front/src/css/app.scss). Define en `:root` los tokens del modo claro y los redefine bajo `body.body--dark` para el modo oscuro. Tokens:
  - `--app-bg`, `--app-bg-soft`, `--app-surface`, `--app-surface-soft`
  - `--app-text`, `--app-text-strong`, `--app-text-soft`, `--app-text-muted`
  - `--app-border`, `--app-border-strong`
  - `--app-primary-soft`, `--app-primary-tint`, `--app-accent-soft`
  - `--app-warning-soft`, `--app-warning-text`, `--app-info-soft`, `--app-info-text`
  - `--app-overlay`
- **Eliminados los overrides agresivos en `app.scss`** que rompían el modo oscuro:
  - `body { background-color: white !important; color: black !important; }` → eliminado.
  - `.q-drawer { background-color: white !important; }` → eliminado.
  - Reemplazados por estilos sin `!important` que dejan a Quasar gestionar el body según `Dark.set(...)`.

### Sweep masivo de hex → var()

Aplicado con `sed` selectivo a 28 archivos `.vue`. Mapeo principal:

| Hex hardcoded | Variable nueva |
|---|---|
| `#ffffff` / `#fff` | `var(--app-surface)` |
| `#f9fafb` / `#f3f4f6` | `var(--app-bg-soft)` |
| `#f1f5f9` | `var(--app-surface-soft)` |
| `#1f2937` | `var(--app-text)` |
| `#0f172a` | `var(--app-text-strong)` |
| `#6b7280` / `#4b5563` / `#64748b` / `#475569` | `var(--app-text-soft)` |
| `#9ca3af` / `#94a3b8` | `var(--app-text-muted)` |
| `#e5e7eb` | `var(--app-border)` |
| `#d1d5db` | `var(--app-border-strong)` |
| `#f0fdfa` | `var(--app-primary-soft)` |
| `#99f6e4` | `var(--app-primary-tint)` |
| `#fff7ed` | `var(--app-accent-soft)` |
| `#fef3c7` / `#fffbeb` | `var(--app-warning-soft)` |
| `#92400e` | `var(--app-warning-text)` |
| `#0f766e` | `var(--q-primary)` (Quasar) |
| `#ea580c` | `var(--q-accent)` |
| `#d97706` | `var(--q-warning)` |
| `#059669` | `var(--q-positive)` |
| `background: white;` | `background: var(--app-surface);` |

### Layouts

- **[`AppFooter.vue`](CourselyLabs-front/src/layouts/AppFooter.vue)** — usa colores fijos oscuros siempre (es un footer dark intencional). En modo oscuro baja un escalón (`#1f2937` → `#0a0a0a`) para mantener contraste con el contenido. Selector `:global(.body--dark)` para no romper el scoped.
- **[`AppSidebar.vue`](CourselyLabs-front/src/layouts/AppSidebar.vue)** — `class="bg-grey-1"` (hardcoded) → `:class="$q.dark.isActive ? 'bg-grey-9' : 'bg-grey-1'"`.
- **[`LessonView.vue`](CourselyLabs-front/src/views/LessonView.vue)** — `q-layout` y `q-header` con clases condicionales (antes `bg-white text-dark` fijos).
- **[`AppHeader.vue`](CourselyLabs-front/src/layouts/AppHeader.vue)** — sigue usando `var(--q-primary)` para el fondo (teal en ambos modos, intencional).

### Notas

- **Gradientes intencionados** se preservan (hero del curso, banners de éxito, badges Premium…). Algunos usan `var(--q-primary)` y `var(--q-accent)` ahora; otros (gradientes de marketing en PremiumPage) mantienen hex específicos.
- **`AdminLayout`, `InstructorLayout` y `MainLayout`** ya tenían modo oscuro de la rama anterior (`fix/ux-pass-1`).
- **Pendiente refinamiento manual** — algunos componentes con `text-grey-7` (clase Quasar) pueden verse poco legibles en oscuro. Ajustar caso por caso a medida que aparezcan.

### Iteración tras testing manual

- **Layout roto en toda la app**: el archivo `src/assets/main.css` (scaffold inicial de Vue/Vite que nunca se quitó) imponía `#app { max-width: 1280px; margin: 0 auto; padding: 2rem; }` y `body { display: flex; place-items: center; }`. Esto creaba bandas negras a los lados. Antes era invisible porque `app.scss` con `!important` lo sobrescribía; al quitar los `!important` para que el dark mode funcionara, `main.css` tomó el control y rompió el layout.
  - **Eliminados `src/assets/main.css` y `src/assets/base.css`** (deuda del scaffold).
  - **Quitado el import** de `main.css` en [`main.ts`](CourselyLabs-front/src/main.ts).
  - **Restaurado `width: 100%`** y `min-height` en `html, body, #app, #q-app` en `app.scss` (sin `!important`).
- **Fondo del body en dark**: quitado el override `body { background: var(--app-bg); color: var(--app-text); }` del `app.scss` para dejar a Quasar gestionar `body.body--dark` automáticamente. Quasar pinta el body con `$dark-page` (`#111827`) y los componentes con `$dark` (`#1F2937`) sin que tengamos que tocar nada.
- **Contraste del HomeView en dark**: el hero tenía gradient con muy poca opacidad sobre fondo oscuro y `text-grey-8` en subtítulos (invisible en oscuro). Solución:
  - `.hero-title` y `.hero-subtitle` con `color: var(--app-text-strong)` / `var(--app-text-soft)` (cambian con el tema).
  - Selector `:global(.body--dark) .hero` con gradient más opaco (0.18/0.12) para que se vea sobre fondo oscuro.
  - `text-grey-8` → `text-grey-7` en todos los lugares de HomeView.
- **CourseCard**: títulos y descripciones se veían "fantasma" en dark. Reemplazado `text-grey-7`/`text-grey-8` por clases custom `.card-title` (var(--app-text-strong)), `.card-description` y `.rating-text` (var(--app-text-soft)).

---

## [Sin versión] - 2026-04-30 — Pasada de bugs y UX `fix/ux-pass-1`

### Bloque 6 — Iteración tras testing manual

- **Botón "Nuevo curso" del estado vacío** también respeta `:disable="!canCreate"` (antes solo el del header).
- **`SidebarItem.vue` rehecho** con dos templates `v-if/v-else` y prop `exact` nativa de `q-item` (evita el problema de `:exact-active-class="undefined"` no aplicando bien la cancelación del `active-class` por defecto).
- **Saltos de línea con palabras largas** (`aaaaa…`): añadido `overflow-wrap: anywhere; word-break: break-word;` en `.hero-short-description` y en `.rich-content` global.
- **Tab "Valoraciones" bloqueaba el resto de tabs**: causa probable era doble — `<CourseTabInstructors>` se usaba como hijo directo de `q-tab-panels` (Quasar lo busca por hijos directos `q-tab-panel`); además los `name="descripción"` con tilde en tabs/panels eran frágiles. Solución: nuevo componente [`CourseTabInstructorsList.vue`](CourselyLabs-front/src/components/CourseTabInstructorsList.vue) sin `q-tab-panel` envolvente, inline dentro de un `<q-tab-panel name="instructores">` en CourseDetailView; tabs renombradas a ASCII (`descripcion` en lugar de `descripción`); `animated` reemplazado por `keep-alive` en `q-tab-panels`.
- **Modo oscuro de los layouts**:
  - [`MainLayout.vue`](CourselyLabs-front/src/layouts/MainLayout.vue): `class="bg-white"` → `:class="$q.dark.isActive ? 'bg-grey-10' : 'bg-grey-1'"`.
  - [`AdminLayout.vue`](CourselyLabs-front/src/layouts/AdminLayout.vue): mismo tratamiento al `q-layout`, al `q-header` y al `q-drawer`.
  - [`InstructorLayout.vue`](CourselyLabs-front/src/layouts/InstructorLayout.vue): `q-drawer` con `:class` condicional. Pendiente: muchos `<style scoped>` con colores hardcoded, eso es trabajo de `feature/dark-mode-polish`.
- **Header sin huecos en el layout**: la versión anterior cambiaba la altura del `q-header` (120 → 56 px), lo que dejaba un hueco en el `q-page-container`. Reescrito con altura fija (~64 px) y la animación se aplica solo al **branding interno** (logo se contrae de 44 → 32 px y se desplaza de centrado a la izquierda con `flexbox justify-content`). El `q-layout` no recalcula el espacio reservado y no hay huecos.
- **Contador "Cursos pendientes" en sidebar admin se actualiza al instante** tras aprobar/rechazar: `AdminLayout` escucha `window` event `admin:refresh-pending`; `AdminCourseQueue` lo emite en `handleApprove` y `handleReject`. Sin recargar la página.
- **Fallback en `CourseTabInstructorsList`** acepta tanto los nombres nuevos (`name`, `avatarUrl`) como los antiguos (`fullName`, `profilePictureUrl`) por si el backend no se ha reiniciado tras el cambio del DTO.

- **`/profile` no guardaba al editar perfil** — el handler `saveProfile` salía con un `return` silencioso si los campos no pasaban un check inline (`length < 2`), sin notificar al usuario. Y el botón "Guardar cambios" tenía `@click` además del `@submit` del `<q-form>` (doble llamada). Solución:
  - Botón con `type="submit"` (cancela tiene `type="button"`); `q-form` con `ref` y validación vía `profileFormRef.value.validate()` antes de enviar (muestra errores visuales si los hay).
  - Eliminado el guard silencioso.
  - El `catch` ahora extrae `err.response.data.message` (mensaje real del backend) en lugar de un genérico.
  - Guard para `user.value?.id` con notify ("Sesión expirada") si no hay sesión.
  - Texto "Sobre mi" → "Sobre mí" (tilde correcta).



### Bloque 1 — Bugs rápidos

- **Email verificado obligatorio para crear cursos** — `CourseService.create()` lanza ahora `BadRequestException` si `user.isVerified == false`, salvo admin. Frontend: en [`InstructorCourseList.vue`](CourselyLabs-front/src/views/instructor/InstructorCourseList.vue) el botón "Nuevo curso" se deshabilita y aparece un banner ámbar con CTA a "Mi perfil" para reenviar el correo de verificación.
- **Sidebar pública: link "Inicio" se quedaba marcado** — `<q-item :to="/">` sin `exact-active-class` hacía match con cualquier ruta. Solución: prop `exact?: boolean` en [`SidebarItem.vue`](CourselyLabs-front/src/layouts/SidebarItem.vue) que aplica `exact-active-class` solo al link `/`.
- **Modal de inscripción "Seguir explorando" no redirigía** — el botón solo cerraba el dialog. Ahora `handleKeepExploring()` cierra el dialog **y** hace `router.push('/cursos')`.
- **Tabs bloqueadas tras pulsar "Valoraciones" en vista previa** — el `<q-tab-panel name="valoraciones">` envolvía `<CourseTabReviews>` con un `<q-banner>` innecesario que rompía la interacción con las tabs. Eliminado.

### Bloque 2 — UX media

- **Saltos de línea en descripciones** — `course.shortDescription` (texto plano de un textarea) no respetaba `\n`. Solución: clase `.hero-short-description` con `white-space: pre-line` en [`CourseHero.vue`](CourselyLabs-front/src/components/CourseHero.vue), y migración del bloque `.rich-content` a [`src/css/app.scss`](CourselyLabs-front/src/css/app.scss) global con la misma propiedad para que la descripción larga (TipTap) también respete saltos.
- **Pestaña instructores mostraba "placeholder" siempre** — el `InstructorSummaryDTO` del backend usaba `fullName` y `profilePictureUrl`, pero el frontend esperaba `name` y `avatarUrl`. Renombrado en backend para alinear con el frontend; añadido `isMain` para mostrar badge "Principal". Inline en `CourseDetailView` reemplazado por componente [`CourseTabInstructors.vue`](CourselyLabs-front/src/components/CourseTabInstructors.vue) con avatar, badge "Principal", bio con saltos de línea y orden del principal primero.
- **Filtros de cursos rediseñados** — antes había 5 selects sueltos en línea ocupando 2 filas. Ahora hay una **barra de búsqueda** + un botón **"Filtros"** con `q-btn-dropdown` que despliega categoría + nivel + tipo en un panel ordenado, con badge de contador de filtros activos + botón "Limpiar todo". Aparte, un select de orden. Chips activos debajo con labels descriptivos. Strings traducidos al castellano (antes había `Beginner`/`Intermediate`/`Advanced` mezclados).

### Bloque 3 — Layout

- **Header rediseñado** — antes el header se ocultaba al hacer scroll. Ahora arranca alto (~120 px) con logo (`/logo.png`, sin fondo) + texto "CourselyLabs" centrados juntos. Al scrollear se contrae a ~56 px y branding se desplaza a la izquierda con animación de 0.3 s. El logo `Logo-no-bg.png` se ha movido a `CourselyLabs-front/public/logo.png` (URL pública estable).
- **Footer compacto con contenido real** — antes era una `q-toolbar` fija con un solo `<q-toolbar-title>` enorme con el copyright. Ahora es no-fijo (`q-layout view="hHh Lpr fff"` con `f` minúscula → footer estático que aparece al final del scroll). Contenido: branding con logo, 3 columnas (Producto, Legal, Soporte) y barra inferior con copyright pequeño. Algunos links apuntan a `/proximamente` (página por crear).

### Bloque 4 — Modo oscuro fase 1

- **Toggle en sidebar** — nuevo item "Modo oscuro" / "Modo claro" en [`AppSidebar.vue`](CourselyLabs-front/src/layouts/AppSidebar.vue) con icono `dark_mode`/`light_mode`.
- **Persistencia** — la elección se guarda en `localStorage` con clave `coursely-dark`.
- **Inicialización** — al arrancar la app, [`main.ts`](CourselyLabs-front/src/main.ts) lee `localStorage` y aplica `Dark.set(true/false)` antes de montar.
- ⚠️ **Deuda**: muchos componentes propios usan colores hardcoded en `<style scoped>` (`#0f766e`, `#f9fafb`, etc.) que no respetan el modo oscuro. El sweep de variables Quasar es trabajo del Bloque 6 (rama futura `feature/dark-mode-polish`).

### Bloque 5 — Planificación (sin código)

- Documentadas dos secciones nuevas en [`docs/06-roadmap/branches.md`](docs/06-roadmap/branches.md):
  - **§19 `feature/co-instructors`** — qué falta de UI/backend para gestionar co-instructores: añadir/eliminar, promover principal, búsqueda de usuarios. Dependencias con `feature/messaging` y `feature/notifications`. Decisiones de producto pendientes (¿pueden editar?, ¿requieren consentimiento?).
  - **§20 `chore/i18n-total`** — plan completo de migración del resto del frontend a `t()`. Incluye el traspaso de las **categorías de cursos** (hoy mezclan catalán/castellano en el seed: `Programacio`, `Disseny`, `Negocis`, `Idiomes`, `Musica`) usando `t('category.' + slug)` con bloque `category` en `es.json`.

### Otros cambios menores

- Movidos `CourselyLabs-front/Logo*.png` a `CourselyLabs-front/public/logo.png` y `logo-with-bg.png` (URLs estables, no `git mv` con rename solo una vez).
- Removidos los estilos duplicados de `.rich-content` en `LessonTextViewer.vue` (ahora viven en `app.scss` global).

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
