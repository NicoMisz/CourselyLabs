-- V10: Assessments (quiz, project, open_text)

-- Extend lesson types to include assessments
ALTER TABLE lessons DROP CONSTRAINT IF EXISTS lessons_type_check;
ALTER TABLE lessons ADD CONSTRAINT lessons_type_check
    CHECK (type IN ('video', 'text', 'pdf', 'audio', 'quiz', 'project', 'open_text'));

-- Main assessment entity (1:1 with a lesson)
CREATE TABLE IF NOT EXISTS assessments (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    lesson_id           UUID NOT NULL UNIQUE REFERENCES lessons(id) ON DELETE CASCADE,
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

-- Quiz questions (only for type='quiz')
CREATE TABLE IF NOT EXISTS quiz_questions (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    assessment_id   UUID NOT NULL REFERENCES assessments(id) ON DELETE CASCADE,
    question_text   TEXT NOT NULL,
    position        INTEGER NOT NULL DEFAULT 0,
    points          INTEGER NOT NULL DEFAULT 1,
    created_at      TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_quiz_questions_assessment_id ON quiz_questions(assessment_id);

-- Quiz options
CREATE TABLE IF NOT EXISTS quiz_options (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    question_id     UUID NOT NULL REFERENCES quiz_questions(id) ON DELETE CASCADE,
    option_text     TEXT NOT NULL,
    is_correct      BOOLEAN NOT NULL DEFAULT FALSE,
    explanation     TEXT,
    position        INTEGER NOT NULL DEFAULT 0
);

CREATE INDEX idx_quiz_options_question_id ON quiz_options(question_id);

-- Attempts (one per try, per user, per assessment)
CREATE TABLE IF NOT EXISTS assessment_attempts (
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

-- Quiz answers (per attempt, per question)
CREATE TABLE IF NOT EXISTS quiz_answers (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    attempt_id          UUID NOT NULL REFERENCES assessment_attempts(id) ON DELETE CASCADE,
    question_id         UUID NOT NULL REFERENCES quiz_questions(id) ON DELETE CASCADE,
    selected_option_id  UUID REFERENCES quiz_options(id) ON DELETE SET NULL,
    UNIQUE (attempt_id, question_id)
);

CREATE INDEX idx_quiz_answers_attempt_id ON quiz_answers(attempt_id);

-- Submissions (project / open_text)
CREATE TABLE IF NOT EXISTS submissions (
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
