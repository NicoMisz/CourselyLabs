# Estilo de código

> Convenciones específicas del repo. Para temas más amplios (capas, naming) ver [`04-backend/README.md`](../04-backend/README.md) y [`03-frontend/README.md`](../03-frontend/README.md).

---

## Backend (Java)

### Formato

- **4 espacios** de indentación (no tabs).
- **Llaves K&R** (apertura en la misma línea).
- **120 columnas** máximo (relajado, no estricto).
- **Imports organizados** (sin estrellas; una línea por import).

### Lombok

Usar **siempre** que reduzca boilerplate:

```java
@Entity
@Data                  // getters + setters + toString + equals + hashCode
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users")
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    private String email;
    // ...
}
```

Excepción: si necesitas constructor o métodos custom, usa `@Getter` / `@Setter` selectivos en lugar de `@Data` para no perder control.

### Excepciones

- Lanzar **excepciones tipadas** (`BadRequestException`, `ResourceNotFoundException`, `UnauthorizedException`).
- **No** lanzar `RuntimeException` genérico.
- Mensajes en **castellano** y **dirigidos al usuario** (los muestra el frontend).

```java
// Bien
throw new BadRequestException("Este bloque ya tiene una evaluación asociada");

// Mal
throw new RuntimeException("Block already has assessment");
```

### Transacciones

- En **services**, no en controllers ni repositories.
- `@Transactional(readOnly = true)` para lectura.
- `@Transactional` para escritura.

### Naming

| Concepto | Convención | Ejemplo |
|---|---|---|
| Clases | PascalCase | `LessonBlockService` |
| Métodos | camelCase, verbos imperativos | `createForBlock`, `findByLessonId` |
| Variables | camelCase | `currentAttempt` |
| Constantes | UPPER_SNAKE_CASE | `MAX_SUBMISSION_BYTES` |
| Tablas SQL | snake_case plural | `lesson_blocks`, `course_prerequisites` |
| Columnas SQL | snake_case | `created_at`, `last_position_seconds` |

### Comentarios

- **Solo cuando el "porqué" no es obvio** del código.
- **No** comentarios que repiten lo que el código ya dice.
- **Javadoc** para clases públicas y métodos no triviales.

```java
// Bien
/** Devuelve la primera evaluación del bloque. Con multi-bloque (V11), un bloque tiene 1:1 con assessment. */
Optional<AssessmentEntity> findByBlockId(UUID blockId);

// Mal
// Busca por block id
Optional<AssessmentEntity> findByBlockId(UUID blockId);
```

### Ejemplo de service típico

```java
@Service
@Transactional
public class XxxService {

    private final XxxRepository repository;
    private final YyyService yyyService;

    public XxxService(XxxRepository repository, YyyService yyyService) {
        this.repository = repository;
        this.yyyService = yyyService;
    }

    @Transactional(readOnly = true)
    public XxxDTO findById(UUID id) {
        XxxEntity entity = repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Xxx", "id", id));
        return toDTO(entity);
    }

    public XxxDTO create(XxxDTO dto, String email) {
        // Validaciones
        if (dto.getX() == null) {
            throw new BadRequestException("X es obligatorio");
        }

        // Lógica
        XxxEntity entity = new XxxEntity();
        entity.setX(dto.getX());
        return toDTO(repository.save(entity));
    }

    private XxxDTO toDTO(XxxEntity entity) {
        XxxDTO dto = new XxxDTO();
        dto.setId(entity.getId());
        dto.setX(entity.getX());
        return dto;
    }
}
```

---

## Frontend (Vue 3 + TypeScript)

### Formato

- **2 espacios** de indentación.
- **Single quotes** en TS (`'`), **double quotes** en HTML (`"`).
- **No semicolons** (ESLint lo enforça).
- **120 columnas** máximo (relajado).

### `<script setup>`

Siempre usar la sintaxis Composition API con `<script setup lang="ts">`. **No** usar Options API.

```vue
<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute } from 'vue-router'
import { useQuasar } from 'quasar'
import { someApi } from '@/api/xxx'
import type { Xxx } from '@/types/xxx'

const props = defineProps<{ id: string }>()
const emit = defineEmits<{ (e: 'updated', x: Xxx): void }>()

const { t } = useI18n()
const route = useRoute()
const $q = useQuasar()

// State
const data = ref<Xxx | null>(null)
const loading = ref(true)

// Computed
const isReady = computed(() => !loading.value && !!data.value)

// Methods
async function load() {
  loading.value = true
  try {
    data.value = await someApi.fetch(props.id)
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<template>
  <div>
    <!-- ... -->
  </div>
</template>

<style scoped>
.x { /* ... */ }
</style>
```

