-- ============================================
-- 05: SEED PREREQUISITES — Cadena de aprendizaje
-- ============================================
--
-- Topología:
--
--     Python (entry, gratis)
--       └─> PostgreSQL (gratis, requiere Python al 80%)
--             ├─> Vue 3              (Premium, requiere PostgreSQL al 80%)
--             ├─> Algoritmos         (Premium, requiere PostgreSQL al 80%)
--             └─> Diseño UI/UX       (Premium, requiere PostgreSQL al 80%)
--
-- ============================================

-- PostgreSQL requiere Python al 80%
INSERT INTO course_prerequisites (course_id, prerequisite_course_id, completion_threshold)
SELECT c.id, p.id, 80
FROM courses c
JOIN courses p ON p.slug = 'python-principiantes'
WHERE c.slug = 'introduccion-postgresql';

-- Vue 3 requiere PostgreSQL al 80%
INSERT INTO course_prerequisites (course_id, prerequisite_course_id, completion_threshold)
SELECT c.id, p.id, 80
FROM courses c
JOIN courses p ON p.slug = 'introduccion-postgresql'
WHERE c.slug = 'vue3-desarrollo-web';

-- Algoritmos requiere PostgreSQL al 80%
INSERT INTO course_prerequisites (course_id, prerequisite_course_id, completion_threshold)
SELECT c.id, p.id, 80
FROM courses c
JOIN courses p ON p.slug = 'introduccion-postgresql'
WHERE c.slug = 'algoritmos-estructuras-datos';

-- Diseño UI/UX requiere PostgreSQL al 80%
INSERT INTO course_prerequisites (course_id, prerequisite_course_id, completion_threshold)
SELECT c.id, p.id, 80
FROM courses c
JOIN courses p ON p.slug = 'introduccion-postgresql'
WHERE c.slug = 'diseno-ui-ux';
