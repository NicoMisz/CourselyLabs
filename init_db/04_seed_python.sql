-- ============================================
-- 04: SEED — Curso Python completo (1 bloque de cada tipo)
-- ============================================
--
-- Este seed pobla el curso "Python para principiantes" con un bloque
-- de cada tipo de contenido: text, video, pdf, quiz, project, open_text.
-- Sirve como demo del modelo multi-bloque y permite probar todas las
-- vistas del frontend con datos reales.
-- ============================================

DO $$
DECLARE
  v_course UUID;
  s1 UUID; s2 UUID; s3 UUID;
  l1 UUID; l2 UUID; l3 UUID; l4 UUID;
  b_quiz UUID; b_project UUID; b_open UUID;
  q1 UUID; q2 UUID; q3 UUID;
  a_quiz UUID; a_project UUID; a_open UUID;
BEGIN

SELECT id INTO v_course FROM courses WHERE slug = 'python-principiantes';
IF v_course IS NULL THEN RAISE NOTICE 'Curso python-principiantes no encontrado, saltando seed'; RETURN; END IF;

-- ── SECCIÓN 1: Bienvenida ─────────────────────────────────────────
INSERT INTO sections (id, course_id, title, description, position)
VALUES (gen_random_uuid(), v_course, 'Bienvenida', 'Presentación del curso y de tu instructor.', 0)
RETURNING id INTO s1;

INSERT INTO lessons (id, section_id, title, description, type, duration, position, is_free)
VALUES (gen_random_uuid(), s1, 'Introducción al curso', 'Qué vas a aprender y cómo aprovecharlo al máximo.', NULL, 240, 0, TRUE)
RETURNING id INTO l1;

