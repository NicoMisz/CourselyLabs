-- ============================================
-- 02: SEED BASE — Usuarios, categorias, cursos base
-- ============================================
--
-- Setup mínimo para demos y desarrollo:
--   - 1 usuario instructor (login: instructor@cursos.com / instructor123)
--   - 6 categorias
--   - 5 cursos en cadena de aprendizaje:
--       Python (gratis, entry-point)
--         └─> PostgreSQL (gratis, requiere Python 80%)
--               └─> Vue 3 / Algoritmos / Diseño UI-UX (Premium, requieren PostgreSQL 80%)
--
-- Los alumnos se registran y prueban el flujo real (sin seeds de student/reviews/enrollments).
-- Para promover a admin manualmente: UPDATE users SET role='admin' WHERE email='X';
-- ============================================

-- Categorias
INSERT INTO categories (name, slug, description) VALUES
('Programación', 'programacion', 'Cursos de desarrollo de software y programación'),
('Diseño',       'diseno',       'Cursos de diseño gráfico, UI/UX y creación visual'),
('Negocios',     'negocios',     'Cursos de gestión empresarial y emprendimiento'),
('Marketing',    'marketing',    'Cursos de marketing digital y estrategias de venta'),
('Idiomas',      'idiomas',      'Cursos de aprendizaje de idiomas'),
('Música',       'musica',       'Cursos de música, instrumentos y teoría musical');

-- Usuarios
-- Password: instructor123 (hash bcrypt $2y$10 válido para Spring Security)
INSERT INTO users (email, password_hash, first_name, last_name, role, bio, is_verified, is_active) VALUES
('instructor@cursos.com',
 '$2y$10$.DvHFY1XA0U.VVTLM5P2puaPq2TeOg/9BFsxMYdswGzvdG7DKDmA6',
 'Joan', 'Garcia', 'user',
 'Instructor de programación con 10 años de experiencia. Aficionado a la docencia clara y a los proyectos prácticos.',
 TRUE, TRUE);

-- Cursos
INSERT INTO courses (title, slug, description, short_description, category_id, level, is_free, price, status, is_published, published_at) VALUES
('Python para principiantes', 'python-principiantes',
 'Aprende Python desde cero. Cubrimos variables, funciones, listas, diccionarios, orientación a objetos y proyectos prácticos reales. Es el punto de entrada recomendado para todos los itinerarios técnicos del catálogo.',
 'El mejor punto de partida para aprender a programar', 1, 'beginner', TRUE, NULL, 'published', TRUE, CURRENT_TIMESTAMP - INTERVAL '30 days'),

('Introducción a PostgreSQL', 'introduccion-postgresql',
 'Aprende a gestionar bases de datos relacionales con PostgreSQL desde cero. Continuación natural del curso de Python para empezar a trabajar con datos persistentes.',
 'Aprende PostgreSQL desde cero', 1, 'beginner', TRUE, NULL, 'published', TRUE, CURRENT_TIMESTAMP - INTERVAL '20 days'),

('Desarrollo web con Vue 3', 'vue3-desarrollo-web',
 'Curso completo de Vue 3 con Composition API, Pinia, Vue Router e integración con backends REST.',
 'Domina Vue 3 y construye aplicaciones web modernas', 1, 'intermediate', FALSE, NULL, 'published', TRUE, CURRENT_TIMESTAMP - INTERVAL '15 days'),

('Algoritmos y estructuras de datos', 'algoritmos-estructuras-datos',
 'Estudio profundo de algoritmos de búsqueda, ordenación, grafos y estructuras como pilas, colas y árboles.',
 'Prepárate para entrevistas técnicas', 1, 'advanced', FALSE, NULL, 'published', TRUE, CURRENT_TIMESTAMP - INTERVAL '10 days'),

('Diseño UI/UX desde cero', 'diseno-ui-ux',
 'Aprende los fundamentos del diseño de interfaces y experiencia de usuario. Figma, prototipado y tests.',
 'Crea productos digitales que la gente ama', 2, 'beginner', FALSE, NULL, 'published', TRUE, CURRENT_TIMESTAMP - INTERVAL '5 days');

-- Instructores (todos los cursos asignados al único instructor del seed)
INSERT INTO course_instructors (course_id, instructor_id, is_main)
SELECT c.id, u.id, TRUE
FROM courses c, users u
WHERE u.email = 'instructor@cursos.com';

-- Set created_by from main instructor
UPDATE courses c SET created_by = (
    SELECT ci.instructor_id FROM course_instructors ci
    WHERE ci.course_id = c.id AND ci.is_main = TRUE LIMIT 1
) WHERE c.created_by IS NULL;

-- Sin enrollments ni reviews en seed: el flujo real lo prueban los alumnos al registrarse.
