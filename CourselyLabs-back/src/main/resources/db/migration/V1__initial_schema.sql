-- V1: Initial schema — CourselyLabs
-- Captures the existing database structure managed by Hibernate ddl-auto

-- Users
CREATE TABLE IF NOT EXISTS users (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email           VARCHAR(255) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    first_name      VARCHAR(100) NOT NULL,
    last_name       VARCHAR(100) NOT NULL,
    role            VARCHAR(20)  NOT NULL DEFAULT 'user',
    bio             TEXT,
    profile_picture_url VARCHAR(500),
    is_verified     BOOLEAN NOT NULL DEFAULT FALSE,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP NOT NULL DEFAULT now()
);

-- Categories
CREATE TABLE IF NOT EXISTS categories (
    id          SERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    slug        VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    created_at  TIMESTAMP NOT NULL DEFAULT now()
);

-- Courses
CREATE TABLE IF NOT EXISTS courses (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title             VARCHAR(255) NOT NULL,
    slug              VARCHAR(255) NOT NULL UNIQUE,
    description       TEXT NOT NULL,
    short_description VARCHAR(500),
    thumbnail_url     VARCHAR(500),
    category_id       INTEGER REFERENCES categories(id),
    level             VARCHAR(20),
    is_free           BOOLEAN NOT NULL DEFAULT FALSE,
    price             NUMERIC(10, 2),
    status            VARCHAR(20) NOT NULL DEFAULT 'draft',
    is_published      BOOLEAN NOT NULL DEFAULT FALSE,
    published_at      TIMESTAMP,
    total_students    INTEGER NOT NULL DEFAULT 0,
    average_rating    NUMERIC(3, 2) NOT NULL DEFAULT 0.00,
    created_at        TIMESTAMP NOT NULL DEFAULT now(),
    updated_at        TIMESTAMP NOT NULL DEFAULT now()
);

-- Course instructors (many-to-many)
CREATE TABLE IF NOT EXISTS course_instructors (
    id            SERIAL PRIMARY KEY,
    course_id     UUID NOT NULL REFERENCES courses(id),
    instructor_id UUID NOT NULL REFERENCES users(id),
    is_main       BOOLEAN NOT NULL DEFAULT FALSE,
    created_at    TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (course_id, instructor_id)
);

-- Enrollments
CREATE TABLE IF NOT EXISTS enrollments (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id          UUID NOT NULL REFERENCES users(id),
    course_id        UUID NOT NULL REFERENCES courses(id),
    access_type      VARCHAR(20) NOT NULL,
    enrolled_at      TIMESTAMP NOT NULL DEFAULT now(),
    last_accessed_at TIMESTAMP,
    UNIQUE (user_id, course_id)
);

-- Reviews
CREATE TABLE IF NOT EXISTS reviews (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    course_id  UUID NOT NULL REFERENCES courses(id),
    user_id    UUID NOT NULL REFERENCES users(id),
    rating     INTEGER NOT NULL,
    comment    TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (course_id, user_id)
);

-- Refresh tokens
CREATE TABLE IF NOT EXISTS refresh_tokens (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID NOT NULL REFERENCES users(id),
    token      VARCHAR(500) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    revoked    BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

-- Role constraint
ALTER TABLE users DROP CONSTRAINT IF EXISTS users_role_check;
ALTER TABLE users ADD CONSTRAINT users_role_check CHECK (role IN ('admin', 'user', 'premium'));
