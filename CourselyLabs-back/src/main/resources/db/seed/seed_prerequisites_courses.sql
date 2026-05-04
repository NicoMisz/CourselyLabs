-- Cadena de aprendizaje:
--     Python (entry, gratis)
--       └─> PostgreSQL (gratis, requiere Python al 80%)
--             ├─> Vue 3              (Premium)
--             ├─> Algoritmos         (Premium)
--             └─> Diseño UI/UX       (Premium)

INSERT INTO course_prerequisites (course_id, prerequisite_course_id, completion_threshold)
SELECT c.id, p.id, 80
FROM courses c
JOIN courses p ON p.slug = 'python-principiantes'
WHERE c.slug = 'introduccion-postgresql';

INSERT INTO course_prerequisites (course_id, prerequisite_course_id, completion_threshold)
SELECT c.id, p.id, 80
FROM courses c
JOIN courses p ON p.slug = 'introduccion-postgresql'
WHERE c.slug = 'vue3-desarrollo-web';

INSERT INTO course_prerequisites (course_id, prerequisite_course_id, completion_threshold)
SELECT c.id, p.id, 80
FROM courses c
JOIN courses p ON p.slug = 'introduccion-postgresql'
WHERE c.slug = 'algoritmos-estructuras-datos';

INSERT INTO course_prerequisites (course_id, prerequisite_course_id, completion_threshold)
SELECT c.id, p.id, 80
FROM courses c
JOIN courses p ON p.slug = 'introduccion-postgresql'
WHERE c.slug = 'diseno-ui-ux';
