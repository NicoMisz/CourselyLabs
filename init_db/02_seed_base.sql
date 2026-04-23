-- ============================================
-- 02: SEED BASE — Usuarios, categorias, cursos base
-- ============================================

-- Categorias
INSERT INTO categories (name, slug, description) VALUES
('Programacio', 'programacio', 'Cursos de desenvolupament de software i programacio'),
('Disseny', 'disseny', 'Cursos de disseny grafic, UI/UX i creacio visual'),
('Negocis', 'negocis', 'Cursos de gestio empresarial i emprenedoria'),
('Marketing', 'marketing', 'Cursos de marqueting digital i estrategies de venda'),
('Idiomes', 'idiomes', 'Cursos d''aprenentatge d''idiomes'),
('Musica', 'musica', 'Cursos de musica, instruments i teoria musical');

-- Usuarios
-- Password de todos: admin123 (hash bcrypt)
INSERT INTO users (email, password_hash, first_name, last_name, role, bio, is_verified, is_active) VALUES
('admin@cursos.com',         '$2b$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5lMjH.UPA4C.m', 'Admin',   'Sistema',  'admin', NULL, TRUE, TRUE),
('instructor@cursos.com',    '$2b$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5lMjH.UPA4C.m', 'Joan',    'Garcia',   'user',  'Instructor de programacio amb 10 anys d''experiencia', TRUE, TRUE),
('student@cursos.com',       '$2b$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5lMjH.UPA4C.m', 'Maria',   'Lopez',    'user',  NULL, TRUE, TRUE),
('sara.martin@cursos.com',   '$2b$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5lMjH.UPA4C.m', 'Sara',    'Martin',   'user',  'Dissenyadora UX/UI amb 7 anys d''experiencia.', TRUE, TRUE),
('pau.roca@cursos.com',      '$2b$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5lMjH.UPA4C.m', 'Pau',     'Roca',     'user',  'Expert en marqueting digital.', TRUE, TRUE),
('laia.font@cursos.com',     '$2b$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5lMjH.UPA4C.m', 'Laia',    'Font',     'user',  NULL, TRUE, TRUE),
('marc.puig@cursos.com',     '$2b$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5lMjH.UPA4C.m', 'Marc',    'Puig',     'user',  NULL, TRUE, TRUE),
('ana.vidal@cursos.com',     '$2b$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5lMjH.UPA4C.m', 'Ana',     'Vidal',    'user',  NULL, FALSE, TRUE),
('jordi.mas@cursos.com',     '$2b$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5lMjH.UPA4C.m', 'Jordi',   'Mas',      'user',  NULL, TRUE, TRUE),
('clara.soler@cursos.com',   '$2b$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5lMjH.UPA4C.m', 'Clara',   'Soler',    'user',  NULL, TRUE, TRUE),
('inactive@cursos.com',      '$2b$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5lMjH.UPA4C.m', 'Usuari',  'Inactiu',  'user',  NULL, FALSE, FALSE);

-- Cursos
INSERT INTO courses (title, slug, description, short_description, category_id, level, is_free, price, status, is_published, published_at) VALUES
('Introduccio a PostgreSQL', 'introduccio-postgresql',
 'Apren a gestionar bases de dades relacionals amb PostgreSQL des de zero.',
 'Apren PostgreSQL des de zero', 1, 'beginner', TRUE, NULL, 'published', TRUE, CURRENT_TIMESTAMP - INTERVAL '45 days'),

('Python per a principiants', 'python-principiants',
 'Apren Python des de zero. Cobrim variables, funcions, llistes, diccionaris, orientacio a objectes i projectes practics reals.',
 'El millor punt de partida per aprendre a programar', 1, 'beginner', TRUE, NULL, 'published', TRUE, CURRENT_TIMESTAMP - INTERVAL '30 days'),

('Desenvolupament web amb Vue 3', 'vue3-desenvolupament-web',
 'Curs complet de Vue 3 amb Composition API, Pinia, Vue Router i integracio amb backends REST.',
 'Domina Vue 3 i construeix aplicacions web modernes', 1, 'intermediate', FALSE, NULL, 'published', TRUE, CURRENT_TIMESTAMP - INTERVAL '15 days'),

('Algorismes i estructures de dades', 'algorismes-estructures-dades',
 'Estudi profund d''algorismes de cerca, ordenacio, grafos i estructures com piles, cues i arbres.',
 'Prepara''t per a entrevistes tecniques', 1, 'advanced', FALSE, NULL, 'published', TRUE, CURRENT_TIMESTAMP - INTERVAL '60 days'),

('Disseny UI/UX des de zero', 'disseny-ui-ux',
 'Apren els fonaments del disseny d''interficies i experiencia d''usuari. Figma, prototipat i tests.',
 'Crea productes digitals que la gent estima', 2, 'beginner', FALSE, NULL, 'published', TRUE, CURRENT_TIMESTAMP - INTERVAL '20 days'),

