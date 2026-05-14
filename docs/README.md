yes# Documentación de CourselyLabs

Índice navegable de toda la documentación técnica del proyecto.

> **Si llegas por primera vez**: lee primero [01-overview/state.md](01-overview/state.md) — es el snapshot completo del proyecto en 10 minutos de lectura.

---

## 01 — Visión general

Qué es el producto, qué hay hecho, cómo está organizado.

- [**state.md**](01-overview/state.md) — Estado del proyecto: stack real, features hechas/pendientes, decisiones, deuda técnica.
- [**product-spec.md**](01-overview/product-spec.md) — Especificación funcional del producto (qué hace, reglas de negocio).
- [**architecture.md**](01-overview/architecture.md) — Capas, módulos, flujo de datos.
- [**data-model.md**](01-overview/data-model.md) — Entidades JPA, relaciones, esquema relacional.

## 02 — Empezar a trabajar

Cómo levantar el entorno y trabajar con los servicios.

- [**setup.md**](02-getting-started/setup.md) — Instalación y comandos básicos.
- [**docker.md**](02-getting-started/docker.md) — Servicios en contenedores (postgres, mailhog, minio).
- [**services.md**](02-getting-started/services.md) — Puertos y URLs de cada servicio local.
- [**env-vars.md**](02-getting-started/env-vars.md) — Variables de entorno (front + back).
- [**seeders.md**](02-getting-started/seeders.md) — Cómo cargar datos de prueba.

## 03 — Frontend (Vue 3 + Quasar)

- [**README.md**](03-frontend/README.md) — Comandos, estructura y endpoints principales.
- [**design-system.md**](03-frontend/design-system.md) — Paleta de colores, tipografía, logo, accesibilidad.
- [**quasar.md**](03-frontend/quasar.md) — Personalización del framework Quasar.
- [**routing.md**](03-frontend/routing.md) — Mapa de rutas y guards.
- [**state.md**](03-frontend/state.md) — Stores Pinia y patrones de estado.
- [**i18n.md**](03-frontend/i18n.md) — Política de traducción y cómo añadir claves.

## 04 — Backend (Spring Boot)

- [**README.md**](04-backend/README.md) — Layout de paquetes y comandos.
- [**api.md**](04-backend/api.md) — Referencia de endpoints REST.
- [**security.md**](04-backend/security.md) — Spring Security, JWT, roles y `@PreAuthorize`.
- [**migrations.md**](04-backend/migrations.md) — Flyway: cómo añadir migraciones.
- [**storage.md**](04-backend/storage.md) — MinIO (S3-compatible) para uploads.

## 05 — Features

Documentación funcional por feature.

- [**auth.md**](05-features/auth.md) — Registro, login, JWT, refresh, verificación de email.
- [**courses-lessons.md**](05-features/courses-lessons.md) — CRUD de cursos, secciones y lecciones.
- [**lesson-blocks.md**](05-features/lesson-blocks.md) — Refactor multi-bloque (V11).
- [**assessments.md**](05-features/assessments.md) — Quiz, proyecto y pregunta abierta.
- [**grading.md**](05-features/grading.md) — Dashboard de calificación.
- [**prerequisites.md**](05-features/prerequisites.md) — Prerequisitos entre cursos.
- [**premium-stripe.md**](05-features/premium-stripe.md) — Suscripción Premium con Stripe.
- [**progress.md**](05-features/progress.md) — Tracking de progreso del estudiante.
- [**reviews.md**](05-features/reviews.md) — Sistema de valoraciones.

## 06 — Roadmap

- [**branches.md**](06-roadmap/branches.md) — Plan de ramas y estado por feature.
- [**ux-backlog.md**](06-roadmap/ux-backlog.md) — Mejoras UX detectadas pendientes.

## 07 — Convenciones

Cómo trabajamos en este repo.

- [**git-workflow.md**](07-conventions/git-workflow.md) — Convenciones de ramas, commits y PRs.
- [**code-style.md**](07-conventions/code-style.md) — Estilo de código backend y frontend.
- [**i18n-policy.md**](07-conventions/i18n-policy.md) — Política de strings castellano.

---

## Mantenimiento de esta documentación

- **Cambios técnicos relevantes** → registrar en [`/CHANGELOG.md`](../CHANGELOG.md).
- **Features nuevas** → crear archivo en `05-features/` con la misma estructura que el resto.
- **Migraciones nuevas** → actualizar `04-backend/migrations.md` y, si afecta al modelo, `01-overview/data-model.md`.
- **Cambios de stack** → actualizar `01-overview/state.md` §2.
- **Convenciones nuevas** → añadir a `07-conventions/`.
