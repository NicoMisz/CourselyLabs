-- Seed: Curso completo de Flamenco
-- Incluye: categoria, curso, instructor, secciones, lecciones, enrollments, reviews, progreso

DO $$
DECLARE
  cat_id       INTEGER;
  course_id    UUID;
  instructor UUID; -- Carmen Vargas (instructora de flamenco)
  student1   UUID; -- Maria Lopez
  student2   UUID; -- Sara Martin
  enroll1      UUID;
  enroll2      UUID;
  s1 UUID; s2 UUID; s3 UUID; s4 UUID; s5 UUID;
  l1 UUID; l2 UUID; l3 UUID; l4 UUID; l5 UUID;
  l6 UUID; l7 UUID; l8 UUID; l9 UUID; l10 UUID;
  l11 UUID; l12 UUID; l13 UUID; l14 UUID;
BEGIN

-- Crear usuaria instructora específica de flamenco si no existe.
-- Password: admin123 (mismo hash bcrypt que los demás seeds).
INSERT INTO users (email, password_hash, first_name, last_name, role, bio, is_verified, is_active)
VALUES (
    'carmen.vargas@cursos.com',
    '$2b$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5lMjH.UPA4C.m',
    'Carmen', 'Vargas', 'user',
    'Bailaora y profesora de flamenco con 20 años de experiencia. Formada en la Bienal de Sevilla.',
    TRUE, TRUE
)
ON CONFLICT (email) DO NOTHING;

SELECT id INTO instructor FROM users WHERE email = 'carmen.vargas@cursos.com';
SELECT id INTO student1 FROM users WHERE email = 'student@cursos.com';
SELECT id INTO student2 FROM users WHERE email = 'sara.martin@cursos.com';

-- ==========================================
-- 1. CATEGORIA
-- ==========================================
INSERT INTO categories (name, slug)
VALUES ('Musica', 'musica')
ON CONFLICT DO NOTHING;

SELECT id INTO cat_id FROM categories WHERE slug = 'musica';

-- ==========================================
-- 2. CURSO
-- ==========================================
INSERT INTO courses (id, title, slug, description, short_description, category_id, level, is_free, price, status, is_published, published_at, total_students, average_rating, created_by)
VALUES (
  gen_random_uuid(),
  'Arte Flamenco: De los origenes al escenario',
  'arte-flamenco',
  'Un recorrido completo por el flamenco: su historia, los palos fundamentales, tecnica de guitarra, cante y baile. Aprende a sentir el compas, entender la estructura de cada palo y desarrollar tu propia expresion flamenca. Incluye ejemplos practicos, partituras y ejercicios progresivos.',
  'Descubre el flamenco: historia, palos, guitarra, cante y baile',
  cat_id,
  'beginner',
  true,
  NULL,
  'published',
  true,
  now() - interval '15 days',
  2,
  0.00,
  instructor
)
RETURNING id INTO course_id;

-- ==========================================
-- 3. INSTRUCTOR
-- ==========================================
INSERT INTO course_instructors (course_id, instructor_id, is_main)
VALUES (course_id, instructor, true)
ON CONFLICT DO NOTHING;

-- ==========================================
-- 4. SECCIONES Y LECCIONES
-- ==========================================

-- Seccion 1: Origenes e historia
INSERT INTO sections (id, course_id, title, description, position)
VALUES (gen_random_uuid(), course_id, 'Origenes e historia del flamenco', 'Contexto historico, influencias culturales y evolucion del arte flamenco.', 0)
RETURNING id INTO s1;

