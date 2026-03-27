-- V6: Add rejection_reason to courses

ALTER TABLE courses ADD COLUMN IF NOT EXISTS rejection_reason TEXT;