('Marketing digital per a negocis', 'marketing-digital-negocis',
 'Estrategies de marqueting digital: SEO, SEM, xarxes socials, email marqueting i analitica web.',
 'Fes creixer el teu negoci al mon digital', 4, 'beginner', FALSE, NULL, 'published', TRUE, CURRENT_TIMESTAMP - INTERVAL '10 days'),

('Introduccio a la Intel·ligencia Artificial', 'introduccio-ia',
 'Curs en preparacio sobre IA i machine learning per a no tecnics.',
 'Enten la IA sense necessitat de programar', 1, 'beginner', FALSE, NULL, 'draft', FALSE, NULL);

-- Instructores
INSERT INTO course_instructors (course_id, instructor_id, is_main)
SELECT c.id, u.id, TRUE FROM courses c, users u WHERE c.slug = 'introduccio-postgresql'       AND u.email = 'instructor@cursos.com';
INSERT INTO course_instructors (course_id, instructor_id, is_main)
SELECT c.id, u.id, TRUE FROM courses c, users u WHERE c.slug = 'python-principiants'           AND u.email = 'instructor@cursos.com';
INSERT INTO course_instructors (course_id, instructor_id, is_main)
SELECT c.id, u.id, TRUE FROM courses c, users u WHERE c.slug = 'vue3-desenvolupament-web'      AND u.email = 'instructor@cursos.com';
INSERT INTO course_instructors (course_id, instructor_id, is_main)
SELECT c.id, u.id, FALSE FROM courses c, users u WHERE c.slug = 'vue3-desenvolupament-web'     AND u.email = 'sara.martin@cursos.com';
INSERT INTO course_instructors (course_id, instructor_id, is_main)
SELECT c.id, u.id, TRUE FROM courses c, users u WHERE c.slug = 'algorismes-estructures-dades'  AND u.email = 'instructor@cursos.com';
INSERT INTO course_instructors (course_id, instructor_id, is_main)
SELECT c.id, u.id, TRUE FROM courses c, users u WHERE c.slug = 'disseny-ui-ux'                 AND u.email = 'sara.martin@cursos.com';
INSERT INTO course_instructors (course_id, instructor_id, is_main)
SELECT c.id, u.id, TRUE FROM courses c, users u WHERE c.slug = 'marketing-digital-negocis'     AND u.email = 'pau.roca@cursos.com';
INSERT INTO course_instructors (course_id, instructor_id, is_main)
SELECT c.id, u.id, TRUE FROM courses c, users u WHERE c.slug = 'introduccio-ia'                AND u.email = 'instructor@cursos.com';

-- Set created_by from main instructor
UPDATE courses c SET created_by = (
    SELECT ci.instructor_id FROM course_instructors ci
    WHERE ci.course_id = c.id AND ci.is_main = TRUE LIMIT 1
) WHERE c.created_by IS NULL;

-- Enrollments
INSERT INTO enrollments (user_id, course_id, access_type, enrolled_at, last_accessed_at)
SELECT u.id, c.id, 'free', CURRENT_TIMESTAMP - INTERVAL '25 days', CURRENT_TIMESTAMP - INTERVAL '2 days'
FROM users u, courses c WHERE u.email = 'student@cursos.com' AND c.slug = 'introduccio-postgresql';

INSERT INTO enrollments (user_id, course_id, access_type, enrolled_at, last_accessed_at)
SELECT u.id, c.id, 'free', CURRENT_TIMESTAMP - INTERVAL '20 days', CURRENT_TIMESTAMP - INTERVAL '1 day'
FROM users u, courses c WHERE u.email = 'student@cursos.com' AND c.slug = 'python-principiants';

INSERT INTO enrollments (user_id, course_id, access_type, enrolled_at, last_accessed_at)
SELECT u.id, c.id, 'paid', CURRENT_TIMESTAMP - INTERVAL '12 days', CURRENT_TIMESTAMP - INTERVAL '3 days'
FROM users u, courses c WHERE u.email = 'student@cursos.com' AND c.slug = 'vue3-desenvolupament-web';

INSERT INTO enrollments (user_id, course_id, access_type, enrolled_at, last_accessed_at)
SELECT u.id, c.id, 'free', CURRENT_TIMESTAMP - INTERVAL '18 days', CURRENT_TIMESTAMP - INTERVAL '5 days'
FROM users u, courses c WHERE u.email = 'laia.font@cursos.com' AND c.slug = 'python-principiants';

INSERT INTO enrollments (user_id, course_id, access_type, enrolled_at, last_accessed_at)
SELECT u.id, c.id, 'paid', CURRENT_TIMESTAMP - INTERVAL '14 days', CURRENT_TIMESTAMP - INTERVAL '1 day'
FROM users u, courses c WHERE u.email = 'laia.font@cursos.com' AND c.slug = 'disseny-ui-ux';

