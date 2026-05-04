-- ============================================
-- 02: SEED BASE — Usuarios, categorias, cursos base
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
-- Password de todos: admin123 (hash bcrypt)
INSERT INTO users (email, password_hash, first_name, last_name, role, bio, is_verified, is_active) VALUES
('admin@cursos.com',         '$2b$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5lMjH.UPA4C.m', 'Admin',   'Sistema',  'admin', NULL, TRUE, TRUE),
('instructor@cursos.com',    '$2b$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5lMjH.UPA4C.m', 'Joan',    'Garcia',   'user',  'Instructor de programación con 10 años de experiencia', TRUE, TRUE),
('student@cursos.com',       '$2b$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5lMjH.UPA4C.m', 'Maria',   'Lopez',    'user',  NULL, TRUE, TRUE),
('sara.martin@cursos.com',   '$2b$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5lMjH.UPA4C.m', 'Sara',    'Martin',   'user',  'Diseñadora UX/UI con 7 años de experiencia.', TRUE, TRUE),
('pau.roca@cursos.com',      '$2b$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5lMjH.UPA4C.m', 'Pau',     'Roca',     'user',  'Experto en marketing digital.', TRUE, TRUE),
('laia.font@cursos.com',     '$2b$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5lMjH.UPA4C.m', 'Laia',    'Font',     'user',  NULL, TRUE, TRUE),
('marc.puig@cursos.com',     '$2b$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5lMjH.UPA4C.m', 'Marc',    'Puig',     'user',  NULL, TRUE, TRUE),
('ana.vidal@cursos.com',     '$2b$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5lMjH.UPA4C.m', 'Ana',     'Vidal',    'user',  NULL, FALSE, TRUE),
('jordi.mas@cursos.com',     '$2b$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5lMjH.UPA4C.m', 'Jordi',   'Mas',      'user',  NULL, TRUE, TRUE),
('clara.soler@cursos.com',   '$2b$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5lMjH.UPA4C.m', 'Clara',   'Soler',    'user',  NULL, TRUE, TRUE),
('inactive@cursos.com',      '$2b$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5lMjH.UPA4C.m', 'Usuario', 'Inactivo', 'user',  NULL, FALSE, FALSE);

-- Cursos
INSERT INTO courses (title, slug, description, short_description, category_id, level, is_free, price, status, is_published, published_at) VALUES
('Introducción a PostgreSQL', 'introduccion-postgresql',
 'Aprende a gestionar bases de datos relacionales con PostgreSQL desde cero.',
 'Aprende PostgreSQL desde cero', 1, 'beginner', TRUE, NULL, 'published', TRUE, CURRENT_TIMESTAMP - INTERVAL '45 days'),

('Python para principiantes', 'python-principiantes',
 'Aprende Python desde cero. Cubrimos variables, funciones, listas, diccionarios, orientación a objetos y proyectos prácticos reales.',
 'El mejor punto de partida para aprender a programar', 1, 'beginner', TRUE, NULL, 'published', TRUE, CURRENT_TIMESTAMP - INTERVAL '30 days'),

('Desarrollo web con Vue 3', 'vue3-desarrollo-web',
 'Curso completo de Vue 3 con Composition API, Pinia, Vue Router e integración con backends REST.',
 'Domina Vue 3 y construye aplicaciones web modernas', 1, 'intermediate', FALSE, NULL, 'published', TRUE, CURRENT_TIMESTAMP - INTERVAL '15 days'),

('Algoritmos y estructuras de datos', 'algoritmos-estructuras-datos',
 'Estudio profundo de algoritmos de búsqueda, ordenación, grafos y estructuras como pilas, colas y árboles.',
 'Prepárate para entrevistas técnicas', 1, 'advanced', FALSE, NULL, 'published', TRUE, CURRENT_TIMESTAMP - INTERVAL '60 days'),

('Diseño UI/UX desde cero', 'diseno-ui-ux',
 'Aprende los fundamentos del diseño de interfaces y experiencia de usuario. Figma, prototipado y tests.',
 'Crea productos digitales que la gente ama', 2, 'beginner', FALSE, NULL, 'published', TRUE, CURRENT_TIMESTAMP - INTERVAL '20 days'),

('Marketing digital para negocios', 'marketing-digital-negocios',
 'Estrategias de marketing digital: SEO, SEM, redes sociales, email marketing y analítica web.',
 'Haz crecer tu negocio en el mundo digital', 4, 'beginner', FALSE, NULL, 'published', TRUE, CURRENT_TIMESTAMP - INTERVAL '10 days'),

