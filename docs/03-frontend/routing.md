# Routing

> Mapa de rutas, guards de autenticación, deep-links. Definidas en `CourselyLabs-front/src/router/routes.ts`.

---

## Estructura

El router está en `src/router/index.ts` (instancia + guards) y `src/router/routes.ts` (definición de rutas). Vue-router 4.

Hay **tres layouts**:
- `MainLayout.vue` — público (con header y footer estándar).
- `InstructorLayout.vue` — `/instructor/*`, sidebar de instructor.
- `AdminLayout.vue` — `/admin/*`, sidebar de admin.

`LessonView.vue` no tiene layout: ocupa toda la pantalla con su propio header y sidebar de navegación de lecciones.

---

## Mapa de rutas

### Públicas (con MainLayout)

| Path | Componente | Notas |
|---|---|---|
| `/` | `HomeView.vue` | Inicio |
| `/cursos` | `CoursesView.vue` | Catálogo |
| `/cursos/:slug` | `CourseDetailView.vue` | Detalle de curso |
| `/login` | `formView.vue` | Login |
| `/register` | `formRegister.vue` | Registro |
| `/terminios` | `cardTerminosCondiciones.vue` | (sí, está mal escrito el path — legacy) |
| `/verificar-email` | `VerifyEmailView.vue` | Confirmación tras click en email |
| `/premium` | `PremiumPage.vue` | Landing de Premium |
| `/pago/exito` | `PaymentSuccessView.vue` | Tras Stripe Checkout exitoso |
| `/pago/cancelado` | `PaymentCancelledView.vue` | Tras Stripe Checkout cancelado |

> **URLs sin tildes** — los paths son ASCII para evitar problemas de encoding y compatibilidad. La política está en [`07-conventions/i18n-policy.md`](../07-conventions/i18n-policy.md).

### Autenticadas

| Path | Componente | Auth | Notas |
|---|---|---|---|
| `/profile` | `ProfileView.vue` | sí | Mi perfil |
| `/mis-cursos` | `MyCoursesView.vue` | sí | Cursos en los que estoy inscrito |
| `/cursos/:slug/leccion/:lessonId` | `LessonView.vue` | sí | Reproducción de lección |

### Instructor (con InstructorLayout, requiere auth)

| Path | Componente | Notas |
|---|---|---|
| `/instructor/cursos` | `InstructorCourseList.vue` | Mis cursos creados |
| `/instructor/cursos/:id/editar` | `CourseWizard.vue` | Editor visual del curso |
| `/instructor/calificar` | `GradingDashboard.vue` | Cola de calificación global |
| `/instructor/cursos/:id/contenido` | redirect → `/editar` | Legacy redirect |
| `/instructor/cursos/nuevo` | redirect → `/cursos` | Legacy redirect |

### Admin (con AdminLayout, requiere `role: admin`)

| Path | Componente | Notas |
|---|---|---|
| `/admin` | `AdminDashboard.vue` | Inicio del panel |
| `/admin/cursos` | `AdminCourseQueue.vue` | Cursos pendientes de revisión |
| `/admin/usuarios` | `AdminUserTable.vue` | Gestión de usuarios |

### Catch-all

| Path | Componente |
|---|---|
| `/:catchAll(.*)*` | `NotFoundView.vue` (con MainLayout) |

---

## Guards

Definidos en `src/router/index.ts`:

```ts
router.beforeEach((to) => {
  if (to.meta.requiresAuth) {
    const authStore = useAuthStore()
    if (!authStore.isLoggedIn) {
      return { path: '/login', query: { redirect: to.fullPath } }
    }
    if (to.meta.requiresRole && authStore.user?.role !== to.meta.requiresRole) {
      return { path: '/' }
    }
  }
})
```

### Meta usado en routes

| Meta | Tipo | Efecto |
|---|---|---|
| `requiresAuth: true` | boolean | Bloquea acceso sin login. Si no hay sesión → redirige a `/login?redirect=...` |
| `requiresRole: 'admin'` | string | Sólo si el rol coincide. Si no, redirige a `/` |
| `title: '...'` | string | Se aplica a `document.title` en `router.afterEach` |

> Hoy solo `admin` se controla por `requiresRole`. Para `premium` se hacen comprobaciones a nivel de componente (vía `checkIsPremium` o el store), no en el guard.

---

## Deep-links importantes

### Calificar → Editor con bloque resaltado

Desde `/instructor/calificar` cada entrega tiene un botón que navega a:

```
/instructor/cursos/{courseId}/editar?lesson={lessonId}&block={blockId}
```

`CourseWizard.vue` lee `route.query` en `applyDeepLinkFromQuery()`:

1. Selecciona la lección correspondiente en el sidebar.
2. Hace `scrollIntoView({ behavior: 'smooth', block: 'center' })` al elemento `#block-{blockId}`.
3. Aplica la clase `.block-highlight` durante 2.2 s para destacarlo.

Detalles en [`05-features/grading.md`](../05-features/grading.md).

### Login con redirect

Cuando el guard bloquea acceso a una ruta autenticada, redirige a `/login?redirect=<ruta-original>`. Tras login exitoso, el formulario lee ese query y vuelve a la ruta original.

---

## Convenciones para añadir rutas

1. **Path en kebab-case y ASCII**. No tildes. Ejemplos: `/mis-cursos`, `/verificar-email`.
2. **Lazy load**: usar `() => import('@/views/...')` siempre. Vite divide cada uno en chunk.
3. **Meta `title` siempre** para rutas top-level. Vue-router lo aplica a `document.title` en `afterEach`.
4. **Auth-protegidas**: añadir `meta: { requiresAuth: true }`. Para admin: `meta: { requiresAuth: true, requiresRole: 'admin' }`.
5. **Si la ruta cambia de nombre**, dejar un redirect en lugar de eliminarla:
   ```ts
   { path: 'old-path', redirect: '/new-path' }
   ```
6. **Layout adecuado**: rutas públicas o de estudiante → `MainLayout`. Rutas de instructor → `InstructorLayout`. Rutas admin → `AdminLayout`.

---

## TODO conocidos

- `/terminios` debería ser `/terminos` (faltó en el sweep) — pendiente cuando se rediseñe la página de T&C.
- Falta página de "Olvidé mi contraseña" / reset.
- El catch-all 404 no informa de qué ruta intentó visitar (nice-to-have).
