-- V3: Lesson progress tracking

CREATE TABLE IF NOT EXISTS lesson_progress (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id                 UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    lesson_id               UUID NOT NULL REFERENCES lessons(id) ON DELETE CASCADE,
    is_completed            BOOLEAN NOT NULL DEFAULT FALSE,
    completed_at            TIMESTAMP,
    last_position_seconds   INTEGER NOT NULL DEFAULT 0,
    created_at              TIMESTAMP NOT NULL DEFAULT now(),
    updated_at              TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (user_id, lesson_id)
);

CREATE INDEX idx_lesson_progress_user_id ON lesson_progress(user_id);
CREATE INDEX idx_lesson_progress_lesson_id ON lesson_progress(lesson_id);
