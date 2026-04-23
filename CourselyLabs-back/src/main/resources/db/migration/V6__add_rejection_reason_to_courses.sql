-- V6: Add rejection_reason and 'rejected' status to courses

ALTER TABLE courses ADD COLUMN IF NOT EXISTS rejection_reason TEXT;

-- Add 'rejected' to status CHECK constraint
ALTER TABLE courses DROP CONSTRAINT IF EXISTS courses_status_check;
ALTER TABLE courses ADD CONSTRAINT courses_status_check
    CHECK (status IN ('draft', 'pending_review', 'published', 'archived', 'rejected'));
