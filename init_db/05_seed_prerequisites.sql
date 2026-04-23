-- ============================================
-- 05: SEED PREREQUISITES - Rutas de aprendizaje entre cursos
-- ============================================

-- Vue 3 requiere saber PostgreSQL al 80%
INSERT INTO course_prerequisites (course_id, prerequisite_course_id, completion_threshold)
SELECT c.id, p.id, 80
FROM courses c
JOIN courses p ON p.slug = 'introduccio-postgresql'
WHERE c.slug = 'vue3-desenvolupament-web';

-- Algoritmos requiere Python al 85%
INSERT INTO course_prerequisites (course_id, prerequisite_course_id, completion_threshold)
SELECT c.id, p.id, 85
FROM courses c
JOIN courses p ON p.slug = 'python-principiants'
WHERE c.slug = 'algorismes-estructures-dades';

-- UI/UX requiere Python al 70%
INSERT INTO course_prerequisites (course_id, prerequisite_course_id, completion_threshold)
SELECT c.id, p.id, 70
FROM courses c
JOIN courses p ON p.slug = 'python-principiants'
WHERE c.slug = 'disseny-ui-ux';

-- Marketing digital requiere UI/UX al 80%
INSERT INTO course_prerequisites (course_id, prerequisite_course_id, completion_threshold)
SELECT c.id, p.id, 80
FROM courses c
JOIN courses p ON p.slug = 'disseny-ui-ux'
WHERE c.slug = 'marketing-digital-negocis';
