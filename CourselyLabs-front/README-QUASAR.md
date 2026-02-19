# Guía de Personalización de Quasar Framework

Esta guía explica cómo personalizar Quasar Framework en el proyecto CourselyLabs.

## Tabla de Contenidos

1. [Variables de Color](#variables-de-color)
2. [Otras Variables SASS](#otras-variables-sass)
3. [Configuración de Plugins](#configuración-de-plugins)
4. [Iconos](#iconos)
5. [Componentes Comunes](#componentes-comunes)
6. [Modo Oscuro](#modo-oscuro)
7. [Configuración de Vite](#configuración-de-vite)
8. [Recursos Adicionales](#recursos-adicionales)

---

## Variables de Color

Las variables de color de Quasar se definen en [src/quasar-variables.sass](src/quasar-variables.sass).

### Colores del Proyecto

La paleta de CourselyLabs usa teal como color principal, naranja como acento y escala de grises para el resto. Para la guia completa de colores, accesibilidad y logo, ver [DESIGN.md](DESIGN.md).

```sass
$primary   : #0F766E  // Teal 700 - Header, navegacion, botones principales
$secondary : #4B5563  // Gray 600 - Elementos secundarios
$accent    : #EA580C  // Orange 600 - Detalles, CTAs, destacados

$dark      : #1F2937  // Gray 800 - Modo oscuro, texto principal
$dark-page : #111827  // Gray 900 - Fondo en modo oscuro

$positive  : #059669  // Emerald 600 - Exito
$negative  : #DC2626  // Red 600 - Error
$info      : #0891B2  // Cyan 600 - Informacion
$warning   : #D97706  // Amber 600 - Advertencia
```

### Cómo Cambiar los Colores

1. Abre [src/quasar-variables.sass](src/quasar-variables.sass)
2. Modifica los valores hexadecimales de los colores
3. Guarda el archivo (Vite recargará automáticamente)
4. Actualiza [DESIGN.md](DESIGN.md) para mantener la documentación sincronizada

### Usar el Theme Builder

Quasar ofrece una herramienta visual para generar paletas de colores:

1. Visita: https://quasar.dev/style/theme-builder
2. Ajusta los colores con los selectores visuales
3. Copia el código SASS generado
4. Pégalo en [src/quasar-variables.sass](src/quasar-variables.sass)

---

## Otras Variables SASS

Además de los colores, puedes personalizar muchos otros aspectos:

### Tipografía

```sass
// Fuentes
$typography-font-family : 'Roboto', sans-serif

// Tamaños de fuente
$h1: (size: 6rem,    line-height: 6rem,    letter-spacing: -.01562em, weight: 300)
$h2: (size: 3.75rem, line-height: 3.75rem, letter-spacing: -.00833em, weight: 300)
$h3: (size: 3rem,    line-height: 3.125rem, letter-spacing: normal,   weight: 400)
$h4: (size: 2.125rem, line-height: 2.5rem,  letter-spacing: .00735em, weight: 400)
$h5: (size: 1.5rem,  line-height: 2rem,    letter-spacing: normal,   weight: 400)
$h6: (size: 1.25rem, line-height: 2rem,    letter-spacing: .0125em,  weight: 500)
```

### Espaciado y Bordes

```sass
// Espaciado de componentes
$space-base: 16px
$space-x-base: $space-base
$space-y-base: $space-base

// Bordes redondeados
$generic-border-radius: 4px
$button-border-radius: $generic-border-radius
$card-border-radius: $generic-border-radius
```

### Sombras

```sass
// Elevación de componentes
$shadow-color: rgba(0, 0, 0, .2)
$shadow-transition: box-shadow .28s cubic-bezier(.4, 0, .2, 1)
```

### Ver Todas las Variables

Consulta la lista completa de variables personalizables en:
https://github.com/quasarframework/quasar/blob/dev/src/css/variables.sass

---

## Configuración de Plugins

Los plugins de Quasar se configuran en [src/main.ts](src/main.ts:20-22).

### Estructura Actual

```typescript
app.use(Quasar, {
  plugins: {}, // import Quasar plugins and add here
})
```

### Plugins Disponibles

#### Notify (Notificaciones)

Para mostrar notificaciones toast:

```typescript
import { Quasar, Notify } from 'quasar'

app.use(Quasar, {
  plugins: {
    Notify
  }
})
```

Uso en componentes:

```typescript
import { useQuasar } from 'quasar'

const $q = useQuasar()

$q.notify({
  message: 'Curso guardado correctamente',
  color: 'positive',
  icon: 'check_circle'
})
```

#### Dialog (Diálogos)

Para mostrar ventanas modales de confirmación:

```typescript
import { Quasar, Dialog } from 'quasar'

app.use(Quasar, {
  plugins: {
    Dialog
  }
})
```

Uso:

```typescript
import { useQuasar } from 'quasar'

const $q = useQuasar()

$q.dialog({
  title: 'Confirmar',
  message: '¿Deseas eliminar este curso?',
  cancel: true,
  persistent: true
}).onOk(() => {
  // Usuario confirmó
})
```

#### Loading (Indicador de Carga Global)

Para mostrar un spinner de carga a pantalla completa:

```typescript
import { Quasar, Loading } from 'quasar'

app.use(Quasar, {
  plugins: {
    Loading
  },
  config: {
    loading: {
      /* opciones de Loading */
    }
  }
})
```

Uso:

```typescript
import { Loading } from 'quasar'

Loading.show()
// realizar operación asíncrona
Loading.hide()
```

#### LocalStorage y SessionStorage

Para persistir datos en el navegador:

```typescript
import { Quasar, LocalStorage, SessionStorage } from 'quasar'

app.use(Quasar, {
  plugins: {
    LocalStorage,
    SessionStorage
  }
})
```

Uso:

```typescript
import { LocalStorage } from 'quasar'

LocalStorage.set('auth-token', 'xyz123')
const token = LocalStorage.getItem('auth-token')
LocalStorage.remove('auth-token')
```

### Configuración Completa Recomendada

```typescript
import { Quasar, Notify, Dialog, Loading, LocalStorage } from 'quasar'

app.use(Quasar, {
  plugins: {
    Notify,
    Dialog,
    Loading,
    LocalStorage
  },
  config: {
    notify: {
      position: 'top-right',
      timeout: 2500
    },
    loading: {
      delay: 400 // ms
    }
  }
})
```

---

## Iconos

### Iconos Actualmente Instalados

El proyecto usa Material Icons ([src/main.ts](src/main.ts:8)):

```typescript
import '@quasar/extras/material-icons/material-icons.css'
```

### Usar Iconos en Componentes

```vue
<q-btn icon="add" label="Agregar" />
<q-icon name="home" size="2rem" color="primary" />
```

Busca iconos disponibles en: https://fonts.google.com/icons

### Instalar Otras Bibliotecas de Iconos

#### Font Awesome

```bash
npm install @quasar/extras
```

```typescript
// main.ts
import '@quasar/extras/fontawesome-v6/fontawesome-v6.css'
```

Uso:

```vue
<q-icon name="fa-solid fa-heart" />
```

#### Material Design Icons (MDI)

```typescript
// main.ts
import '@quasar/extras/mdi-v7/mdi-v7.css'
```

Uso:

```vue
<q-icon name="mdi-account" />
```

#### Ionicons

```typescript
// main.ts
import '@quasar/extras/ionicons-v7/ionicons-v7.css'
```

Uso:

```vue
<q-icon name="ion-heart" />
```

### Cambiar el Set de Iconos por Defecto

En [src/main.ts](src/main.ts):

```typescript
import { Quasar } from 'quasar'
import iconSet from 'quasar/icon-set/fontawesome-v6'

app.use(Quasar, {
  plugins: {},
  iconSet: iconSet
})
```

---

## Componentes Comunes

### Lista de Componentes más Utilizados

| Componente   | Uso                          | Documentación |
|--------------|------------------------------|---------------|
| `q-btn`      | Botones                      | [Docs](https://quasar.dev/vue-components/button) |
| `q-card`     | Tarjetas de contenido        | [Docs](https://quasar.dev/vue-components/card) |
| `q-list`     | Listas de elementos          | [Docs](https://quasar.dev/vue-components/list-and-list-items) |
| `q-table`    | Tablas con paginación        | [Docs](https://quasar.dev/vue-components/table) |
| `q-input`    | Campos de texto              | [Docs](https://quasar.dev/vue-components/input) |
| `q-select`   | Selectores desplegables      | [Docs](https://quasar.dev/vue-components/select) |
| `q-checkbox` | Casillas de verificación     | [Docs](https://quasar.dev/vue-components/checkbox) |
| `q-radio`    | Botones de radio             | [Docs](https://quasar.dev/vue-components/radio) |
| `q-toggle`   | Interruptores on/off         | [Docs](https://quasar.dev/vue-components/toggle) |
| `q-dialog`   | Ventanas modales             | [Docs](https://quasar.dev/vue-components/dialog) |
| `q-drawer`   | Paneles laterales            | [Docs](https://quasar.dev/vue-components/drawer) |
| `q-tabs`     | Pestañas de navegación       | [Docs](https://quasar.dev/vue-components/tabs) |
| `q-stepper`  | Wizard de pasos              | [Docs](https://quasar.dev/vue-components/stepper) |
| `q-banner`   | Mensajes informativos        | [Docs](https://quasar.dev/vue-components/banner) |
| `q-spinner`  | Indicadores de carga         | [Docs](https://quasar.dev/vue-components/spinner) |
| `q-badge`    | Etiquetas y contadores       | [Docs](https://quasar.dev/vue-components/badge) |
| `q-chip`     | Chips (tags pequeños)        | [Docs](https://quasar.dev/vue-components/chip) |
| `q-tooltip`  | Tooltips informativos        | [Docs](https://quasar.dev/vue-components/tooltip) |
| `q-separator`| Líneas divisorias            | [Docs](https://quasar.dev/vue-components/separator) |

### Ejemplos Rápidos

#### Botón con icono

```vue
<q-btn color="primary" icon="add" label="Nuevo Curso" @click="crearCurso" />
```

#### Tarjeta de curso

```vue
<q-card>
  <q-img src="/curso.jpg" />
  <q-card-section>
    <div class="text-h6">Introducción a Vue 3</div>
    <div class="text-subtitle2">John Doe</div>
  </q-card-section>
  <q-card-section>
    Aprende Vue 3 desde cero con este curso completo.
  </q-card-section>
  <q-card-actions>
    <q-btn flat color="primary">Ver detalles</q-btn>
    <q-btn flat color="secondary">Inscribirse</q-btn>
  </q-card-actions>
</q-card>
```

#### Formulario de login

```vue
<q-form @submit="onSubmit">
  <q-input
    v-model="email"
    label="Email"
    type="email"
    lazy-rules
    :rules="[val => !!val || 'Email requerido']"
  />

  <q-input
    v-model="password"
    label="Contraseña"
    type="password"
    lazy-rules
    :rules="[val => !!val || 'Contraseña requerida']"
  />

  <q-btn type="submit" color="primary" label="Iniciar sesión" />
</q-form>
```

#### Tabla de datos

```vue
<q-table
  :rows="courses"
  :columns="columns"
  row-key="id"
  :pagination="{ rowsPerPage: 10 }"
/>

<script setup lang="ts">
const columns = [
  { name: 'title', label: 'Título', field: 'title', align: 'left' },
  { name: 'instructor', label: 'Instructor', field: 'instructor' },
  { name: 'price', label: 'Precio', field: 'price' }
]
</script>
```

---

## Modo Oscuro

### Habilitar Modo Oscuro

#### Opción 1: Modo Oscuro Automático (Sistema)

En [src/main.ts](src/main.ts):

```typescript
import { Quasar, Dark } from 'quasar'

app.use(Quasar, {
  plugins: {},
  config: {
    dark: 'auto' // true | false | 'auto'
  }
})
```

#### Opción 2: Toggle Manual

Crear un botón para alternar entre modos:

```vue
<template>
  <q-btn
    :icon="$q.dark.isActive ? 'dark_mode' : 'light_mode'"
    @click="toggleDark"
  />
</template>

<script setup lang="ts">
import { useQuasar } from 'quasar'

const $q = useQuasar()

function toggleDark() {
  $q.dark.toggle()
}
</script>
```

### Estilos Personalizados para Modo Oscuro

Usar la clase `.body--dark` para estilos condicionales:

```sass
.my-card
  background: white
  color: black

.body--dark .my-card
  background: #1D1D1D
  color: white
```

O usar la variable `$dark`:

```sass
@import 'quasar/src/css/variables.sass'

.my-element
  background: if($dark, #1D1D1D, white)
```

---

## Configuración de Vite

La configuración de Quasar en Vite se encuentra en [vite.config.ts](vite.config.ts).

### Configuración Actual

```typescript
import { quasar, transformAssetUrls } from '@quasar/vite-plugin'

export default defineConfig({
  plugins: [
    vue({
      template: { transformAssetUrls }
    }),
    quasar({
      sassVariables: fileURLToPath(new URL('./src/quasar-variables.sass', import.meta.url))
    })
  ]
})
```

### Opciones Adicionales del Plugin Quasar

#### Auto-importar Componentes

Por defecto, los componentes de Quasar se auto-importan. Si quieres desactivar esto:

```typescript
quasar({
  sassVariables: '...',
  autoImportComponentCase: 'pascal' // 'kebab' | 'pascal' | 'combined' | false
})
```

#### Configurar el SSR (Server-Side Rendering)

Si en el futuro quieres añadir SSR:

```typescript
quasar({
  sassVariables: '...',
  ssr: {
    prodPort: 3000
  }
})
```

---

## Recursos Adicionales

### Documentación Oficial

- **Sitio principal**: https://quasar.dev
- **Componentes**: https://quasar.dev/vue-components
- **Plugins**: https://quasar.dev/quasar-plugins
- **Directivas**: https://quasar.dev/vue-directives
- **Utils**: https://quasar.dev/quasar-utils
- **Estilo**: https://quasar.dev/style

### Herramientas Útiles

- **Theme Builder**: https://quasar.dev/style/theme-builder
- **Icon Picker**: https://fonts.google.com/icons
- **Playground**: https://codepen.io/quasarframework/

### Comunidad

- **Discord**: https://chat.quasar.dev
- **Forum**: https://forum.quasar.dev
- **GitHub**: https://github.com/quasarframework/quasar
- **Twitter**: @quasarframework

### Tutoriales Recomendados

- [Quasar V2 Crash Course](https://www.youtube.com/watch?v=iml3hDVboHk) (YouTube)
- [Building a Real-World App with Quasar](https://quasar.dev/start/pick-quasar-flavour)
- [Quasar Best Practices](https://quasar.dev/quasar-cli-vite/developing-ssr/preparation)

---

## Resumen de Archivos Clave

| Archivo | Propósito |
|---------|-----------|
| [src/quasar-variables.sass](src/quasar-variables.sass) | Variables de colores y estilos SASS |
| [src/main.ts](src/main.ts) | Configuración de plugins de Quasar |
| [vite.config.ts](vite.config.ts) | Configuración del plugin de Quasar para Vite |
| `src/assets/main.css` | Estilos globales personalizados |

---

Para volver a la documentación principal, consulta [README.md](README.md).
