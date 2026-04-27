-- V11: Lesson content blocks (multi-content per lesson) + relink assessments to blocks

-- 1. New table for lesson blocks (ordered list of content per lesson)
CREATE TABLE IF NOT EXISTS lesson_blocks (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    lesson_id       UUID NOT NULL REFERENCES lessons(id) ON DELETE CASCADE,
    type            VARCHAR(20) NOT NULL CHECK (type IN ('text', 'video', 'pdf', 'quiz', 'project', 'open_text')),
    position        INTEGER NOT NULL DEFAULT 0,
    text_content    TEXT,
    video_url       VARCHAR(500),
    pdf_url         VARCHAR(500),
    created_at      TIMESTAMP NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_lesson_blocks_lesson_id ON lesson_blocks(lesson_id);

-- 2. Add block_id to assessments (replaces lesson_id 1:1 with block 1:1)
ALTER TABLE assessments ADD COLUMN IF NOT EXISTS block_id UUID REFERENCES lesson_blocks(id) ON DELETE CASCADE;

-- 3. Migrate existing lessons → 1 block per lesson (preserves all data)
INSERT INTO lesson_blocks (id, lesson_id, type, position, text_content, video_url, pdf_url)
SELECT
    gen_random_uuid(),
    l.id,
    l.type,
    0,
    CASE WHEN l.type = 'text' THEN l.content_text ELSE NULL END,
    CASE WHEN l.type = 'video' THEN l.content_url ELSE NULL END,
    CASE WHEN l.type = 'pdf' THEN l.content_url ELSE NULL END
FROM lessons l
WHERE l.type IS NOT NULL;

-- 4. Link existing assessments to their corresponding new block
UPDATE assessments a
SET block_id = (
    SELECT b.id FROM lesson_blocks b
    WHERE b.lesson_id = a.lesson_id
    LIMIT 1
)
WHERE a.lesson_id IS NOT NULL;

-- 5. Drop old assessment.lesson_id constraint, allow lesson_id to be nullable
ALTER TABLE assessments DROP CONSTRAINT IF EXISTS assessments_lesson_id_key;
ALTER TABLE assessments ALTER COLUMN lesson_id DROP NOT NULL;

-- 6. Make block_id unique (1 assessment per block)
ALTER TABLE assessments ADD CONSTRAINT assessments_block_id_key UNIQUE (block_id);

-- 7. Lesson type is no longer the source of truth; relax the constraint and make nullable
ALTER TABLE lessons DROP CONSTRAINT IF EXISTS lessons_type_check;
ALTER TABLE lessons ALTER COLUMN type DROP NOT NULL;
