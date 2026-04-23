-- Prerequisits to access a course

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE course_prerequisites (
    id                     UUID          NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    course_id              UUID          NOT NULL,
    prerequisite_course_id UUID          NOT NULL,
    completion_threshold   INT           NOT NULL DEFAULT 80,
    created_at             TIMESTAMP     NOT NULL DEFAULT now(),

    CONSTRAINT fk_cp_course
        FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    CONSTRAINT fk_cp_prerequisite
        FOREIGN KEY (prerequisite_course_id) REFERENCES courses(id) ON DELETE CASCADE,
    CONSTRAINT uq_cp_course_prereq
        UNIQUE (course_id, prerequisite_course_id)
);