-- V5: Add created_by to courses for ownership tracking

ALTER TABLE courses ADD COLUMN created_by UUID REFERENCES users(id);

-- Migrate existing courses: assign to their main instructor (or first instructor)
UPDATE courses c
SET created_by = (
    SELECT ci.instructor_id
    FROM course_instructors ci
    WHERE ci.course_id = c.id AND ci.is_main = TRUE
    LIMIT 1
);

-- For courses without a main instructor, try any instructor
UPDATE courses c
SET created_by = (
    SELECT ci.instructor_id
    FROM course_instructors ci
    WHERE ci.course_id = c.id
    LIMIT 1
)
WHERE c.created_by IS NULL;

-- For courses still without created_by, assign to admin
UPDATE courses c
SET created_by = (
    SELECT id FROM users WHERE role = 'admin' LIMIT 1
)
WHERE c.created_by IS NULL;

-- Now make it NOT NULL
ALTER TABLE courses ALTER COLUMN created_by SET NOT NULL;

CREATE INDEX idx_courses_created_by ON courses(created_by);
