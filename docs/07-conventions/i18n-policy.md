# Política de strings (castellano + i18n)

> Cómo manejamos texto visible al usuario. Para detalles técnicos de `vue-i18n` y cómo añadir claves ver [`03-frontend/i18n.md`](../03-frontend/i18n.md).

---

## Reglas obligatorias

1. **Todo string visible al usuario va en castellano correcto**:
   - Tildes (`evaluación`, `descripción`, `categoría`).
   - Signos de apertura `¿…?` y `¡…!`.
   - Comillas `«…»` o `“…”` (no `"..."` para citas, sí en código).
2. **Componentes nuevos usan `t()` desde el primer commit**. No se acepta hardcodear strings en componentes nuevos.
3. **URLs son ASCII** (sin tildes). `/cursos/:slug/leccion/:id`, no `/lección/`.
4. **Identificadores en código siguen en inglés**. `lessonId`, `currentQuestion`, `LessonBlock`. Los strings castellano viven en JSON, los símbolos en el código son inglés.
5. **No mezclamos idiomas en la UI**. Si un componente está parcialmente en inglés (legacy), no lo dejes a medias — tradúcelo entero o no lo toques.

---

## Cuándo migrar a `t()`

| Situación | Acción |
|---|---|
| Componente **nuevo** | `t()` desde el inicio. Obligatorio. |
| Componente que **tocas para cambiar/añadir** features | Migra los strings que toques en ese mismo PR. |
| Componente que **solo lees** | Migración no obligatoria. Si ves faltas de tilde, corrígelas inline. |
| Componente con **muchos strings inline** que quieres limpiar | PR aparte de "i18n migration of X" — no mezclarlo con cambios funcionales. |

---

## Estado de la migración

Hoy, **3 componentes** están migrados a `t()`:
- `layouts/InstructorLayout.vue`
- `views/instructor/GradingDashboard.vue`
- `components/AssessmentEditor.vue`

**El resto del frontend (~65 archivos)** usa strings inline en castellano correcto (tras un sweep masivo de tildes con `sed`). La migración progresiva se hace cuando se tocan los componentes para otra cosa.

---

## Claves en `es.json`

### Estructura

Por **dominio**, no por componente. Claves reutilizables.

```json
{
  "common":     {...},   // botones genéricos: save, cancel, delete, refresh, ...
  "nav":        {...},   // sidebar / header: home, courses, ...
  "auth":       {...},   // login, register, email, password, ...
  "course":     {...},   // detalle de curso: tabs, level, status, ...
  "lesson":     {...},   // vista de lección
  "assessment": {        // evaluaciones
    "type":     {"quiz": "Cuestionario", ...},
    "editor":   {...},
    "intro":    {...},
    "results":  {...}
  },
  "instructor": {        // panel instructor
    "wizard":   {...},
    "grading":  {...}
  },
  "admin":      {...},
  "premium":    {...},
  ...
}
```

### Convención de nombres

- `camelCase` (no snake_case ni kebab).
- Verbos en infinitivo o imperativo: `save`, `cancel`, `markCompleted`.
- Pares para acciones que pueden notificar:
  - `delete` → label del botón
  - `deletedOk` → notify positivo "Eliminado"
  - `deleteError` → notify negativo "Error al eliminar"
- Sufijos para diálogos:
  - `deleteConfirmTitle` → título
  - `deleteConfirmBody` → cuerpo

### Mantenimiento

- Si una clave queda obsoleta, **bórrala** del JSON. No dejes huérfanas.
- Si una clave es ambigua, **renómbrala** y arregla los usos.
- El JSON crece — pero no es problema mientras tenga jerarquía clara y cada clave esté justificada.

---

## Errores comunes

| Error | Solución |
|---|---|
| Falta `useI18n()` en setup | Importar y llamar |
| Clave inexistente | Verificar en `es.json` |
| Variable no pasada a interpolación | `t('xxx', { name: '...' })` |
| Pluralización con `tc` | Usar `t(key, count)` directo en v9 |
| HTML en `t()` aparece escapado | Usar `<i18n-t>` component o `v-html` con cuidado |
| Tilde en path de routing | Las URLs son ASCII; arreglar |

---

## Backend

El backend devuelve mensajes de error en castellano (hardcoded en strings de excepción). Cuando se internacionalice, se introducirá `MessageSource` de Spring con `messages_es.properties` / `messages_en.properties`.

Hoy, los strings del backend ya pasaron por el mismo sweep que el frontend, por lo que las tildes y faltas están corregidas.

---

## Multi-idioma futuro

Cuando se añada otro locale (e.g. inglés):

1. Crear `src/i18n/locales/en.json` con la misma estructura que `es.json`.
2. Añadir `en` al `messages` de `i18n/index.ts`.
3. Selector de idioma en `AppHeader.vue`.
4. Persistir elección en `localStorage` (`coursely-locale`).
5. axios envía `Accept-Language` con el locale activo.
6. Backend introduce `MessageSource` para errores localizados.

No es prioritario hoy (proyecto solo en castellano). La infraestructura está lista cuando se decida.

---

## Sweep ortográfico (histórico)

Ya hecho. Cubrió ~150 patrones de palabras castellanas comunes con sed selectivo (word-boundary). Aplicado a:

- `CourselyLabs-front/src/**/*.{vue,ts}`
- `CourselyLabs-back/src/main/java/**/*.java`
- `CourselyLabs-back/src/main/resources/*.properties`

Patrones cubiertos: sustantivos `-ción/-sión`, adjetivos comunes, verbos en futuro/condicional, adverbios, geo, demostrativos protegidos, signos de puntuación.

URLs y package imports preservados (no tocados).

---

## Referencias

- [`03-frontend/i18n.md`](../03-frontend/i18n.md) — guía técnica de `vue-i18n`
- vue-i18n v9 docs — https://vue-i18n.intlify.dev/
