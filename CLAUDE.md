# Instrucciones para Claude

> Este archivo lo lee Claude (asistente de IA) al empezar cada sesión. Contiene memorias, preferencias y atajos para trabajar en este repo.
>
> Para entender el producto, lee [`docs/01-overview/product-spec.md`](docs/01-overview/product-spec.md). Para el stack y estado actual, [`docs/01-overview/state.md`](docs/01-overview/state.md).

---

## Quién soy y cómo trabajamos

- **Idioma de comunicación**: castellano. Todo el contexto y conversación en castellano.
- **Strings de UI**: castellano correcto, con tildes y signos de puntuación apropiados (`¿…?`, `¡…!`, `«…»`).
- **Identificadores en código**: inglés. `lessonId`, `currentQuestion`, `LessonBlock`. Solo strings UI van en castellano.

## Convenciones del repo

Todas documentadas en [`docs/07-conventions/`](docs/07-conventions/):

- [`git-workflow.md`](docs/07-conventions/git-workflow.md) — ramas, commits, PRs.
- [`code-style.md`](docs/07-conventions/code-style.md) — estilo backend (Java) y frontend (Vue 3 + TS).
- [`i18n-policy.md`](docs/07-conventions/i18n-policy.md) — política de strings y `vue-i18n`.

## Cuando me pidan cambios, recordar

1. **Siempre actualizar `CHANGELOG.md`** cuando se hagan cambios relevantes.
2. **Si toca documentación**, mantenerla en `docs/` con la jerarquía existente.
3. **Si toca el modelo de datos**, añadir migración Flyway (`V12+`) y actualizar `docs/01-overview/data-model.md`.
4. **Si toca strings UI**, usar `t()` en componentes nuevos. Para componentes con strings inline existentes, basta con que estén ortográficamente correctos.
5. **Si toca rutas**, las URLs son ASCII (sin tildes) y deben actualizar `docs/03-frontend/routing.md`.

## Memoria persistente

Mi memoria persistente entre conversaciones vive en `~/.claude/projects/-home-nmiszczak-Documentos-CourselyLabs/memory/MEMORY.md`. Allí recuerdo:

- Preferencias del usuario.
- Estructura de la documentación (esta que hay aquí).
- Paleta de colores, fuentes y stack.
- Auth flow.

Si necesito refrescar, miro ahí.

## Stack en una línea

Vue 3 + Quasar + TypeScript + Pinia + vue-i18n (frontend) · Spring Boot 4 + Java 21 + PostgreSQL 16 + Flyway + JWT + Stripe + MinIO (backend) · Docker Compose para infra local.

Detalle real en [`docs/01-overview/state.md`](docs/01-overview/state.md) §2.

## Atajos para Claude

Si el usuario pide:

- **"Añadir feature X"** → ramificar de `develop`, considerar si necesita migración (V##), backend service+controller+test, frontend api+componente, docs en `05-features/`, CHANGELOG.
- **"Documentar algo"** → buscar en `docs/` la sección correcta. Si es feature nueva, ir a `docs/05-features/`. Si es convención, a `docs/07-conventions/`.
- **"Limpiar tildes"** → ya hay un sweep masivo aplicado; solo casos puntuales nuevos.
- **"Probar algo"** → si requiere browser, **decir explícitamente que no puedo y pedir verificación al usuario**, no inventar que está probado.