INSERT INTO lessons (id, section_id, title, description, type, content_text, duration, position, is_free)
VALUES (gen_random_uuid(), s1, 'Raices del flamenco', 'Las culturas que dieron forma al flamenco.', 'text',
'# Raices del flamenco

El flamenco nace en Andalucia como resultado de la fusion de multiples culturas a lo largo de siglos.

## Influencias principales

- **Cultura gitana**: Los gitanos llegaron a la peninsula iberica en el siglo XV y aportaron su tradicion musical oral, la intensidad emotiva y la improvisacion.
- **Herencia arabe**: Ocho siglos de presencia musulmana dejaron escalas musicales (modo frigio), ornamentacion vocal y la estetica del arabesco.
- **Tradicion judia**: Los cantos sinagogales sefardies influyeron en la melismatica del cante jondo.
- **Folklore castellano**: Romances, seguidillas y jotas se integraron en el repertorio.
- **Influencia africana**: Ritmos y percusion corporal que llegaron a traves del comercio con Africa.

## La fragua del arte

El flamenco como lo conocemos se fraguo en la Baja Andalucia — Cadiz, Jerez, Sevilla, los Puertos — en comunidades marginales donde gitanos, moriscos y clases populares compartian penas y fiestas.

> *"El flamenco no se aprende, se vive."* — Dicho popular

## Cronologia basica

| Epoca | Hito |
|-------|------|
| s. XV | Llegada de los gitanos a Andalucia |
| s. XVIII | Primeras referencias escritas al flamenco |
| 1842-1910 | Edad de Oro: cafes cantantes |
| 1920-1955 | Opera flamenca y transformacion |
| 1955-hoy | Renacimiento y fusion |', 360, 0, true)
RETURNING id INTO l1;

INSERT INTO lessons (id, section_id, title, description, type, content_text, duration, position, is_free)
VALUES (gen_random_uuid(), s1, 'Los cafes cantantes y la Edad de Oro', 'El periodo que definio el flamenco profesional.', 'text',
'# Los cafes cantantes (1842-1910)

## El nacimiento del flamenco profesional

A mediados del siglo XIX, el flamenco salio de las reuniones privadas y entro en los **cafes cantantes**: locales donde se pagaba entrada para ver actuar a cantaores, guitarristas y bailaoras.

## Silverio Franconetti

Considerado el primer gran cantaor profesional. Abrio su cafe cantante en Sevilla en 1881 y elevo el cante a categoria artistica.

- Recupero palos antiguos que estaban desapareciendo
- Fusiono estilos gitanos y no gitanos (payos)
- Creo escuela y formo a la siguiente generacion

## Caracteristicas de la epoca

1. **Profesionalizacion**: El flamenco pasa de arte familiar a espectaculo pagado
2. **Codificacion**: Los palos se definen y clasifican
3. **La guitarra gana protagonismo**: Deja de ser mero acompanamiento
4. **El baile se estiliza**: Las bailaoras desarrollan tecnica propia

## Palos que se consolidan en esta epoca

- Siguiriyas
- Soleares
- Tangos
- Alegrias
- Bulerias (al final del periodo)

## El debate: pureza vs. evolucion

Ya en esta epoca surgio el debate que aun hoy existe:

> ¿El flamenco debe mantenerse "puro" o evolucionar con los tiempos?

Ambas posturas han enriquecido el arte.', 300, 1, true)
RETURNING id INTO l2;

INSERT INTO lessons (id, section_id, title, description, type, content_text, duration, position, is_free)
VALUES (gen_random_uuid(), s1, 'El flamenco hoy: fusion y patrimonio', 'Estado actual y reconocimiento de la UNESCO.', 'text',
'# El flamenco en el siglo XXI

## Patrimonio Inmaterial de la Humanidad

En **noviembre de 2010**, la UNESCO declaro el flamenco como Patrimonio Cultural Inmaterial de la Humanidad. Este reconocimiento supuso:

- Mayor visibilidad internacional
- Programas de conservacion y ensenanza
- Impulso al turismo cultural en Andalucia

## Corrientes actuales

### Flamenco puro
Artistas que mantienen la tradicion ortodoxa. Referentes:
- **Tomas de Perrate** (cante)
- **Diego del Morao** (guitarra)

### Nuevo flamenco
Fusion con pop, rock, jazz y electronica:
- **Rosalia** — flamenco + pop experimental
- **Niño de Elche** — flamenco + arte conceptual

### Flamenco jazz
- **Chick Corea** con **Paco de Lucia**
- **Jorge Pardo** (flauta y saxo flamenco)

## El flamenco en el mundo

El flamenco se estudia y practica en:
- **Japon**: Mas de 600 academias de flamenco
- **Estados Unidos**: Festivales en Nueva York y Nuevo Mexico
- **Alemania**: Fuerte escena en Berlin
- **Francia**: Festivales en Nimes y Mont-de-Marsan

## Datos curiosos

- Hay mas academias de flamenco en Japon que en Espana
- El festival mas grande es la Bienal de Flamenco de Sevilla (cada 2 anos)
- Paco de Lucia vendio mas de 20 millones de discos', 240, 2, false)
RETURNING id INTO l3;

-- Seccion 2: El compas flamenco
INSERT INTO sections (id, course_id, title, description, position)
VALUES (gen_random_uuid(), course_id, 'El compas flamenco', 'Entender el ritmo: la base de todo en el flamenco.', 1)
RETURNING id INTO s2;

INSERT INTO lessons (id, section_id, title, description, type, content_text, duration, position, is_free)
VALUES (gen_random_uuid(), s2, 'Que es el compas', 'El concepto de compas y por que es fundamental.', 'text',
'# El compas flamenco

## Definicion

El **compas** es el patron ritmico que estructura cada palo flamenco. Es la columna vertebral del flamenco: sin compas, no hay flamenco.

## Diferencia con el ritmo clasico

En la musica clasica, el ritmo es regular y predecible. En el flamenco:

- Los **acentos** caen en sitios inesperados
- El **silencio** es tan importante como el sonido
- La **libertad** del interprete convive con la **estructura** del compas

## Los dos grandes grupos

### Compas binario (2/4 o 4/4)
- **Tangos**: 1 2 3 **4** | 1 2 3 **4**
- **Rumbas**: 1 2 3 **4** | 1 2 3 **4**
- **Tientos**: mas lento que tangos, mismo compas

### Compas de 12 tiempos
El mas caracteristico del flamenco. Los acentos son:

```
1  2  3  4  5  6  7  8  9  10  11  12
         •     •     •  •       •
```

Los acentos principales caen en: **3, 6, 8, 10, 12**

Palos en compas de 12:
- Soleares
- Alegrias
- Bulerias
- Siguiriyas (con variacion)

## Ejercicio practico

Palmea este patron de 12 tiempos. Los tiempos en **negrita** son las palmadas fuertes:

1 - 2 - **3** - 4 - 5 - **6** - 7 - **8** - 9 - **10** - 11 - **12**

Repite hasta que sea natural. ¡No corras!', 420, 0, false)
RETURNING id INTO l4;

INSERT INTO lessons (id, section_id, title, description, type, content_text, duration, position, is_free)
VALUES (gen_random_uuid(), s2, 'Palmas y jaleo', 'Tecnica de palmas y el arte de jalear.', 'text',
'# Palmas y jaleo

## Tipos de palmas

### Palmas sordas
- Manos huecas, sonido apagado
- Se usan en momentos intimos o de cante jondo
- Tecnica: palma contra palma con las manos ligeramente curvadas

### Palmas fuertes (claras)
- Manos planas, sonido seco y fuerte
- Para momentos de alegria y fiesta
- Tecnica: dedos de una mano golpean la palma de la otra

### Nudillos
- Golpear la mesa o superficie con los nudillos
- Efecto percusivo complementario

## Patrones basicos

### Patron de tangos (4 tiempos)
```
Fuerte - suave - suave - Fuerte
PA     - pa    - pa    - PA
```

### Patron de bulerias (12 tiempos)
```
1    2    3    4    5    6    7    8    9    10   11   12
pa   pa   PA   pa   pa   PA   pa   PA   pa   PA   pa   PA
```

## El jaleo

**Jalear** es animar al artista con expresiones vocales:

| Expresion | Cuando usarla |
|-----------|---------------|
| ¡Ole! | Tras un remate o momento cumbre |
| ¡Eso es! | Cuando el artista esta inspirado |
| ¡Toma! | Golpe ritmico especialmente bueno |
| ¡Agua! | Cuando el cante es muy sentido |
| ¡Vamos alla! | Para animar a empezar |

## Regla de oro

> Las palmas **nunca** deben tapar al cantaor ni al guitarrista. Acompanan, no compiten.', 300, 1, false)
RETURNING id INTO l5;

-- Seccion 3: Los palos fundamentales
INSERT INTO sections (id, course_id, title, description, position)
VALUES (gen_random_uuid(), course_id, 'Los palos fundamentales', 'Analisis de los palos mas importantes del flamenco.', 2)
RETURNING id INTO s3;

INSERT INTO lessons (id, section_id, title, description, type, content_text, duration, position, is_free)
VALUES (gen_random_uuid(), s3, 'Soleares: la madre del flamenco', 'El palo mas completo y base de muchos otros.', 'text',
'# Soleares

## El palo madre

La solea (o soleares en plural) es considerada la **madre del cante flamenco**. De ella derivan muchos otros palos.

## Caracteristicas

| Aspecto | Detalle |
|---------|---------|
| Compas | 12 tiempos |
| Tonalidad | Modo frigio (Mi frigio habitualmente) |
| Caracter | Solemne, profundo, melancolico |
| Origen | Cadiz, Jerez, Triana (Sevilla) |
| Tempo | Moderado-lento |

## Estructura de una solea

1. **Guitarra sola** — introduce el tono y el compas (falseta de entrada)
2. **Temple** — el cantaor calienta la voz: "Ti-ri-ri-ran..."
3. **Primer tercio** — primera frase de la letra
4. **Segundo tercio** — desarrollo
5. **Tercer tercio** — resolucion, a menudo la frase mas intensa
6. **Falseta** — interludio de guitarra
7. **Se repite** con otra letra
8. **Remate final** — cierre definitivo

## Letras clasicas

> *"A mi me pueden mandar*
> *a lo que quieran mandar,*
> *menos olvidar tu cara*
> *que eso no lo pueo lograr."*

## Estilos de soleares

- **Solea de Cadiz**: mas ritmica, allegre
- **Solea de Triana**: ornamentada, melodica
- **Solea de Jerez**: jonda, austera
- **Solea apolá**: intermedia entre solea y polo', 480, 0, false)
RETURNING id INTO l6;

INSERT INTO lessons (id, section_id, title, description, type, content_text, duration, position, is_free)
VALUES (gen_random_uuid(), s3, 'Bulerias: la fiesta flamenca', 'El palo mas rapido y festivo.', 'text',
'# Bulerias

## El palo rey de la fiesta

La buleria es el palo mas rapido, libre y festivo del flamenco. Es el broche de oro de toda juerga flamenca.

## Caracteristicas

| Aspecto | Detalle |
|---------|---------|
| Compas | 12 tiempos (el mas rapido) |
| Tonalidad | Variada (mayor y menor) |
| Caracter | Alegre, pícaro, virtuoso |
| Origen | Jerez de la Frontera |
| Tempo | Rapido a muy rapido |

## El compas de bulerias

El mismo ciclo de 12 tiempos que la solea, pero mucho mas rapido y con acentos mas marcados:

```
12  1  2  3  4  5  6  7  8  9  10  11
PA      PA      PA  PA      PA
```

Nota: en bulerias se suele empezar a contar desde el **12**.

## Tipos de bulerias

### Buleria al golpe
- Tempo moderado
- Mas espacio para el cante
- Cercana a la solea

### Buleria ligera
- Tempo rapido
- Mucha improvisacion
- La mas comun en fiestas

### Buleria canastera
- De los gitanos canasteros de Granada
- Sabor especial, muy ritmica

## Artistas referentes

- **La Paquera de Jerez** — "La reina de las bulerias"
- **Camaron de la Isla** — Revoluciono el genero
- **Tomatito** — Guitarra virtuosa por bulerias

## Consejo

> Las bulerias son el ultimo palo que se domina. No tengas prisa. Un buen aficionado lleva anos solo para palmear bien por bulerias.', 420, 1, false)
RETURNING id INTO l7;

INSERT INTO lessons (id, section_id, title, description, type, content_text, duration, position, is_free)
VALUES (gen_random_uuid(), s3, 'Alegrias: la luz de Cadiz', 'El palo festivo en modo mayor.', 'text',
'# Alegrias

## El cante de Cadiz

Las alegrias son el palo emblematico de Cadiz. Son el cante festivo por excelencia, con un caracter luminoso y optimista.

## Caracteristicas

| Aspecto | Detalle |
|---------|---------|
| Compas | 12 tiempos |
| Tonalidad | Modo mayor (habitualmente Mi mayor) |
| Caracter | Alegre, luminoso, elegante |
| Origen | Cadiz |
| Tempo | Moderado-rapido |

## Diferencia con soleares

Aunque comparten el compas de 12, las alegrias son en **modo mayor** (suena "feliz") mientras que las soleares son en **modo frigio** (suena "triste").

## Estructura

1. Salida del cantaor: *"Tirititran tran tran..."*
2. Cante principal (varias letras)
3. **Castellana** — parte intermedia mas lenta
4. **Silencio** — seccion pausada, muy caracteristica
5. **Escobilla** — seccion de zapateado en el baile
6. Bulerias de Cadiz para cerrar

## Letras tipicas

> *"Dicen que no nos queremos*
> *porque no nos ven hablar;*
> *a tu corazon y al mio*
> *se lo pueden preguntar."*

## El silencio de alegrias

Es la seccion mas magica: la guitarra toca una melodia suave, el baile se vuelve lento y expresivo, y todo se detiene un instante antes de volver a explotar con el zapateado.', 360, 2, false)
RETURNING id INTO l8;

INSERT INTO lessons (id, section_id, title, description, type, content_text, duration, position, is_free)
VALUES (gen_random_uuid(), s3, 'Siguiriyas: el cante jondo', 'El palo mas profundo y dramatico.', 'text',
'# Siguiriyas

## El grito del alma

La siguiriya es el palo mas **jondo** (profundo) del flamenco. Expresa dolor, perdida y desgarro. Es el cante de la pena negra.

## Caracteristicas

| Aspecto | Detalle |
|---------|---------|
| Compas | 12 tiempos (con distribucion diferente) |
| Tonalidad | Modo frigio |
| Caracter | Tragico, desgarrador, solemne |
| Origen | Cadiz, los Puertos, Jerez |
| Tempo | Lento |

## El compas de siguiriyas

Aunque tiene 12 tiempos como la solea, los acentos caen de forma diferente:

```
1  2  3  4  5  6  7  8  9  10  11  12
•        •        •     •
```

Acentos en: **1, 4, 7, 9** (algunos lo cuentan como 5 tiempos: 3+3+2+2+2)

## Maestros de la siguiriya

- **Manuel Torre**: *"Ese tiene el sonido negro"* (dijo Garcia Lorca)
- **Terremoto de Jerez**: Voz volcánica
- **Chocolate**: Interprete referencial del siglo XX
- **Manuel Agujetas**: Ultimo gran cantaor de siguiriyas puro

## Letras clasicas

> *"A la luna le pio*
> *la del alto cielo,*
> *como le pio que me saque a mi pare*
> *de onde esta metio."*

(Le pido a la luna que saque a mi padre de donde esta encerrado)

## Escuchar siguiriyas

No se puede entender la siguiriya solo leyendo. Hay que escucharla en silencio, sin prisa. Es una experiencia que va mas alla de la musica.

> *"La siguiriya no se canta. Se llora."*', 480, 3, false)
RETURNING id INTO l9;

-- Seccion 4: Guitarra flamenca
INSERT INTO sections (id, course_id, title, description, position)
VALUES (gen_random_uuid(), course_id, 'Guitarra flamenca', 'Tecnica basica y acompanamiento.', 3)
RETURNING id INTO s4;

INSERT INTO lessons (id, section_id, title, description, type, content_text, duration, position, is_free)
VALUES (gen_random_uuid(), s4, 'La guitarra flamenca vs. clasica', 'Diferencias entre ambos instrumentos y tecnicas.', 'text',
'# Guitarra flamenca vs. clasica

## Diferencias del instrumento

| Caracteristica | Clasica | Flamenca |
|----------------|---------|----------|
| Tapa | Cedro (gruesa) | Ciprés o abeto (fina) |
| Sonido | Redondo, sostenido | Brillante, percusivo |
| Accion | Alta (cuerdas lejos del mastil) | Baja (cuerdas cerca) |
| Golpeador | No tiene | Si (protege la tapa) |
| Cejilla | Opcional | Imprescindible |
| Peso | Mas pesado | Mas ligero |

## Diferencias de tecnica

### Mano derecha
- **Clasica**: apoyando y tirando, busca volumen y legato
- **Flamenca**: rasgueos, alzapua, golpes en la tapa, picado rapido

### Mano izquierda
- **Clasica**: posicion fija, limpieza
- **Flamenca**: ligados rapidos, cejilla constante, vibrato intenso

## Tecnicas exclusivas del flamenco

1. **Rasgueo**: abanico de dedos sobre las cuerdas (el sonido mas reconocible)
2. **Alzapua**: tecnica de pulgar con movimiento de ida y vuelta
3. **Golpe**: percusion con los dedos sobre la tapa
4. **Picado**: alternancia rapida indice-medio (como en clasica, pero mas agresivo)
5. **Tremolo**: repeticion rapida de una nota (4 dedos en flamenco vs. 3 en clasica)

## Constructores legendarios

- **Santos Hernandez** (s. XIX-XX)
- **Marcelo Barbero**
- **Conde Hermanos** (la marca de Paco de Lucia)', 360, 0, false)
RETURNING id INTO l10;

INSERT INTO lessons (id, section_id, title, description, type, content_text, duration, position, is_free)
VALUES (gen_random_uuid(), s4, 'Rasgueos basicos', 'Los primeros rasgueos para principiantes.', 'text',
'# Rasgueos basicos

## Posicion de la mano derecha

Situa la mano sobre la boca de la guitarra, relajada. Los dedos deben caer sobre las cuerdas de forma natural, no forzada.

## Rasgueo 1: Indice simple

El mas basico. Un solo dedo barre las cuerdas de agudas a graves.

```
↓ (indice hacia abajo)
```

Practica lento, buscando que todas las cuerdas suenen limpio.

## Rasgueo 2: Abanico de 4 dedos

El rasgueo clasico flamenco:

```
↓ menique
↓ anular
↓ corazon
↓ indice
```

Cada dedo cae una fraccion de segundo despues del anterior, creando un efecto de "cascada".

## Rasgueo 3: Arriba y abajo

```
↓ indice (hacia abajo)
↑ indice (hacia arriba, con la una)
```

## Ejercicio por tangos (4/4)

```
Tiempo:  1       2       3       4
Mano:    ↓↑      ↓↑      ↓↑↓↑    ↓
         lento   lento   rapido  golpe
```

## Errores comunes

- **Tension excesiva**: Si te duele la mano, para. La muneca debe estar relajada.
- **Rasguear desde el codo**: El movimiento sale de la **muneca**, no del brazo.
- **Tocar demasiado fuerte**: Primero precision, luego potencia.

## Practica diaria

15 minutos al dia de rasgueos es suficiente al principio. Usa metronomo empezando a 60 BPM.', 420, 1, false)
RETURNING id INTO l11;

INSERT INTO lessons (id, section_id, title, description, type, content_text, duration, position, is_free)
VALUES (gen_random_uuid(), s4, 'Acordes flamencos esenciales', 'Los acordes que necesitas para acompanar.', 'text',
'# Acordes flamencos esenciales

## La cadencia andaluza

La progresion de acordes mas caracteristica del flamenco:

```
Am → G → F → E
La menor → Sol → Fa → Mi
```

Esta cadencia es la base de soleares, bulerias, tangos y muchos mas palos.

## Acordes basicos

### Mi mayor (E) — El acorde rey del flamenco
```
E ||---0---|---0---|---0---|
B ||---0---|---0---|---0---|
G ||---1---|---0---|---0---|
D ||---2---|---2---|---0---|
A ||---2---|---2---|---2---|
E ||---0---|---0---|---0---|
```

### La menor (Am)
```
E ||---0---|
B ||---1---|
G ||---2---|
D ||---2---|
A ||---0---|
E ||-------|
```

### Fa mayor (F) — con cejilla
```
E ||---1---|---1---|
B ||---1---|---1---|
G ||---2---|---1---|
D ||---3---|---1---|
A ||---3---|---1---|
E ||---1---|---1---|
```

## Acordes flamencos especiales

### Mi frigio con 9a (acorde flamenco por excelencia)
```
E ||---0---|
B ||---1---|
G ||---0---|
D ||---2---|
A ||---2---|
E ||---0---|
```

Este acorde tiene un sonido "arabe" muy caracteristico.

## Ejercicio: cadencia andaluza con rasgueo

```
Am (2 compases) → G (2 compases) → F (2 compases) → E (4 compases)
```

El acorde de Mi se mantiene mas tiempo porque es el acorde de **resolucion** (donde descansa la musica).', 480, 2, false)
RETURNING id INTO l12;

-- Seccion 5: Cante y baile
INSERT INTO sections (id, course_id, title, description, position)
VALUES (gen_random_uuid(), course_id, 'Cante y baile flamenco', 'Introduccion al cante y los fundamentos del baile.', 4)
RETURNING id INTO s5;

INSERT INTO lessons (id, section_id, title, description, type, content_text, duration, position, is_free)
VALUES (gen_random_uuid(), s5, 'Introduccion al cante', 'Que es el cante y como se estructura.', 'text',
'# Introduccion al cante flamenco

## Que es el cante

El **cante** (no "canto") es la expresion vocal del flamenco. Es el elemento mas antiguo y, para muchos, el mas importante de la triada flamenca (cante, toque, baile).

## Clasificacion del cante

### Cante jondo (o grande)
- El mas profundo y antiguo
- Palos: siguiriyas, soleares, tonás
- Requiere mucha tecnica y sentimiento
- No es musica "bonita" — es musica **verdadera**

### Cante intermedio
- Palos: alegrias, tientos, farruca
- Mezcla de profundidad y accesibilidad

### Cante chico (o ligero)
- Palos: tangos, rumbas, sevillanas
- Mas festivo y facil de escuchar
- "Chico" no significa inferior

## Elementos del cante

### El temple
Calentamiento vocal antes de empezar: *"Ay... ay ay ay..."*

### El tercio
Cada frase de la letra. Una copla tiene normalmente 3 o 4 tercios.

### El quejio
Expresion vocal de dolor o emocion intensa. El "ay" flamenco.

### El melisma
Ornamentacion vocal: una silaba se extiende por varias notas.

## Como escuchar cante

1. No busques "afinacion perfecta" — busca **expresion**
2. Presta atencion al **silencio** entre frases
3. Cierra los ojos y siente el **compas**
4. El cante bueno te pone los pelos de punta — si no ocurre, sigue buscando', 360, 0, false)
RETURNING id INTO l13;

INSERT INTO lessons (id, section_id, title, description, type, content_text, duration, position, is_free)
VALUES (gen_random_uuid(), s5, 'Fundamentos del baile flamenco', 'Postura, braceo y zapateado basico.', 'text',
'# Fundamentos del baile flamenco

## La postura flamenca

El baile flamenco se distingue por una postura erguida, orgullosa:

- **Espalda recta**, casi arqueada hacia atras
- **Barbilla alta**, mirada al frente o ligeramente arriba
- **Hombros abiertos**, brazos activos
- **Centro de gravedad bajo**, rodillas ligeramente flexionadas
- **Pies**: peso distribuido, nunca planos del todo

> *"En el flamenco se baila hacia el suelo, no hacia el cielo."*

## Braceo (movimiento de brazos)

### Brazos femeninos
- Movimientos ondulantes, circulares
- Munecas que giran con gracia
- Dedos expresivos (pulgares activos)

### Brazos masculinos
- Mas angulares y geometricos
- Menos ornamentacion
- Potencia en lugar de gracia

### Ejercicio basico de braceo
1. Brazos a los lados, relajados
2. Sube el brazo derecho en semicirculo hasta arriba
3. Gira la muneca hacia fuera
4. Baja describiendo un circulo
5. Repite con el izquierdo

## Zapateado basico

### Planta
Golpe con toda la planta del pie. Sonido fuerte y grave.

### Tacon
Golpe solo con el tacon. Sonido seco y agudo.

### Punta
Golpe con la punta del pie. Sonido suave.

### Patron basico (por tangos)
```
Tiempo:  1        2        3        4
Pie:     PLANTA   tacon    punta    PLANTA
         fuerte   medio    suave    fuerte
```

## Consejo final

El zapateado es **percusion**. Los pies son un instrumento mas. No pienses en "pasos de baile" — piensa en **ritmo**.

## Calzado

- Mujeres: zapatos con clavo en tacon y punta
- Hombres: botin con tacon cubano y clavo
- Imprescindible: suelo de madera (nunca baldosa o moqueta)', 480, 1, false)
RETURNING id INTO l14;

-- ==========================================
-- 5. ENROLLMENTS
-- ==========================================
INSERT INTO enrollments (id, user_id, course_id, access_type, enrolled_at, last_accessed_at)
VALUES (gen_random_uuid(), student1, course_id, 'free', now() - interval '10 days', now() - interval '1 day')
RETURNING id INTO enroll1;

INSERT INTO enrollments (id, user_id, course_id, access_type, enrolled_at, last_accessed_at)
VALUES (gen_random_uuid(), student2, course_id, 'free', now() - interval '5 days', now() - interval '2 days')
RETURNING id INTO enroll2;

-- ==========================================
-- 6. REVIEWS
-- ==========================================
INSERT INTO reviews (id, course_id, user_id, rating, comment)
VALUES
(gen_random_uuid(), course_id, student1, 5, 'Increible curso. Las explicaciones de los palos son muy claras y el contenido de guitarra esta genial. Me ha enganchado desde la primera leccion.'),
(gen_random_uuid(), course_id, student2, 4, 'Muy buen curso para empezar con el flamenco. Echo en falta videos con ejemplos de cante y baile, pero el contenido teorico es excelente.');

-- ==========================================
-- 7. LESSON PROGRESS (Maria ha avanzado bastante)
-- ==========================================

-- Maria: completadas las 3 primeras lecciones + la 4a
INSERT INTO lesson_progress (id, user_id, lesson_id, is_completed, completed_at, last_position_seconds)
VALUES
(gen_random_uuid(), student1, l1, true, now() - interval '9 days', 0),
(gen_random_uuid(), student1, l2, true, now() - interval '8 days', 0),
(gen_random_uuid(), student1, l3, true, now() - interval '7 days', 0),
(gen_random_uuid(), student1, l4, true, now() - interval '6 days', 0),
(gen_random_uuid(), student1, l5, true, now() - interval '5 days', 0),
(gen_random_uuid(), student1, l6, true, now() - interval '4 days', 0),
(gen_random_uuid(), student1, l7, true, now() - interval '3 days', 0),
(gen_random_uuid(), student1, l8, false, NULL, 0);

-- Sara: solo 2 lecciones
INSERT INTO lesson_progress (id, user_id, lesson_id, is_completed, completed_at, last_position_seconds)
VALUES
(gen_random_uuid(), student2, l1, true, now() - interval '4 days', 0),
(gen_random_uuid(), student2, l2, true, now() - interval '3 days', 0);

END;
$$;