('Introducción a la Inteligencia Artificial', 'introduccion-ia',
 'Curso en preparación sobre IA y machine learning para no técnicos.',
 'Entiende la IA sin necesidad de programar', 1, 'beginner', FALSE, NULL, 'draft', FALSE, NULL);

-- Instructores
INSERT INTO course_instructors (course_id, instructor_id, is_main)
SELECT c.id, u.id, TRUE FROM courses c, users u WHERE c.slug = 'introduccion-postgresql'      AND u.email = 'instructor@cursos.com';
INSERT INTO course_instructors (course_id, instructor_id, is_main)
SELECT c.id, u.id, TRUE FROM courses c, users u WHERE c.slug = 'python-principiantes'         AND u.email = 'instructor@cursos.com';
INSERT INTO course_instructors (course_id, instructor_id, is_main)
SELECT c.id, u.id, TRUE FROM courses c, users u WHERE c.slug = 'vue3-desarrollo-web'          AND u.email = 'instructor@cursos.com';
INSERT INTO course_instructors (course_id, instructor_id, is_main)
SELECT c.id, u.id, FALSE FROM courses c, users u WHERE c.slug = 'vue3-desarrollo-web'         AND u.email = 'sara.martin@cursos.com';
INSERT INTO course_instructors (course_id, instructor_id, is_main)
SELECT c.id, u.id, TRUE FROM courses c, users u WHERE c.slug = 'algoritmos-estructuras-datos' AND u.email = 'instructor@cursos.com';
INSERT INTO course_instructors (course_id, instructor_id, is_main)
SELECT c.id, u.id, TRUE FROM courses c, users u WHERE c.slug = 'diseno-ui-ux'                 AND u.email = 'sara.martin@cursos.com';
INSERT INTO course_instructors (course_id, instructor_id, is_main)
SELECT c.id, u.id, TRUE FROM courses c, users u WHERE c.slug = 'marketing-digital-negocios'   AND u.email = 'pau.roca@cursos.com';
INSERT INTO course_instructors (course_id, instructor_id, is_main)
SELECT c.id, u.id, TRUE FROM courses c, users u WHERE c.slug = 'introduccion-ia'              AND u.email = 'instructor@cursos.com';

-- Set created_by from main instructor
UPDATE courses c SET created_by = (
    SELECT ci.instructor_id FROM course_instructors ci
    WHERE ci.course_id = c.id AND ci.is_main = TRUE LIMIT 1
) WHERE c.created_by IS NULL;

-- Enrollments
INSERT INTO enrollments (user_id, course_id, access_type, enrolled_at, last_accessed_at)
SELECT u.id, c.id, 'free', CURRENT_TIMESTAMP - INTERVAL '25 days', CURRENT_TIMESTAMP - INTERVAL '2 days'
FROM users u, courses c WHERE u.email = 'student@cursos.com' AND c.slug = 'introduccion-postgresql';

INSERT INTO enrollments (user_id, course_id, access_type, enrolled_at, last_accessed_at)
SELECT u.id, c.id, 'free', CURRENT_TIMESTAMP - INTERVAL '20 days', CURRENT_TIMESTAMP - INTERVAL '1 day'
FROM users u, courses c WHERE u.email = 'student@cursos.com' AND c.slug = 'python-principiantes';

INSERT INTO enrollments (user_id, course_id, access_type, enrolled_at, last_accessed_at)
SELECT u.id, c.id, 'paid', CURRENT_TIMESTAMP - INTERVAL '12 days', CURRENT_TIMESTAMP - INTERVAL '3 days'
FROM users u, courses c WHERE u.email = 'student@cursos.com' AND c.slug = 'vue3-desarrollo-web';

INSERT INTO enrollments (user_id, course_id, access_type, enrolled_at, last_accessed_at)
SELECT u.id, c.id, 'free', CURRENT_TIMESTAMP - INTERVAL '18 days', CURRENT_TIMESTAMP - INTERVAL '5 days'
FROM users u, courses c WHERE u.email = 'laia.font@cursos.com' AND c.slug = 'python-principiantes';

INSERT INTO enrollments (user_id, course_id, access_type, enrolled_at, last_accessed_at)
SELECT u.id, c.id, 'paid', CURRENT_TIMESTAMP - INTERVAL '14 days', CURRENT_TIMESTAMP - INTERVAL '1 day'
FROM users u, courses c WHERE u.email = 'laia.font@cursos.com' AND c.slug = 'diseno-ui-ux';

INSERT INTO enrollments (user_id, course_id, access_type, enrolled_at, last_accessed_at)
SELECT u.id, c.id, 'free', CURRENT_TIMESTAMP - INTERVAL '8 days', CURRENT_TIMESTAMP
FROM users u, courses c WHERE u.email = 'marc.puig@cursos.com' AND c.slug = 'introduccion-postgresql';

