# Changelog

Todos los cambios relevantes del proyecto se documentan en este archivo.

El formato se basa en [Keep a Changelog](https://keepachangelog.com/es/1.1.0/).

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