INSERT INTO enrollments (user_id, course_id, access_type, enrolled_at, last_accessed_at)
SELECT u.id, c.id, 'free', CURRENT_TIMESTAMP - INTERVAL '8 days', CURRENT_TIMESTAMP
FROM users u, courses c WHERE u.email = 'marc.puig@cursos.com' AND c.slug = 'introduccio-postgresql';

INSERT INTO enrollments (user_id, course_id, access_type, enrolled_at, last_accessed_at)
SELECT u.id, c.id, 'paid', CURRENT_TIMESTAMP - INTERVAL '6 days', CURRENT_TIMESTAMP - INTERVAL '2 days'
FROM users u, courses c WHERE u.email = 'marc.puig@cursos.com' AND c.slug = 'marketing-digital-negocis';

INSERT INTO enrollments (user_id, course_id, access_type, enrolled_at, last_accessed_at)
SELECT u.id, c.id, 'free', CURRENT_TIMESTAMP - INTERVAL '55 days', CURRENT_TIMESTAMP - INTERVAL '10 days'
FROM users u, courses c WHERE u.email = 'jordi.mas@cursos.com' AND c.slug = 'algorismes-estructures-dades';

INSERT INTO enrollments (user_id, course_id, access_type, enrolled_at, last_accessed_at)
SELECT u.id, c.id, 'paid', CURRENT_TIMESTAMP - INTERVAL '9 days', CURRENT_TIMESTAMP - INTERVAL '1 day'
FROM users u, courses c WHERE u.email = 'clara.soler@cursos.com' AND c.slug = 'disseny-ui-ux';

-- Reviews
INSERT INTO reviews (course_id, user_id, rating, comment, created_at)
SELECT c.id, u.id, 5, 'Excellent curs! Molt clar i ben estructurat.', CURRENT_TIMESTAMP - INTERVAL '20 days'
FROM courses c, users u WHERE c.slug = 'introduccio-postgresql' AND u.email = 'student@cursos.com';

INSERT INTO reviews (course_id, user_id, rating, comment, created_at)
SELECT c.id, u.id, 4, 'Molt bon curs. Les explicacions son clares.', CURRENT_TIMESTAMP - INTERVAL '10 days'
FROM courses c, users u WHERE c.slug = 'introduccio-postgresql' AND u.email = 'marc.puig@cursos.com';

INSERT INTO reviews (course_id, user_id, rating, comment, created_at)
SELECT c.id, u.id, 5, 'El millor curs de Python que he fet.', CURRENT_TIMESTAMP - INTERVAL '15 days'
FROM courses c, users u WHERE c.slug = 'python-principiants' AND u.email = 'student@cursos.com';

INSERT INTO reviews (course_id, user_id, rating, comment, created_at)
SELECT c.id, u.id, 5, 'Perfecte per comencar!', CURRENT_TIMESTAMP - INTERVAL '12 days'
FROM courses c, users u WHERE c.slug = 'python-principiants' AND u.email = 'laia.font@cursos.com';

INSERT INTO reviews (course_id, user_id, rating, comment, created_at)
SELECT c.id, u.id, 4, 'Molt complet. Falta un capitol de testing.', CURRENT_TIMESTAMP - INTERVAL '8 days'
FROM courses c, users u WHERE c.slug = 'vue3-desenvolupament-web' AND u.email = 'student@cursos.com';

INSERT INTO reviews (course_id, user_id, rating, comment, created_at)
SELECT c.id, u.id, 5, 'El curs de disseny UX/UI es brutal.', CURRENT_TIMESTAMP - INTERVAL '7 days'
FROM courses c, users u WHERE c.slug = 'disseny-ui-ux' AND u.email = 'laia.font@cursos.com';

INSERT INTO reviews (course_id, user_id, rating, comment, created_at)
SELECT c.id, u.id, 4, 'Molt util per al meu negoci.', CURRENT_TIMESTAMP - INTERVAL '4 days'
FROM courses c, users u WHERE c.slug = 'marketing-digital-negocis' AND u.email = 'marc.puig@cursos.com';

INSERT INTO reviews (course_id, user_id, rating, comment, created_at)
SELECT c.id, u.id, 3, 'El contingut es bo pero el ritme es massa rapid.', CURRENT_TIMESTAMP - INTERVAL '30 days'
FROM courses c, users u WHERE c.slug = 'algorismes-estructures-dades' AND u.email = 'jordi.mas@cursos.com';

INSERT INTO reviews (course_id, user_id, rating, comment, created_at)
SELECT c.id, u.id, 5, 'Impressionant. Molt ben estructurat.', CURRENT_TIMESTAMP - INTERVAL '5 days'
FROM courses c, users u WHERE c.slug = 'disseny-ui-ux' AND u.email = 'clara.soler@cursos.com';
