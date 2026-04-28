-- ============================================
-- 01: SCHEMA BASE — Tablas, triggers, vistas, funciones
-- ============================================

-- Extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ============================================
-- USERS
-- ============================================
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'user' CHECK (role IN ('admin', 'user', 'premium')),
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

-- ============================================
-- REFRESH TOKENS
-- ============================================
CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token VARCHAR(500) UNIQUE NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    revoked BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_refresh_tokens_token ON refresh_tokens(token);
CREATE INDEX idx_refresh_tokens_user ON refresh_tokens(user_id);

-- ============================================
-- CATEGORIES
-- ============================================
CREATE TABLE categories (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    slug VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_categories_slug ON categories(slug);

-- ============================================
-- COURSES
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
    is_free BOOLEAN DEFAULT FALSE,
    price DECIMAL(10,2),
    status VARCHAR(20) DEFAULT 'draft' CHECK (status IN ('draft', 'pending_review', 'published', 'archived', 'rejected')),
    rejection_reason TEXT,
    is_published BOOLEAN DEFAULT FALSE,
    published_at TIMESTAMP,
    total_students INTEGER DEFAULT 0,
    average_rating DECIMAL(3,2) DEFAULT 0.00,
    created_by UUID REFERENCES users(id),
    storage_bytes BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_courses_slug ON courses(slug);
CREATE INDEX idx_courses_category ON courses(category_id);
CREATE INDEX idx_courses_status ON courses(status);
CREATE INDEX idx_courses_is_published ON courses(is_published);
CREATE INDEX idx_courses_created_at ON courses(created_at DESC);
CREATE INDEX idx_courses_created_by ON courses(created_by);

-- ============================================
-- COURSE_INSTRUCTORS
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

-- ============================================
-- ENROLLMENTS
-- ============================================
CREATE TABLE enrollments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    course_id UUID NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    access_type VARCHAR(20) NOT NULL CHECK (access_type IN ('free', 'paid')),
    enrolled_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_accessed_at TIMESTAMP,
    UNIQUE(user_id, course_id)
);

CREATE INDEX idx_enrollments_user ON enrollments(user_id);
CREATE INDEX idx_enrollments_course ON enrollments(course_id);
CREATE INDEX idx_enrollments_access_type ON enrollments(access_type);

-- ============================================
-- REVIEWS
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

