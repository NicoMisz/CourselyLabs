# Git workflow

> Convenciones de ramas, commits y pull requests en CourselyLabs.

---

## Branching model

Variante simplificada de Git Flow:

```
main      ← solo merges desde develop, releases etiquetadas
   ▲
develop   ← rama de integración
   ▲
feature/* ← una rama por feature/cambio
hotfix/*  ← arreglos urgentes desde main
```

### `main`

- Estado **estable y desplegable**.
- Solo recibe merges desde `develop` (releases) o `hotfix/*` (urgencias).
- Cada merge a `main` debería etiquetarse con un tag de versión (`v0.1.0`, `v0.2.0`, …).

### `develop`

- Estado **estable, lista para merge a main**.
- Las features se mergean aquí cuando están terminadas.
- Build y tests deben pasar.

### `feature/*`

- Una rama por feature, refactor o cambio importante.
- Se ramifica desde `develop`.
- Convención de nombres: `feature/descripcion-corta-en-kebab-case`.
  - `feature/assessments`
  - `feature/forums`
  - `feature/payments-stripe`
- Para chores (no añaden feature, solo limpieza/infra): `chore/descripcion`.
  - `chore/codebase-cleanup`
  - `chore/i18n-castellano`
  - `chore/deployment`
- Para bug fixes que no entran en una feature en curso: `fix/descripcion`.
  - `fix/security-improvements`

---

## Commits

### Estilo

- **En castellano** o inglés — actualmente hay mezcla histórica. **Recomendado: castellano** para nuevos commits.
- **Imperativo** ("Añade X", no "Añadiendo X" ni "Añadido X").
- **Conciso** — la primera línea ≤ 72 caracteres.
- **Sin punto final** en la primera línea.
- **Cuerpo opcional** separado por una línea en blanco; explicar el porqué, no el qué (el diff ya muestra el qué).

### Ejemplos buenos

```
Añade dashboard de calificación con filtro por curso

Permite al instructor ver todas las entregas pendientes de
sus cursos en una sola vista. Incluye deep-link al bloque
en el editor para revisar enunciado/configuración.
```

```
Refactor multi-bloque: lecciones como contenedores de bloques

V11 introduce lesson_blocks como capa intermedia entre
lessons y assessments. Permite mezclar formatos en una lección
y tener N evaluaciones por lección.
```

```
Corrige URL de routing tras sweep de tildes

Las rutas /cursos/:slug/leccion/:id y /pago/exito quedaron
con tildes tras un sed masivo. URLs deben ser ASCII.
```

### Mensajes a evitar

- "Cambios" / "Update" / "Fix" / "WIP" — sin info.
- "Megacommit de stripe y cosas premium (no funca del todo)" — debería partirse en commits que sí funcionan.
- "Un commit de nada, pequeñito, no hay contenido" — si no hay contenido, no hagas commit.

### Tamaño

- **Pequeño y atómico**: un commit = un cambio coherente.
- Si tu cambio toca front + back y son separables, considera dos commits.
- Si tu cambio es un refactor + cambio de comportamiento, sepáralos: primero refactor (sin cambio de comportamiento), luego el cambio.
- Excepción: si el refactor solo tiene sentido junto al cambio funcional, mantenlo junto.

---

## Pull requests

### Cuándo abrir

- Cuando la feature está **completa** o lista para revisión.
- Si quieres feedback temprano: abre como **draft**.

### Título

- Descriptivo, en castellano, ≤ 70 caracteres.
- Referencia issue si aplica.

### Cuerpo

Plantilla mínima:

```markdown
## Resumen
- Qué cambia (3-5 bullet points)

## Cambios técnicos
- Archivos clave tocados
- Migraciones nuevas (si las hay)
- Endpoints nuevos (si los hay)

## Test plan
- [ ] Compila backend (`./mvnw compile`)
- [ ] Build frontend (`npm run build`)
- [ ] Probado manualmente: <flujos>
- [ ] Tests automáticos pasan (si los hay)

## Doc / changelog
- [ ] Documentación actualizada en `docs/`
- [ ] CHANGELOG.md actualizado
```

### Antes de mergear

- ✅ Build y type-check pasan.
- ✅ La rama está actualizada con `develop` (rebase o merge según prefiera el equipo).
- ✅ CHANGELOG y docs reflejan el cambio.
- ✅ Aprobación de al menos otro miembro del equipo (cuando sois más de uno).

### Estrategia de merge

- **Squash merge** para features pequeñas y claras (un solo commit en `develop`).
- **Merge commit** para features grandes con histórico significativo (preserva todos los commits).
- **Rebase + merge** si quieres mantener histórico lineal sin merge commits.

Hoy el repo usa **merge commits** principalmente; mantenerlo así por consistencia.

---

## Coordinación con migraciones Flyway

⚠️ **Coordina antes** de empezar una rama que añada una migración.

Si dos ramas crean `V12__xxx.sql` en paralelo, la segunda en mergear deberá renombrar a `V13__xxx.sql` antes de la fusión, lo que puede ser molesto. Buena práctica:

1. Anuncia en el canal del equipo: "voy a usar V12 para feature X".
2. Si ves un PR abierto con tu mismo número, coordina.
3. La numeración va por orden de merge a `develop`, no por orden de creación de la rama.

---

## Tags y releases

Cuando se haga `develop → main`:

```bash
git tag -a v0.X.0 -m "Release v0.X.0 — descripción breve"
git push origin v0.X.0
```

GitHub auto-generará la release notes desde los commits, pero puedes editar para añadir highlights y breaking changes.

---

## Referencias

- [Conventional Commits](https://www.conventionalcommits.org/) — convención popular si quieres adoptar `feat:`, `fix:`, `chore:` (no obligatorio aquí).
- [Git Flow](https://nvie.com/posts/a-successful-git-branching-model/) — el original.
