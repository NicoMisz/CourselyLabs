# Mejoras UX — CourselyLabs

Checklist de mejoras de UX/UI detectadas al revisar el producto. Algunas ya aplicadas, otras pendientes.

---

## Ya aplicadas

- [x] **Sidebar**: renombrados "Mis cursos" → "Cursos inscritos" y "Crear curso" → "Cursos creados" + icono distinto (`edit_note`). Elimina ambiguedad para instructores.
- [x] **ProfileView — boton Editar invisible**: era `flat color="primary"` sobre header teal. Cambiado a `outline color="white"` y label "Editar perfil".
- [x] **ProfileView — cursos creados**: nueva card en columna derecha con top 5 cursos creados + chip de estado + enlace "Ver todos". Tambien contador en "Actividad".
- [x] **PremiumPage — centrado**: grid CSS auto-fit en lugar de `row + col-*` de Quasar, se centran correctamente sin importar cuantas cards.
- [x] **PremiumPage — beneficios**: iconos con gradient backgrounds, layout grid en lugar de Quasar row, hover states (border-color + translateY).
- [x] **PremiumPage — precios desde Stripe**: `GET /api/payments/pricing` devuelve los precios reales de los Price IDs de Stripe (fallback a 7/60 si Stripe no responde).
- [x] **PremiumPage — hero con gradient**: seccion full-width con gradient teal→orange, iconos sobre blanco.
- [x] **PremiumPage — tabla comparativa Free vs Premium**: matriz visual de diferencias.
- [x] **Payment history — precio real**: ahora se guarda `amount_total` y `currency` de Stripe en lugar del valor hardcoded.

---

## Pendientes — Alta prioridad

- [ ] **Login del seeder no funciona**: el hash bcrypt del seed no es compatible con Spring Security. Todos los usuarios creados por `02_seed_base.sql` no pueden iniciar sesion con `admin123`. Generar nuevos hashes con `BCryptPasswordEncoder`.
- [ ] **HomeView — hero generico**: el hero actual es un texto simple. Considerar carrusel con cursos destacados, testimonios, o CTA directo a Premium si no eres premium.
- [ ] **CoursesView — sin filtros visibles**: la busqueda avanzada existe en backend pero el frontend solo tiene grid. Anadir filtros de categoria, nivel, gratis/premium, rating.
- [ ] **CourseDetailView — sidebar precio**: mostrar precio tachado y badge "Premium incluido" si el usuario es premium.
- [ ] **MyCoursesView — empty state**: si no hay cursos inscritos, mostrar sugerencias basadas en intereses o top cursos.

---

## Pendientes — Media prioridad

- [ ] **Breadcrumbs**: faltan en la mayoria de vistas internas (Instructor, Admin, Lesson). Navegacion mas clara.
- [ ] **Loading states**: muchas vistas muestran spinner centrado. Skeleton loaders serian mejores (ya usamos algunos en CoursesView).
- [ ] **Feedback de acciones**: tras aprobar/rechazar curso en admin, la card desaparece sin animacion. Anadir slide-out.
- [ ] **LessonView — auto-play siguiente leccion**: al completar una leccion, deberia ofrecer "Siguiente" automaticamente en video o boton destacado.
- [ ] **Busqueda global**: input de busqueda en header que consulte cursos/instructores.
- [ ] **Notificaciones toast posicion**: todas estan en `bottom-right`. En mobile eso tapa el FAB. Mejor `top` en mobile.
- [ ] **Dark mode**: Quasar soporta dark mode out of the box, seria facil anadir toggle.

---

## Pendientes — UX del instructor

- [ ] **CourseContentEditor — drag indicator solo al hover**: mostrar el icono `drag_indicator` solo cuando el cursor este sobre la seccion/leccion.
- [ ] **Wizard de creacion — guardar progreso al navegar**: si el usuario cambia de tab en el stepper sin guardar, avisar.
- [ ] **Dashboard de instructor — estadisticas**: anadir grafica de estudiantes inscritos a lo largo del tiempo, rating medio, cursos mas populares.
- [ ] **Preview modo read-only**: abrir `/cursos/{slug}` en pestana nueva funciona, pero seria mejor un modal inline con la misma vista.
- [ ] **Editor de leccion — upload de archivos**: el modal actual solo acepta URLs. Cuando este `feature/downloadable-resources`, anadir upload directo aqui tambien.

---

## Pendientes — UX del admin

- [ ] **AdminCourseQueue — preview inline**: en lugar de abrir pestana nueva, ver el contenido del curso en el mismo panel (split screen).
- [ ] **AdminUserTable — acciones en lote**: checkbox por fila para banear/cambiar rol a multiples usuarios a la vez.
- [ ] **AdminDashboard — grafico historico**: los numeros absolutos no muestran tendencia. Anadir comparativa mes anterior.
- [ ] **Confirmacion de acciones destructivas**: cambiar rol de otro admin o banear deberia pedir password del admin actual (sensitive actions).

---

## Pendientes — Responsive / mobile

- [ ] **CourseSidebar en mobile**: el sidebar fijo se descoloca en pantallas pequenas. Mover debajo del contenido en mobile.
- [ ] **Instructor/Admin layouts**: el drawer no se colapsa bien en mobile, solape contenido.
- [ ] **Tablas q-table en mobile**: formato card-list seria mas legible.

---

## Detalles menores (quick wins)

- [ ] **Iconos Material Icons**: algunas paginas mezclan iconos `home` con `sym_o_school`. Unificar.
- [ ] **Fecha relativa**: en "Ultimo acceso" se muestra fecha absoluta. Usar "hace 2 dias" seria mas natural.
- [ ] **Precio en cards de curso**: si es premium, mostrar icono de corona en lugar de precio numerico.
- [ ] **Favicon**: comprobar que esta actualizado con el logo.
- [ ] **Meta tags SEO**: `og:image`, `og:description` por vista (ya hay en CourseDetailView, faltan en resto).
