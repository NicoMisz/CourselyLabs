# CourselyLabs - Guia de Diseno

Documento de referencia para colores, tipografia, logo y estilo visual de la plataforma.

---

## 1. Paleta de Colores

### Principios

- **Teal/Turquesa** como color principal (header, navegacion, botones primarios)
- **Naranja** como color de acento (detalles, CTAs secundarios, destacados)
- **Escala de grises** para el resto (texto, fondos, bordes)
- Todos los colores cumplen **WCAG AA** (contraste minimo 4.5:1 sobre blanco para texto)

### Colores Principales

#### Primary - Teal (Header y Navegacion)

| Uso             | Color     | Hex       | Contraste vs blanco |
|-----------------|-----------|-----------|---------------------|
| Primary dark    | Teal 800  | `#115E59` | 7.3:1               |
| **Primary**     | Teal 700  | `#0F766E` | 5.5:1               |
| Primary light   | Teal 500  | `#14B8A6` | 2.8:1 (solo fondos) |
| Primary surface | Teal 50   | `#F0FDFA` | fondo               |

#### Accent - Naranja (Detalles y Destacados)

| Uso            | Color      | Hex       | Contraste vs blanco |
|----------------|------------|-----------|---------------------|
| Accent dark    | Orange 700 | `#C2410C` | 4.8:1               |
| **Accent**     | Orange 600 | `#EA580C` | 3.8:1 (iconos/lg)   |
| Accent light   | Orange 400 | `#FB923C` | 2.3:1 (solo fondos) |
| Accent surface | Orange 50  | `#FFF7ED` | fondo               |

#### Escala de Grises (Texto, Fondos, Bordes)

| Uso                | Color    | Hex       | Aplicacion                    |
|--------------------|----------|-----------|-------------------------------|
| Texto principal    | Gray 800 | `#1F2937` | Titulos, texto body           |
| Texto secundario   | Gray 600 | `#4B5563` | Subtitulos, descripciones     |
| Texto desactivado  | Gray 400 | `#9CA3AF` | Placeholders, texto inactivo  |
| Bordes             | Gray 300 | `#D1D5DB` | Lineas, separadores, bordes   |
| Fondo tarjetas     | Gray 100 | `#F3F4F6` | Cards, secciones              |
| Fondo pagina       | Gray 50  | `#F9FAFB` | Background general            |
| Blanco             | White    | `#FFFFFF` | Cards, modals, inputs         |

### Colores Funcionales

| Funcion      | Hex       | Uso                            |
|--------------|-----------|--------------------------------|
| Positivo     | `#059669` | Exito, completado, aprobado    |
| Negativo     | `#DC2626` | Error, eliminado, suspendido   |
| Info         | `#0891B2` | Informacion, tips, ayuda       |
| Warning      | `#D97706` | Advertencia, atencion          |

### Mapeo a Variables Quasar

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

---

## 2. Tipografia

### Fuentes

| Fuente    | Tipo     | Uso                                   | Archivo                           |
|-----------|----------|---------------------------------------|-----------------------------------|
| Archivo   | Variable | Fuente principal (body, UI)           | `src/assets/fonts/Archivo/`       |
| Monda     | Variable | Titulos, logo, encabezados destacados | `src/assets/fonts/Monda/`         |

### Jerarquia Tipografica

| Elemento        | Fuente  | Peso | Tamano   |
|-----------------|---------|------|----------|
| Logo            | Monda   | 700  | -        |
| H1              | Monda   | 700  | 2.5rem   |
| H2              | Monda   | 600  | 2rem     |
| H3              | Archivo | 600  | 1.5rem   |
| H4              | Archivo | 600  | 1.25rem  |
| Body            | Archivo | 400  | 1rem     |
| Body small      | Archivo | 400  | 0.875rem |
| Botones         | Archivo | 600  | 0.875rem |
| Captions        | Archivo | 400  | 0.75rem  |

---

## 3. Logo

### Concepto

El logo de **CourselyLabs** combina un **libro abierto** fusionado con un **dispositivo digital** (laptop o movil), representando la union entre educacion tradicional y tecnologia.

### Descripcion Visual

```
  ___________
 /           \        Libro abierto cuyas paginas
|   /|   |\   |      se transforman en la pantalla
|  / |   | \  |      de un laptop/movil
| /  |   |  \ |
|/___|___|___\|
 \___|___|___/        La base del libro es la base del laptop
     |___|

  CourselyLabs
```

### Elementos del Logo

1. **Libro abierto**: Parte izquierda, paginas visibles, simboliza conocimiento y aprendizaje
2. **Pantalla digital**: Parte derecha, las paginas se funden con una pantalla, simboliza tecnologia y acceso online
3. **Texto "CourselyLabs"**: Debajo o al lado del icono

