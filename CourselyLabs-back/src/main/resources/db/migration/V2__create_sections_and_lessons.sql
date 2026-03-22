-- V2: Sections and Lessons

-- Sections (chapters within a course)
CREATE TABLE IF NOT EXISTS sections (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    course_id       UUID NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    title           VARCHAR(255) NOT NULL,
    description     TEXT,
    position        INTEGER NOT NULL DEFAULT 0,
    created_at      TIMESTAMP NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_sections_course_id ON sections(course_id);

-- Lessons (individual content items within a section)
CREATE TABLE IF NOT EXISTS lessons (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    section_id      UUID NOT NULL REFERENCES sections(id) ON DELETE CASCADE,
    title           VARCHAR(255) NOT NULL,
    description     TEXT,
    type            VARCHAR(20) NOT NULL,
    content_url     VARCHAR(1000),
    content_text    TEXT,
    duration        INTEGER,
    position        INTEGER NOT NULL DEFAULT 0,
    is_free         BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_lessons_section_id ON lessons(section_id);

-- Type constraint
ALTER TABLE lessons ADD CONSTRAINT lessons_type_check
    CHECK (type IN ('video', 'text', 'pdf', 'audio'));
