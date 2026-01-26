-- ============================================
-- ESQUEMA MÍNIMO BASE DE DADES
-- PLATAFORMA DE CURSOS ONLINE - MVP
-- PostgreSQL 16
-- ============================================

-- Extensions necessàries
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ============================================
-- 1. USERS - Usuaris del sistema
-- ============================================
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'student' CHECK (role IN ('admin', 'instructor', 'student')),
    bio TEXT,
    profile_picture_url VARCHAR(500),
    is_verified BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_is_active ON users(is_active);

COMMENT ON TABLE users IS 'Usuaris del sistema: admins, instructors i estudiants';

-- ============================================
-- 2. CATEGORIES - Categories de cursos
-- ============================================
CREATE TABLE categories (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    slug VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_categories_slug ON categories(slug);

COMMENT ON TABLE categories IS 'Categories per organitzar els cursos';

-- ============================================
-- 3. COURSES - Cursos
-- ============================================
CREATE TABLE courses (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    title VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL UNIQUE,
    description TEXT NOT NULL,
    short_description VARCHAR(500),
    thumbnail_url VARCHAR(500),
    category_id INTEGER REFERENCES categories(id) ON DELETE SET NULL,
    level VARCHAR(20) CHECK (level IN ('beginner', 'intermediate', 'advanced', 'all_levels')),
    
    -- Model de preu
    is_free BOOLEAN DEFAULT FALSE,
    price DECIMAL(10,2),
    
    -- Estat de publicació
    status VARCHAR(20) DEFAULT 'draft' CHECK (status IN ('draft', 'pending_review', 'published', 'archived')),
    is_published BOOLEAN DEFAULT FALSE,
    published_at TIMESTAMP,
    
    -- Estadístiques bàsiques
    total_students INTEGER DEFAULT 0,
    average_rating DECIMAL(3,2) DEFAULT 0.00,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_courses_slug ON courses(slug);
CREATE INDEX idx_courses_category ON courses(category_id);
CREATE INDEX idx_courses_status ON courses(status);
CREATE INDEX idx_courses_is_published ON courses(is_published);
CREATE INDEX idx_courses_created_at ON courses(created_at DESC);

COMMENT ON TABLE courses IS 'Cursos de la plataforma';
COMMENT ON COLUMN courses.status IS 'draft=esborrany, pending_review=pendent aprovació, published=publicat, archived=arxivat';

-- ============================================
-- 4. COURSE_INSTRUCTORS - Instructors per curs
-- ============================================
CREATE TABLE course_instructors (
    id SERIAL PRIMARY KEY,
    course_id UUID NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    instructor_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    is_main BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(course_id, instructor_id)
);

CREATE INDEX idx_course_instructors_course ON course_instructors(course_id);
CREATE INDEX idx_course_instructors_instructor ON course_instructors(instructor_id);

COMMENT ON TABLE course_instructors IS 'Relació many-to-many entre cursos i instructors';

-- ============================================
-- 5. ENROLLMENTS - Inscripcions a cursos
-- ============================================
CREATE TABLE enrollments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    course_id UUID NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    
    -- Tipus d'accés
    access_type VARCHAR(20) NOT NULL CHECK (access_type IN ('free', 'paid')),
    
    -- Dates
    enrolled_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_accessed_at TIMESTAMP,
    
    UNIQUE(user_id, course_id)
);

CREATE INDEX idx_enrollments_user ON enrollments(user_id);
CREATE INDEX idx_enrollments_course ON enrollments(course_id);
CREATE INDEX idx_enrollments_access_type ON enrollments(access_type);

COMMENT ON TABLE enrollments IS 'Inscripcions d''estudiants als cursos';

-- ============================================
-- 6. REVIEWS - Valoracions de cursos
-- ============================================
CREATE TABLE reviews (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    course_id UUID NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(course_id, user_id)
);

CREATE INDEX idx_reviews_course ON reviews(course_id);
CREATE INDEX idx_reviews_user ON reviews(user_id);
CREATE INDEX idx_reviews_rating ON reviews(rating);

COMMENT ON TABLE reviews IS 'Valoracions i comentaris dels cursos';

-- ============================================
-- TRIGGERS
-- ============================================

-- Trigger: Actualitzar updated_at
CREATE OR REPLACE FUNCTION update_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_users_updated_at 
BEFORE UPDATE ON users
FOR EACH ROW EXECUTE FUNCTION update_updated_at();

CREATE TRIGGER trg_courses_updated_at 
BEFORE UPDATE ON courses
FOR EACH ROW EXECUTE FUNCTION update_updated_at();

CREATE TRIGGER trg_reviews_updated_at 
BEFORE UPDATE ON reviews
FOR EACH ROW EXECUTE FUNCTION update_updated_at();

-- Trigger: Actualitzar total_students quan hi ha nova inscripció
CREATE OR REPLACE FUNCTION update_course_students()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        UPDATE courses 
        SET total_students = total_students + 1 
        WHERE id = NEW.course_id;
    ELSIF TG_OP = 'DELETE' THEN
        UPDATE courses 
        SET total_students = GREATEST(total_students - 1, 0) 
        WHERE id = OLD.course_id;
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_update_course_students
AFTER INSERT OR DELETE ON enrollments
FOR EACH ROW EXECUTE FUNCTION update_course_students();

-- Trigger: Actualitzar average_rating quan hi ha nova valoració
CREATE OR REPLACE FUNCTION update_course_rating()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE courses
    SET average_rating = (
        SELECT COALESCE(AVG(rating), 0) 
        FROM reviews 
        WHERE course_id = NEW.course_id
    )
    WHERE id = NEW.course_id;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_update_course_rating
AFTER INSERT OR UPDATE ON reviews
FOR EACH ROW EXECUTE FUNCTION update_course_rating();

-- ============================================
-- VISTES ÚTILS
-- ============================================

-- Vista: Cursos amb informació completa
CREATE OR REPLACE VIEW v_courses_full AS
SELECT 
    c.id,
    c.title,
    c.slug,
    c.description,
    c.short_description,
    c.thumbnail_url,
    c.level,
    c.is_free,
    c.price,
    c.status,
    c.is_published,
    c.total_students,
    c.average_rating,
    c.created_at,
    cat.name AS category_name,
    cat.slug AS category_slug,
    STRING_AGG(u.first_name || ' ' || u.last_name, ', ' ORDER BY ci.is_main DESC) AS instructors,
    COUNT(DISTINCT e.id) AS enrolled_count,
    COUNT(DISTINCT r.id) AS reviews_count
FROM courses c
LEFT JOIN categories cat ON c.category_id = cat.id
LEFT JOIN course_instructors ci ON c.id = ci.course_id
LEFT JOIN users u ON ci.instructor_id = u.id
LEFT JOIN enrollments e ON c.id = e.course_id
LEFT JOIN reviews r ON c.id = r.course_id
GROUP BY c.id, cat.name, cat.slug;

COMMENT ON VIEW v_courses_full IS 'Vista amb informació completa dels cursos';

-- Vista: Estadístiques d'instructors
CREATE OR REPLACE VIEW v_instructor_stats AS
SELECT 
    u.id AS instructor_id,
    u.first_name || ' ' || u.last_name AS instructor_name,
    u.email,
    u.profile_picture_url,
    u.bio,
    COUNT(DISTINCT c.id) AS total_courses,
    COUNT(DISTINCT CASE WHEN c.is_published THEN c.id END) AS published_courses,
    COUNT(DISTINCT e.id) AS total_students,
    COALESCE(AVG(c.average_rating), 0) AS average_rating,
    u.created_at AS instructor_since
FROM users u
LEFT JOIN course_instructors ci ON u.id = ci.instructor_id
LEFT JOIN courses c ON ci.course_id = c.id
LEFT JOIN enrollments e ON c.id = e.course_id
WHERE u.role = 'instructor'
GROUP BY u.id, u.first_name, u.last_name, u.email, u.profile_picture_url, u.bio, u.created_at;

COMMENT ON VIEW v_instructor_stats IS 'Estadístiques dels instructors';

-- ============================================
-- FUNCIONS ÚTILS
-- ============================================

-- Funció: Verificar si un usuari pot accedir a un curs
CREATE OR REPLACE FUNCTION can_access_course(p_user_id UUID, p_course_id UUID)
RETURNS BOOLEAN AS $$
DECLARE
    course_is_free BOOLEAN;
    has_enrollment BOOLEAN;
BEGIN
    -- Comprovar si el curs és gratuït
    SELECT is_free INTO course_is_free 
    FROM courses 
    WHERE id = p_course_id;
    
    IF course_is_free THEN
        RETURN TRUE;
    END IF;
    
    -- Comprovar si té enrollment
    SELECT EXISTS(
        SELECT 1 FROM enrollments 
        WHERE user_id = p_user_id AND course_id = p_course_id
    ) INTO has_enrollment;
    
    RETURN has_enrollment;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION can_access_course IS 'Verifica si un usuari pot accedir a un curs';

-- Funció: Obtenir cursos recomanats per usuari
CREATE OR REPLACE FUNCTION get_recommended_courses(p_user_id UUID, p_limit INTEGER DEFAULT 10)
RETURNS TABLE (
    course_id UUID,
    course_title VARCHAR,
    course_slug VARCHAR,
    category_name VARCHAR,
    average_rating DECIMAL,
    total_students INTEGER
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        c.id,
        c.title,
        c.slug,
        cat.name,
        c.average_rating,
        c.total_students
    FROM courses c
    LEFT JOIN categories cat ON c.category_id = cat.id
    WHERE c.is_published = TRUE
    AND c.id NOT IN (
        SELECT course_id FROM enrollments WHERE user_id = p_user_id
    )
    ORDER BY c.average_rating DESC, c.total_students DESC
    LIMIT p_limit;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION get_recommended_courses IS 'Retorna cursos recomanats per un usuari';

-- ============================================
-- DADES INICIALS
-- ============================================

-- Inserir categories bàsiques
INSERT INTO categories (name, slug, description) VALUES
('Programació', 'programacio', 'Cursos de desenvolupament de software i programació'),
('Disseny', 'disseny', 'Cursos de disseny gràfic, UI/UX i creació visual'),
('Negocis', 'negocis', 'Cursos de gestió empresarial i emprenedoria'),
('Marketing', 'marketing', 'Cursos de màrqueting digital i estratègies de venda'),
('Idiomes', 'idiomes', 'Cursos d''aprenentatge d''idiomes');

-- Inserir usuari admin per defecte
-- Email: admin@cursos.com
-- Password: admin123
INSERT INTO users (email, password_hash, first_name, last_name, role, is_verified, is_active) VALUES
('admin@cursos.com', '$2b$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5lMjH.UPA4C.m', 'Admin', 'Sistema', 'admin', TRUE, TRUE);

-- Inserir instructor d'exemple
-- Email: instructor@cursos.com
-- Password: instructor123
INSERT INTO users (email, password_hash, first_name, last_name, role, bio, is_verified, is_active) VALUES
('instructor@cursos.com', '$2b$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5lMjH.UPA4C.m', 'Joan', 'Garcia', 'instructor', 'Instructor de programació amb 10 anys d''experiència', TRUE, TRUE);

-- Inserir estudiant d'exemple
-- Email: student@cursos.com
-- Password: student123
INSERT INTO users (email, password_hash, first_name, last_name, role, is_verified, is_active) VALUES
('student@cursos.com', '$2b$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5lMjH.UPA4C.m', 'Maria', 'López', 'student', TRUE, TRUE);

-- Inserir curs d'exemple
INSERT INTO courses (title, slug, description, short_description, category_id, level, is_free, price, status, is_published, published_at)
VALUES (
    'Introducció a PostgreSQL',
    'introduccio-postgresql',
    'Aprèn a gestionar bases de dades relacionals amb PostgreSQL des de zero. Aquest curs cobreix des dels conceptes bàsics fins a tècniques avançades.',
    'Aprèn PostgreSQL des de zero',
    1, -- Programació
    'beginner',
    TRUE,
    NULL,
    'published',
    TRUE,
    CURRENT_TIMESTAMP
);

-- Assignar instructor al curs
INSERT INTO course_instructors (course_id, instructor_id, is_main)
SELECT c.id, u.id, TRUE
FROM courses c, users u
WHERE c.slug = 'introduccio-postgresql' AND u.email = 'instructor@cursos.com';

-- ============================================
-- QUERIES D'EXEMPLE
-- ============================================

-- Exemple 1: Llistar tots els cursos publicats amb els seus instructors
/*
SELECT * FROM v_courses_full 
WHERE is_published = TRUE 
ORDER BY created_at DESC;
*/

-- Exemple 2: Obtenir estadístiques d'un instructor
/*
SELECT * FROM v_instructor_stats 
WHERE instructor_id = 'UUID_DEL_INSTRUCTOR';
*/

-- Exemple 3: Inscriure un estudiant a un curs
/*
INSERT INTO enrollments (user_id, course_id, access_type)
VALUES ('UUID_ESTUDIANT', 'UUID_CURS', 'free');
*/

-- Exemple 4: Valorar un curs
/*
INSERT INTO reviews (course_id, user_id, rating, comment)
VALUES ('UUID_CURS', 'UUID_USUARI', 5, 'Excel·lent curs!');
*/

-- Exemple 5: Comprovar si un usuari pot accedir a un curs
/*
SELECT can_access_course('UUID_USUARI', 'UUID_CURS');
*/

-- Exemple 6: Obtenir cursos recomanats per un usuari
/*
SELECT * FROM get_recommended_courses('UUID_USUARI', 5);
*/

-- ============================================
-- FI DE L'ESQUEMA MVP
-- ============================================

COMMENT ON DATABASE cursos_db IS 'Base de dades MVP - Plataforma de cursos online';

-- Mostrar resum de taules
SELECT 
    schemaname,
    tablename,
    pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) AS size
FROM pg_tables
WHERE schemaname = 'public'
ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC;