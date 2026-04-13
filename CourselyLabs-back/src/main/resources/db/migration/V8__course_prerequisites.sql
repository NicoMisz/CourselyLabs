-- Prerequisits to access a course

CREATE TABLE course_prerequisites (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    course_id UUID NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    prerequisite_course_id UUID NOT NULL REFERENCES courses(id) ON DELETE RESTRICT,
    completion_threshold INT NOT NULL DEFAULT 80
        CONSTRAINT chk_threshold_range CHECK (completion_threshold BETWEEN 70 AND 85),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_course_prerequisite UNIQUE (course_id, prerequisite_course_id),
    CONSTRAINT chk_no_self_prerequisite CHECK (course_id <> prerequisite_course_id)
);

CREATE INDEX idx_course_prerequisites_course_id ON course_prerequisites(course_id);
CREATE INDEX idx_course_prerequisites_prerequisite_id ON course_prerequisites(prerequisite_course_id);