-- Bloque 1 (text): bienvenida
INSERT INTO lesson_blocks (lesson_id, type, position, text_content) VALUES
(l1, 'text', 0,
'# ¡Bienvenido al curso de Python!

Este curso es **el punto de partida** del catálogo de CourselyLabs. Está diseñado para que aprendas Python desde cero, sin asumir conocimientos previos de programación.

## Qué vas a aprender

- Sintaxis básica: variables, tipos de datos, estructuras de control.
- Funciones, listas, diccionarios y otras estructuras.
- Programación orientada a objetos.
- Cómo construir tu primer programa real.

## Cómo está organizado

El curso se divide en **3 secciones** con lecciones cortas. Cada lección puede tener varios *bloques* de contenido distintos: texto, vídeo, PDFs, cuestionarios y proyectos prácticos.

> **Tip**: marca cada lección como completada al terminarla. Si la lección tiene un cuestionario o proyecto, se marca automáticamente al aprobarlo.

¡Empezamos!');

-- Bloque 2 (video): vídeo introductorio
INSERT INTO lesson_blocks (lesson_id, type, position, video_url) VALUES
(l1, 'video', 1, 'https://www.youtube.com/embed/Kp4Mvapo5kc');

-- ── SECCIÓN 2: Fundamentos ────────────────────────────────────────
INSERT INTO sections (id, course_id, title, description, position)
VALUES (gen_random_uuid(), v_course, 'Fundamentos de Python', 'Lo mínimo imprescindible para escribir tus primeros programas.', 1)
RETURNING id INTO s2;

-- Lección 2: Variables y tipos
INSERT INTO lessons (id, section_id, title, description, type, duration, position, is_free)
VALUES (gen_random_uuid(), s2, 'Variables y tipos de datos', 'Cómo guardar y manipular información.', NULL, 360, 0, TRUE)
RETURNING id INTO l2;

-- Bloque 3 (text): teoría
INSERT INTO lesson_blocks (lesson_id, type, position, text_content) VALUES
(l2, 'text', 0,
'# Variables y tipos de datos

Una **variable** es un nombre que apunta a un valor. En Python no hace falta declarar el tipo: se infiere del valor que asignas.

## Tipos básicos

| Tipo | Ejemplo | Notas |
|---|---|---|
| `int` | `42` | Enteros sin límite teórico de tamaño |
| `float` | `3.14` | Decimales de doble precisión |
| `str` | `"hola"` | Cadenas, comillas dobles o simples |
| `bool` | `True`, `False` | Booleanos |
| `list` | `[1, 2, 3]` | Mutable, ordenada |
| `dict` | `{"a": 1}` | Pares clave-valor |

## Ejemplo

```python
nombre = "Ana"
edad = 30
es_estudiante = True
notas = [8.5, 9.0, 7.5]
```

Para conocer el tipo de una variable: `type(variable)`.');

-- Bloque 4 (pdf): cheat sheet
-- Nota: pdf_url placeholder — sustituir por upload real en producción
INSERT INTO lesson_blocks (lesson_id, type, position, pdf_url) VALUES
(l2, 'pdf', 1, 'https://www.python.org/static/img/python-logo.png');

-- Bloque 5 (quiz): cuestionario de comprensión
INSERT INTO lesson_blocks (lesson_id, type, position, text_content) VALUES
(l2, 'quiz', 2, 'Comprueba lo que has aprendido sobre variables y tipos.')
RETURNING id INTO b_quiz;

INSERT INTO assessments (id, lesson_id, block_id, type, description, max_attempts, passing_score)
VALUES (gen_random_uuid(), l2, b_quiz, 'quiz',
        'Tres preguntas básicas sobre tipos de datos en Python.',
        3, 70)
RETURNING id INTO a_quiz;

-- Pregunta 1
INSERT INTO quiz_questions (id, assessment_id, question_text, position, points)
VALUES (gen_random_uuid(), a_quiz, '¿Qué tipo tiene el valor `3.14` en Python?', 0, 1)
RETURNING id INTO q1;
INSERT INTO quiz_options (question_id, option_text, is_correct, explanation, position) VALUES
(q1, 'int',   FALSE, '`int` es para enteros sin parte decimal.', 0),
(q1, 'float', TRUE,  'Correcto: los decimales son `float` en Python.', 1),
(q1, 'str',   FALSE, '`str` se usa para cadenas de texto.', 2),
(q1, 'bool',  FALSE, '`bool` es para `True`/`False`.', 3);

-- Pregunta 2
INSERT INTO quiz_questions (id, assessment_id, question_text, position, points)
VALUES (gen_random_uuid(), a_quiz, '¿Qué función devuelve el tipo de una variable?', 1, 1)
RETURNING id INTO q2;
INSERT INTO quiz_options (question_id, option_text, is_correct, explanation, position) VALUES
(q2, 'kind(x)',    FALSE, 'No existe en Python.', 0),
(q2, 'typeof(x)',  FALSE, 'Eso es JavaScript, no Python.', 1),
(q2, 'type(x)',    TRUE,  'Correcto: `type(x)` devuelve la clase del valor.', 2),
(q2, 'class(x)',   FALSE, 'No es la sintaxis correcta en Python.', 3);

-- Pregunta 3
INSERT INTO quiz_questions (id, assessment_id, question_text, position, points)
VALUES (gen_random_uuid(), a_quiz, '¿Cuál de estas estructuras es **mutable**?', 2, 1)
RETURNING id INTO q3;
INSERT INTO quiz_options (question_id, option_text, is_correct, explanation, position) VALUES
(q3, 'tuple',  FALSE, 'Las tuplas son inmutables.', 0),
(q3, 'list',   TRUE,  'Correcto: las listas se pueden modificar después de creadas.', 1),
(q3, 'str',    FALSE, 'Las cadenas son inmutables.', 2),
(q3, 'frozenset', FALSE, 'Como su nombre indica, es congelado (inmutable).', 3);

-- Lección 3: Estructuras de control
INSERT INTO lessons (id, section_id, title, description, type, duration, position, is_free)
VALUES (gen_random_uuid(), s2, 'Estructuras de control', 'Decisiones y bucles para que tu código tome caminos distintos.', NULL, 300, 1, TRUE)
RETURNING id INTO l3;

-- Bloque 6 (text): teoría
INSERT INTO lesson_blocks (lesson_id, type, position, text_content) VALUES
(l3, 'text', 0,
'# Estructuras de control

Tu programa necesita tomar decisiones y repetir tareas. Para eso usamos **condicionales** y **bucles**.

## Condicional `if`

```python
edad = 18
if edad >= 18:
    print("Adulto")
elif edad >= 13:
    print("Adolescente")
else:
    print("Niño")
```

## Bucle `for`

Recorre una secuencia conocida.

```python
for nombre in ["Ana", "Bob", "Carla"]:
    print(f"Hola, {nombre}!")
```

## Bucle `while`

Repite mientras una condición sea verdadera.

```python
contador = 0
while contador < 3:
    print(contador)
    contador += 1
```

> **Recuerda**: la indentación en Python **es** sintaxis. No es estética: son las llaves `{}` de otros lenguajes.');

-- Bloque 7 (open_text): pregunta abierta
INSERT INTO lesson_blocks (lesson_id, type, position, text_content) VALUES
(l3, 'open_text', 1, 'Reflexiona sobre la diferencia entre dos bucles muy parecidos.')
RETURNING id INTO b_open;

INSERT INTO assessments (id, lesson_id, block_id, type, description, max_attempts, passing_score)
VALUES (gen_random_uuid(), l3, b_open, 'open_text',
        'Explica con tus propias palabras cuándo usarías un `for` y cuándo un `while`. Pon un ejemplo concreto de cada uno.',
        2, 70)
RETURNING id INTO a_open;

-- ── SECCIÓN 3: Proyecto final ─────────────────────────────────────
INSERT INTO sections (id, course_id, title, description, position)
VALUES (gen_random_uuid(), v_course, 'Proyecto final', 'Aplica todo lo aprendido en un programa de verdad.', 2)
RETURNING id INTO s3;

INSERT INTO lessons (id, section_id, title, description, type, duration, position, is_free)
VALUES (gen_random_uuid(), s3, 'Tu primer programa', 'Calculadora de notas: pones notas y te dice si has aprobado.', NULL, 600, 0, FALSE)
RETURNING id INTO l4;

-- Bloque 8 (text): instrucciones
INSERT INTO lesson_blocks (lesson_id, type, position, text_content) VALUES
(l4, 'text', 0,
'# Proyecto: calculadora de notas

Vas a escribir un programa que:

1. Pide al usuario el número de asignaturas.
2. Por cada asignatura, pide el nombre y la nota (0-10).
3. Calcula la media.
4. Imprime cada asignatura y, al final, indica si la media es **aprobada** (≥ 5) o **suspensa**.

## Requisitos

- Usa al menos un bucle (`for` o `while`).
- Valida que las notas estén entre 0 y 10.
- Formatea la media con dos decimales.

## Cómo entregarlo

Sube tu archivo `.py` en el bloque siguiente. Tu instructor lo revisará y te dará feedback.

> **Bonus**: añade un sistema de pesos (créditos por asignatura) para calcular media ponderada.');

-- Bloque 9 (project): entrega de archivo
INSERT INTO lesson_blocks (lesson_id, type, position, text_content) VALUES
(l4, 'project', 1, 'Sube tu archivo `calculadora_notas.py` en formato `.py` o `.zip`.')
RETURNING id INTO b_project;

INSERT INTO assessments (id, lesson_id, block_id, type, description, max_attempts, passing_score)
VALUES (gen_random_uuid(), l4, b_project, 'project',
        'Entrega tu calculadora de notas. Será calificada manualmente por el instructor.',
        1, 70)
RETURNING id INTO a_project;

-- Bloque 10 (lab): laboratorio echo (placeholder, instructor configura template_id real)
-- Demo del nuevo bloque tipo "lab": entorno virtual sobre echo (Proxmox).
-- El template_id es PLACEHOLDER (1): el instructor debe sustituirlo por el ID
-- de la VM plantilla real en su instancia de echo. Cada alumno necesita un
-- clone asignado de esa plantilla en echo para poder usarla.
INSERT INTO lesson_blocks (lesson_id, type, position, lab_provider, lab_template_id, lab_instructions)
VALUES (l4, 'lab', 2, 'echo', 1,
'## Laboratorio: prueba tu calculadora en una VM Linux

Este bloque arranca una máquina virtual Ubuntu en echo. Sigue estos pasos:

1. Pulsa **Iniciar laboratorio** y espera a que la VM arranque (~10s).
2. Pulsa **Abrir consola** para acceder por noVNC.
3. Escribe tu programa en `~/calculadora.py` con `nano` o `vim`.
4. Ejecútalo: `python3 ~/calculadora.py`.
5. Cuando termines, pulsa **Detener** para liberar recursos.

> **Nota**: si ves "No tienes una VM asignada", pide a tu instructor que te aprovisione una plantilla.');

END;
$$;
