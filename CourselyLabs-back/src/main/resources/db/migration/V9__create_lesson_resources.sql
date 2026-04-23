-- V8: Lesson resources (downloadable files) + per-course size tracking

CREATE TABLE IF NOT EXISTS lesson_resources (
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

-- Track total size of uploaded files per course (resources + thumbnail + lesson content)
ALTER TABLE courses ADD COLUMN IF NOT EXISTS storage_bytes BIGINT NOT NULL DEFAULT 0;
