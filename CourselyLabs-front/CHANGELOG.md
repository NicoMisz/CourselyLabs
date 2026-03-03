# Changelog

Todos los cambios relevantes del proyecto se documentan en este archivo.

El formato se basa en [Keep a Changelog](https://keepachangelog.com/es/1.1.0/).

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
