# Gestión de estado

> Stores Pinia y patrones de estado en el frontend. La mayoría del estado vive en componentes; solo lo verdaderamente global está en Pinia.

---

## Stores Pinia disponibles

### `useAuthStore` (`src/stores/auth.ts`)

El único store global del proyecto. Gestiona sesión y datos del usuario.

**Estado**:
- `accessToken: string | null` — JWT corto (15 min). Persistido en `localStorage`.
- `refreshToken: string | null` — JWT largo (7 días). Persistido en `localStorage`.
- `user: User | null` — datos del usuario (id, email, role, firstName, lastName, …). Persistido.

**Computed**:
- `isLoggedIn` — `!!accessToken && !!user`.
- `userRole` — `user?.role ?? null`.

**Acciones**:
- `login(credentials)` — POST `/api/auth/login` y guarda sesión.
- `register(payload)` — POST `/api/auth/register` y guarda sesión.
- `logout()` — limpia local + POST `/api/auth/logout` para invalidar el refresh en BD.
- `updateUser(user)` — actualiza el user en estado y localStorage tras editar perfil.
- `checkSession()` — al arrancar la app, intenta refrescar con el refresh token guardado.

**Integración con axios**: el store registra callbacks (`setSessionCallbacks`) que el interceptor de axios invoca cuando refresca o expira el token. Esto mantiene Pinia sincronizado con lo que pasa en la red sin acoplarlos.

---

## Por qué solo hay un store

Decisión consciente:

- **Cursos, lecciones, evaluaciones, etc.**: estado del servidor — vive en el backend, lo cargamos bajo demanda. No tiene sentido cachearlo en Pinia salvo casos puntuales (sería caché frágil).
- **Estado UI** (sidebar abierto, dialogs, formularios): vive en el componente con `ref`/`reactive`. No es global.
- **Auth**: sí es global porque cualquier ruta puede necesitar saber si hay sesión y quién es el usuario.

Si en el futuro hace falta caché de servidor con invalidación (ej. lista de cursos), considera **TanStack Query** (Vue Query) en lugar de un store Pinia. Más limpio para ese caso.

---

## Persistencia en `localStorage`

Solo el `useAuthStore` persiste:

| Clave | Contenido |
|---|---|
| `accessToken` | JWT acceso |
| `refreshToken` | JWT refresh |
| `user` | JSON de `User` |

Al arrancar la app (`main.ts`), se llama a `authStore.checkSession()` que:
1. Lee `refreshToken` de localStorage.
2. Si existe, hace POST `/api/auth/refresh` para obtener un nuevo access token.
3. Si refresca con éxito, deja el usuario logueado.
4. Si falla, limpia todo (`clearSession()`) — sesión expirada o tokens inválidos.

---

## Cómo usar el store en un componente

```vue
<script setup lang="ts">
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()

// Reactive: cambia automáticamente si el usuario cambia
const isLoggedIn = computed(() => authStore.isLoggedIn)
const userName = computed(() => authStore.user?.firstName)

async function handleLogin() {
  try {
    await authStore.login({ email: 'a@b.com', password: 'xxx' })
    router.push('/')
  } catch (err) {
    // ...
  }
}
</script>
```

---

## Patrones de estado dentro de componentes

### Cargar datos del servidor

```vue
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getCourseBySlug } from '@/api/course'
import type { Course } from '@/types/course'

const course = ref<Course | null>(null)
const loading = ref(true)
const error = ref('')

async function load() {
  loading.value = true
  error.value = ''
  try {
    course.value = await getCourseBySlug(slug.value)
  } catch (err: any) {
    error.value = err?.response?.data?.message || 'Error cargando el curso'
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>
```

Patrón estándar:
- `ref` para los datos.
- `ref<boolean>` para loading.
- `ref<string>` para error.
- Función `load()` async con try/finally.
- `onMounted(load)` o llamada manual.

### Reaccionar a cambios de ruta

```vue
import { watch } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
watch(() => route.params.slug, (newSlug) => {
  load()
})
```

### Estado UI local (sin Pinia)

```vue
<script setup lang="ts">
import { ref, reactive } from 'vue'

const drawerOpen = ref(true)
const dialog = ref(false)
const form = reactive({ title: '', description: '' })
</script>
```

---

## Cuándo crear un nuevo store

Crea un store Pinia solo si:

1. El estado es **realmente global** (varias views distintas lo necesitan al mismo tiempo).
2. El estado **persiste entre navegaciones** sin recarga (ej. carrito de compra, no aplica aquí).
3. Hay **lógica compartida** que tiene sentido encapsular (ej. lógica de pago).

Para fetch de datos puntuales: usa `api/*.ts` directamente desde el componente.

---

## TODO conocidos

- Falta un store o composable centralizado para "isPremium" — hoy se llama `checkIsPremium()` desde varios sitios. Sería útil tenerlo cacheado durante la sesión.
- El `accessToken` se vence cada 15 min y axios refresca automáticamente; no hay UI de "tu sesión expira pronto" para casos edge.