INSERT INTO enrollments (user_id, course_id, access_type, enrolled_at, last_accessed_at)
SELECT u.id, c.id, 'paid', CURRENT_TIMESTAMP - INTERVAL '6 days', CURRENT_TIMESTAMP - INTERVAL '2 days'
FROM users u, courses c WHERE u.email = 'marc.puig@cursos.com' AND c.slug = 'marketing-digital-negocios';

INSERT INTO enrollments (user_id, course_id, access_type, enrolled_at, last_accessed_at)
SELECT u.id, c.id, 'free', CURRENT_TIMESTAMP - INTERVAL '55 days', CURRENT_TIMESTAMP - INTERVAL '10 days'
FROM users u, courses c WHERE u.email = 'jordi.mas@cursos.com' AND c.slug = 'algoritmos-estructuras-datos';

INSERT INTO enrollments (user_id, course_id, access_type, enrolled_at, last_accessed_at)
SELECT u.id, c.id, 'paid', CURRENT_TIMESTAMP - INTERVAL '9 days', CURRENT_TIMESTAMP - INTERVAL '1 day'
FROM users u, courses c WHERE u.email = 'clara.soler@cursos.com' AND c.slug = 'diseno-ui-ux';

-- Reviews
INSERT INTO reviews (course_id, user_id, rating, comment, created_at)
SELECT c.id, u.id, 5, '¡Excelente curso! Muy claro y bien estructurado.', CURRENT_TIMESTAMP - INTERVAL '20 days'
FROM courses c, users u WHERE c.slug = 'introduccion-postgresql' AND u.email = 'student@cursos.com';

INSERT INTO reviews (course_id, user_id, rating, comment, created_at)
SELECT c.id, u.id, 4, 'Muy buen curso. Las explicaciones son claras.', CURRENT_TIMESTAMP - INTERVAL '10 days'
FROM courses c, users u WHERE c.slug = 'introduccion-postgresql' AND u.email = 'marc.puig@cursos.com';

INSERT INTO reviews (course_id, user_id, rating, comment, created_at)
SELECT c.id, u.id, 5, 'El mejor curso de Python que he hecho.', CURRENT_TIMESTAMP - INTERVAL '15 days'
FROM courses c, users u WHERE c.slug = 'python-principiantes' AND u.email = 'student@cursos.com';

INSERT INTO reviews (course_id, user_id, rating, comment, created_at)
SELECT c.id, u.id, 5, '¡Perfecto para empezar!', CURRENT_TIMESTAMP - INTERVAL '12 days'
FROM courses c, users u WHERE c.slug = 'python-principiantes' AND u.email = 'laia.font@cursos.com';

INSERT INTO reviews (course_id, user_id, rating, comment, created_at)
SELECT c.id, u.id, 4, 'Muy completo. Falta un capítulo de testing.', CURRENT_TIMESTAMP - INTERVAL '8 days'
FROM courses c, users u WHERE c.slug = 'vue3-desarrollo-web' AND u.email = 'student@cursos.com';

INSERT INTO reviews (course_id, user_id, rating, comment, created_at)
SELECT c.id, u.id, 5, 'El curso de diseño UX/UI es brutal.', CURRENT_TIMESTAMP - INTERVAL '7 days'
FROM courses c, users u WHERE c.slug = 'diseno-ui-ux' AND u.email = 'laia.font@cursos.com';

INSERT INTO reviews (course_id, user_id, rating, comment, created_at)
SELECT c.id, u.id, 4, 'Muy útil para mi negocio.', CURRENT_TIMESTAMP - INTERVAL '4 days'
FROM courses c, users u WHERE c.slug = 'marketing-digital-negocios' AND u.email = 'marc.puig@cursos.com';

INSERT INTO reviews (course_id, user_id, rating, comment, created_at)
SELECT c.id, u.id, 3, 'El contenido es bueno pero el ritmo es muy rápido.', CURRENT_TIMESTAMP - INTERVAL '30 days'
FROM courses c, users u WHERE c.slug = 'algoritmos-estructuras-datos' AND u.email = 'jordi.mas@cursos.com';

INSERT INTO reviews (course_id, user_id, rating, comment, created_at)
SELECT c.id, u.id, 5, 'Impresionante. Muy bien estructurado.', CURRENT_TIMESTAMP - INTERVAL '5 days'
FROM courses c, users u WHERE c.slug = 'diseno-ui-ux' AND u.email = 'clara.soler@cursos.com';