### Variantes del Logo

| Variante        | Uso                                     |
|-----------------|-----------------------------------------|
| Logo completo   | Header, landing page, documentos        |
| Solo icono      | Favicon, app icon, espacios reducidos   |
| Monocromo       | Fondos con color, impresion B/N         |
| Invertido       | Sobre fondos oscuros                    |

### Colores del Logo

| Elemento        | Color principal        | Alternativa        |
|-----------------|------------------------|--------------------|
| Icono libro     | `#0F766E` (Teal 700)  | `#FFFFFF` (blanco) |
| Icono pantalla  | `#EA580C` (Orange 600) | `#FFFFFF` (blanco) |
| Texto "Coursely"| `#1F2937` (Gray 800)  | `#FFFFFF` (blanco) |
| Texto "Labs"    | `#EA580C` (Orange 600) | `#14B8A6` (Teal)  |

### Tipografia del Logo

- **"Coursely"**: Monda Bold (700)
- **"Labs"**: Monda Bold (700) en color accent (naranja)
- Alternativa: todo en Monda Bold, con "Labs" diferenciado por color

### Tamanos Minimos

| Contexto     | Tamano minimo icono | Tamano minimo con texto |
|--------------|---------------------|-------------------------|
| Pantalla     | 24x24px             | 120px ancho             |
| Impresion    | 10mm                | 30mm ancho              |

### Espacio de Respeto

El logo debe tener un margen libre alrededor igual a la altura de la letra "C" del logotipo.

---

## 4. Componentes UI - Guia de Estilo

### Botones

| Tipo        | Fondo               | Texto     | Uso                        |
|-------------|----------------------|-----------|----------------------------|
| Primario    | `#0F766E` (Teal)    | `#FFFFFF` | Accion principal           |
| Secundario  | `transparent`        | `#0F766E` | Accion secundaria (outline)|
| Acento      | `#EA580C` (Orange)  | `#FFFFFF` | CTA, promociones           |
| Desactivado | `#D1D5DB` (Gray300) | `#9CA3AF` | Inactivo                   |

### Cards

- Fondo: `#FFFFFF`
- Borde: `1px solid #D1D5DB`
- Sombra: `0 1px 3px rgba(0,0,0,0.1)`
- Border radius: `8px`
- Padding: `1.5rem`

### Header / Navbar

- Fondo: `#0F766E` (Teal 700)
- Texto: `#FFFFFF`
- Logo: Monda Bold, blanco
- Links activos: `#FFFFFF` con underline
- Links inactivos: `rgba(255,255,255,0.8)`

### Footer

- Fondo: `#1F2937` (Gray 800)
- Texto: `#D1D5DB` (Gray 300)
- Links: `#14B8A6` (Teal 500)

---

## 5. Accesibilidad

### Contraste Minimo (WCAG AA)

- Texto normal (< 18px): ratio minimo **4.5:1**
- Texto grande (>= 18px bold o >= 24px): ratio minimo **3:1**
- Iconos y graficos: ratio minimo **3:1**

### Combinaciones Verificadas

| Texto sobre       | Color texto           | Ratio  | Estado  |
|--------------------|-----------------------|--------|---------|
| Blanco             | `#0F766E` (Primary)   | 5.5:1  | AA      |
| Blanco             | `#1F2937` (Dark)      | 13.1:1 | AAA     |
| Blanco             | `#4B5563` (Secondary) | 7.2:1  | AAA     |
| `#0F766E` (Teal)   | `#FFFFFF` (White)     | 5.5:1  | AA      |
| `#1F2937` (Dark)   | `#FFFFFF` (White)     | 13.1:1 | AAA     |
| `#F9FAFB` (Gray50) | `#1F2937` (Dark)      | 12.5:1 | AAA     |
| `#F9FAFB` (Gray50) | `#EA580C` (Orange)    | 3.9:1  | AA (lg) |

### Notas de Accesibilidad

- El naranja accent (`#EA580C`) solo debe usarse para texto grande (>= 18px bold) o iconos
- Para texto naranja pequeno sobre blanco, usar `#C2410C` (Orange 700, ratio 4.8:1)
- Siempre proporcionar indicadores visuales ademas del color (iconos, subrayado, forma)

---

## 6. Referencias

- [Quasar Theme Builder](https://quasar.dev/style/theme-builder)
- [WebAIM Contrast Checker](https://webaim.org/resources/contrastchecker/)
- [Tailwind CSS Colors](https://tailwindcss.com/docs/colors) (base para la paleta)
- [WCAG 2.1 Guidelines](https://www.w3.org/WAI/WCAG21/quickref/)