-- ============================================
-- SECTIONS (V2)
-- ============================================
CREATE TABLE sections (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    course_id UUID NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    position INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_sections_course_id ON sections(course_id);

-- ============================================
-- LESSONS (V2)
-- ============================================
CREATE TABLE lessons (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    section_id UUID NOT NULL REFERENCES sections(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    type VARCHAR(20),  -- legacy/optional: blocks are the source of truth now
    content_url VARCHAR(500),
    content_text TEXT,
    duration INTEGER DEFAULT 0,
    position INTEGER NOT NULL DEFAULT 0,
    is_free BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_lessons_section_id ON lessons(section_id);

-- ============================================
-- LESSON_PROGRESS (V3)
-- ============================================
CREATE TABLE lesson_progress (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    lesson_id UUID NOT NULL REFERENCES lessons(id) ON DELETE CASCADE,
    is_completed BOOLEAN NOT NULL DEFAULT FALSE,
    completed_at TIMESTAMP,
    last_position_seconds INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (user_id, lesson_id)
);

CREATE INDEX idx_lesson_progress_user_id ON lesson_progress(user_id);
CREATE INDEX idx_lesson_progress_lesson_id ON lesson_progress(lesson_id);

-- ============================================
-- VERIFICATION TOKENS
-- ============================================

CREATE TABLE verification_tokens (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token       VARCHAR(255) NOT NULL UNIQUE,
    expires_at  TIMESTAMP NOT NULL,
    used        BOOLEAN NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_verification_tokens_token ON verification_tokens(token);
CREATE INDEX idx_verification_tokens_user_id ON verification_tokens(user_id);

-- ============================================
-- SUBSCRIPTIONS
-- ============================================

CREATE TABLE subscriptions (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id                 UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    stripe_subscription_id  VARCHAR(255) UNIQUE,
    stripe_customer_id      VARCHAR(255),
    plan                    VARCHAR(20) NOT NULL CHECK (plan IN ('monthly', 'annual')),
    status                  VARCHAR(20) NOT NULL DEFAULT 'active' CHECK (status IN ('active', 'cancelled', 'past_due', 'expired')),
    current_period_start    TIMESTAMP,
    current_period_end      TIMESTAMP,
    cancelled_at            TIMESTAMP,
    created_at              TIMESTAMP NOT NULL DEFAULT now(),
    updated_at              TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_subscriptions_user_id ON subscriptions(user_id);
CREATE INDEX idx_subscriptions_stripe_sub_id ON subscriptions(stripe_subscription_id);

-- ============================================
-- PAYMENTS
-- ============================================

CREATE TABLE payments (
    id                          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id                     UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    stripe_session_id           VARCHAR(255) UNIQUE,
    stripe_payment_intent_id    VARCHAR(255),
    type                        VARCHAR(20) NOT NULL CHECK (type IN ('subscription', 'one_time')),
    description                 VARCHAR(500),
    amount                      DECIMAL(10,2) NOT NULL,
    currency                    VARCHAR(3) NOT NULL DEFAULT 'eur',
    status                      VARCHAR(20) NOT NULL DEFAULT 'pending' CHECK (status IN ('pending', 'completed', 'failed')),
    created_at                  TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_payments_user_id ON payments(user_id);
CREATE INDEX idx_payments_stripe_session ON payments(stripe_session_id);

-- ============================================
-- LESSON RESOURCES
-- ============================================

CREATE TABLE lesson_resources (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    lesson_id       UUID NOT NULL REFERENCES lessons(id) ON DELETE CASCADE,
    file_name       VARCHAR(255) NOT NULL,
    storage_key     VARCHAR(500) NOT NULL,
    file_size       BIGINT NOT NULL,
    mime_type       VARCHAR(100),
    download_count  INTEGER NOT NULL DEFAULT 0,
    position        INTEGER NOT NULL DEFAULT 0,
    created_at      TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_lesson_resources_lesson_id ON lesson_resources(lesson_id);

-- ============================================
-- COURSE PREREQUISITES
-- ============================================

CREATE TABLE course_prerequisites (
    id                     UUID          NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    course_id              UUID          NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    prerequisite_course_id UUID          NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    completion_threshold   INT           NOT NULL DEFAULT 80,
    created_at             TIMESTAMP     NOT NULL DEFAULT now(),
    UNIQUE (course_id, prerequisite_course_id)
);

CREATE INDEX idx_course_prerequisites_course_id ON course_prerequisites(course_id);

-- ============================================
-- LESSON BLOCKS (multi-content per lesson)
-- ============================================

CREATE TABLE lesson_blocks (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    lesson_id       UUID NOT NULL REFERENCES lessons(id) ON DELETE CASCADE,
    type            VARCHAR(20) NOT NULL CHECK (type IN ('text', 'video', 'pdf', 'quiz', 'project', 'open_text')),
    position        INTEGER NOT NULL DEFAULT 0,
    text_content    TEXT,
    video_url       VARCHAR(500),
    pdf_url         VARCHAR(500),
    created_at      TIMESTAMP NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_lesson_blocks_lesson_id ON lesson_blocks(lesson_id);

-- ============================================
-- ASSESSMENTS (quiz, project, open_text)
-- ============================================

CREATE TABLE assessments (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    lesson_id           UUID REFERENCES lessons(id) ON DELETE CASCADE,
    block_id            UUID UNIQUE REFERENCES lesson_blocks(id) ON DELETE CASCADE,
    type                VARCHAR(20) NOT NULL CHECK (type IN ('quiz', 'project', 'open_text')),
    description         TEXT,
    max_attempts        INTEGER NOT NULL DEFAULT 3,
    time_limit_minutes  INTEGER,
    passing_score       INTEGER NOT NULL DEFAULT 70,
    shuffle_options     BOOLEAN NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMP NOT NULL DEFAULT now(),
    updated_at          TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_assessments_lesson_id ON assessments(lesson_id);

CREATE TABLE quiz_questions (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    assessment_id   UUID NOT NULL REFERENCES assessments(id) ON DELETE CASCADE,
    question_text   TEXT NOT NULL,
    position        INTEGER NOT NULL DEFAULT 0,
    points          INTEGER NOT NULL DEFAULT 1,
    created_at      TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_quiz_questions_assessment_id ON quiz_questions(assessment_id);

CREATE TABLE quiz_options (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    question_id     UUID NOT NULL REFERENCES quiz_questions(id) ON DELETE CASCADE,
    option_text     TEXT NOT NULL,
    is_correct      BOOLEAN NOT NULL DEFAULT FALSE,
    explanation     TEXT,
    position        INTEGER NOT NULL DEFAULT 0
);

CREATE INDEX idx_quiz_options_question_id ON quiz_options(question_id);

CREATE TABLE assessment_attempts (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    assessment_id   UUID NOT NULL REFERENCES assessments(id) ON DELETE CASCADE,
    user_id         UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    started_at      TIMESTAMP NOT NULL DEFAULT now(),
    submitted_at    TIMESTAMP,
    score           INTEGER,
    passed          BOOLEAN,
    attempt_number  INTEGER NOT NULL DEFAULT 1,
    status          VARCHAR(20) NOT NULL DEFAULT 'in_progress' CHECK (status IN ('in_progress', 'submitted', 'graded', 'expired')),
    UNIQUE (assessment_id, user_id, attempt_number)
);

CREATE INDEX idx_assessment_attempts_assessment_id ON assessment_attempts(assessment_id);
CREATE INDEX idx_assessment_attempts_user_id ON assessment_attempts(user_id);

CREATE TABLE quiz_answers (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    attempt_id          UUID NOT NULL REFERENCES assessment_attempts(id) ON DELETE CASCADE,
    question_id         UUID NOT NULL REFERENCES quiz_questions(id) ON DELETE CASCADE,
    selected_option_id  UUID REFERENCES quiz_options(id) ON DELETE SET NULL,
    UNIQUE (attempt_id, question_id)
);

CREATE INDEX idx_quiz_answers_attempt_id ON quiz_answers(attempt_id);

CREATE TABLE submissions (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    attempt_id          UUID NOT NULL UNIQUE REFERENCES assessment_attempts(id) ON DELETE CASCADE,
    type                VARCHAR(20) NOT NULL CHECK (type IN ('project', 'open_text')),
    storage_key         VARCHAR(500),
    file_name           VARCHAR(255),
    file_size           BIGINT,
    answer_text         TEXT,
    instructor_feedback TEXT,
    graded_at           TIMESTAMP,
    graded_by           UUID REFERENCES users(id),
    created_at          TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_submissions_attempt_id ON submissions(attempt_id);

-- ============================================
-- TRIGGERS
-- ============================================

CREATE OR REPLACE FUNCTION update_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_users_updated_at BEFORE UPDATE ON users FOR EACH ROW EXECUTE FUNCTION update_updated_at();
CREATE TRIGGER trg_courses_updated_at BEFORE UPDATE ON courses FOR EACH ROW EXECUTE FUNCTION update_updated_at();
CREATE TRIGGER trg_reviews_updated_at BEFORE UPDATE ON reviews FOR EACH ROW EXECUTE FUNCTION update_updated_at();

CREATE OR REPLACE FUNCTION update_course_students()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        UPDATE courses SET total_students = total_students + 1 WHERE id = NEW.course_id;
    ELSIF TG_OP = 'DELETE' THEN
        UPDATE courses SET total_students = GREATEST(total_students - 1, 0) WHERE id = OLD.course_id;
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_update_course_students AFTER INSERT OR DELETE ON enrollments FOR EACH ROW EXECUTE FUNCTION update_course_students();

CREATE OR REPLACE FUNCTION update_course_rating()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE courses SET average_rating = (SELECT COALESCE(AVG(rating), 0) FROM reviews WHERE course_id = NEW.course_id) WHERE id = NEW.course_id;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_update_course_rating AFTER INSERT OR UPDATE ON reviews FOR EACH ROW EXECUTE FUNCTION update_course_rating();

-- ============================================
-- VISTAS
-- ============================================

CREATE OR REPLACE VIEW v_courses_full AS
SELECT
    c.id, c.title, c.slug, c.description, c.short_description, c.thumbnail_url,
    c.level, c.is_free, c.price, c.status, c.is_published, c.total_students,
    c.average_rating, c.created_at, cat.name AS category_name, cat.slug AS category_slug,
    STRING_AGG(u.first_name || ' ' || u.last_name, ', ' ORDER BY ci.is_main DESC) AS instructors,
    COUNT(DISTINCT e.id) AS enrolled_count, COUNT(DISTINCT r.id) AS reviews_count
FROM courses c
LEFT JOIN categories cat ON c.category_id = cat.id
LEFT JOIN course_instructors ci ON c.id = ci.course_id
LEFT JOIN users u ON ci.instructor_id = u.id
LEFT JOIN enrollments e ON c.id = e.course_id
LEFT JOIN reviews r ON c.id = r.course_id
GROUP BY c.id, cat.name, cat.slug;

CREATE OR REPLACE VIEW v_instructor_stats AS
SELECT
    u.id AS instructor_id, u.first_name || ' ' || u.last_name AS instructor_name,
    u.email, u.profile_picture_url, u.bio, COUNT(DISTINCT c.id) AS total_courses,
    COUNT(DISTINCT CASE WHEN c.is_published THEN c.id END) AS published_courses,
    COUNT(DISTINCT e.id) AS total_students, COALESCE(AVG(c.average_rating), 0) AS average_rating,
    u.created_at AS instructor_since
FROM users u
INNER JOIN course_instructors ci ON u.id = ci.instructor_id
LEFT JOIN courses c ON ci.course_id = c.id
LEFT JOIN enrollments e ON c.id = e.course_id
GROUP BY u.id, u.first_name, u.last_name, u.email, u.profile_picture_url, u.bio, u.created_at;

-- ============================================
-- FUNCIONES
-- ============================================

CREATE OR REPLACE FUNCTION can_access_course(p_user_id UUID, p_course_id UUID)
RETURNS BOOLEAN AS $$
DECLARE
    course_is_free BOOLEAN;
BEGIN
    SELECT is_free INTO course_is_free FROM courses WHERE id = p_course_id;
    IF course_is_free THEN RETURN TRUE; END IF;
    RETURN EXISTS(SELECT 1 FROM enrollments WHERE user_id = p_user_id AND course_id = p_course_id);
END;
$$ LANGUAGE plpgsql;