### Naming

| Concepto | Convención | Ejemplo |
|---|---|---|
| Componentes (`.vue`) | PascalCase | `LessonBlockEditor.vue` |
| Composables | camelCase con `use` prefix | `useAuth`, `useCurrentCourse` |
| Stores Pinia | camelCase con `use` prefix + `Store` suffix | `useAuthStore` |
| Funciones | camelCase, verbos | `loadCourse`, `handleSubmit` |
| Tipos / interfaces | PascalCase | `Course`, `LessonBlock` |
| Eventos emitidos | camelCase | `@updated`, `@itemSelected` |

### Strings de UI

- **Castellano correcto** (con tildes, signos `¿…?`, `«…»`).
- **`vue-i18n` con `t()`** — siempre en componentes nuevos. Ver [`i18n-policy.md`](i18n-policy.md).

### Imports

Orden:
1. Vue / Vue Router / Pinia / vue-i18n.
2. Quasar.
3. Librerías externas.
4. Imports del proyecto (`@/`).
5. Tipos (`import type`).

```ts
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useQuasar } from 'quasar'
import { useI18n } from 'vue-i18n'
import draggable from 'vuedraggable'

import { listBlocks, createBlock } from '@/api/lessonBlock'
import LessonBlockEditor from '@/components/LessonBlockEditor.vue'

import type { LessonBlock, BlockType } from '@/types/lesson'
```

### Composición de componentes

- **Single File Components** (`<template>` + `<script setup>` + `<style scoped>`).
- **Estilos `scoped`** salvo casos puntuales.
- **Props tipadas** con `defineProps<{...}>()` (no la API runtime).
- **Emits tipadas** con `defineEmits<{ ... }>()`.
- **Slots** documentados con comentarios cuando hace falta.

### Tamaño

- Si un componente pasa de **~400-500 líneas**, considera dividirlo.
- Si una función pasa de **~60-80 líneas**, considera extraerla.
- `CourseWizard.vue` y `LessonView.vue` son excepciones que **deberían refactorizarse**, no ejemplos a imitar.

### Patrones de fetch

```ts
const data = ref<X | null>(null)
const loading = ref(true)
const error = ref('')

async function load() {
  loading.value = true
  error.value = ''
  try {
    data.value = await someApi.fetch(id.value)
  } catch (err: any) {
    error.value = err?.response?.data?.message || 'Error cargando datos'
  } finally {
    loading.value = false
  }
}
```

---

## SQL / Migraciones

Ver [`04-backend/migrations.md`](../04-backend/migrations.md). Resumen:

- snake_case en nombres.
- Mayúsculas en palabras reservadas (`SELECT`, `INSERT`, `WHERE`, `JOIN`).
- Tipos PostgreSQL nativos.
- `gen_random_uuid()` para PKs UUID con default.
- Constraint nombrados explícitamente cuando sea referenciable.
- Comentario al inicio del archivo de migración.

---

## CSS

- **Quasar variables** definidas en `src/quasar-variables.sass`.
- **CSS custom** en `<style scoped>` por componente.
- **No Tailwind** — Quasar provee utilidades suficientes (`q-pa-md`, `text-grey-7`, `row`, `col`, etc.).
- **Sin `!important`** salvo casos extremos justificados con comentario.

---

## Tools

### Backend
- **Maven Wrapper** (`./mvnw`) — no instales Maven globalmente.
- **Spring Boot DevTools** activado en dev → reload automático con cambios.

### Frontend
- **ESLint + Prettier** configurados. Ejecuta `npm run lint` y `npm run format` antes de commit.
- **vue-tsc** estricto en `npm run type-check`.
- **Vitest** para tests unitarios.

---

## Lo que NO está estandarizado todavía (TODO)

- Pre-commit hooks (Husky + lint-staged).
- Conventional commits enforced via commitlint.
- Coverage thresholds en tests.
- Auto-formato en CI.
- Generación automática de OpenAPI desde el código backend.
