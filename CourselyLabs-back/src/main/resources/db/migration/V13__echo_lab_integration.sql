-- V13: Integración con echo lab (entornos virtuales tipo "laboratorio")

-- 1. Token cifrado de echo por usuario.
--    Texto cifrado AES-GCM con clave del entorno (APP_ENCRYPTION_KEY).
--    Nullable: el usuario lo conecta voluntariamente desde su perfil.
ALTER TABLE users
    ADD COLUMN IF NOT EXISTS echo_token_encrypted TEXT,
    ADD COLUMN IF NOT EXISTS echo_token_updated_at TIMESTAMP;

-- 2. Extender lesson_blocks con un nuevo tipo "lab" y campos asociados.
--    Reemplazamos el CHECK para añadir 'lab' a los tipos permitidos.
ALTER TABLE lesson_blocks
    DROP CONSTRAINT IF EXISTS lesson_blocks_type_check;

ALTER TABLE lesson_blocks
    ADD CONSTRAINT lesson_blocks_type_check
    CHECK (type IN ('text', 'video', 'pdf', 'quiz', 'project', 'open_text', 'lab'));

ALTER TABLE lesson_blocks
    ADD COLUMN IF NOT EXISTS lab_provider VARCHAR(20),     -- 'echo' (futuro: otros)
    ADD COLUMN IF NOT EXISTS lab_template_id INTEGER,      -- id de la VM plantilla en el proveedor
    ADD COLUMN IF NOT EXISTS lab_instructions TEXT;        -- enunciado markdown de qué hacer en la VM

-- 3. Log mínimo de eventos del laboratorio (start/stop/console).
--    Sirve para auditoría y ver actividad por curso/usuario.
CREATE TABLE IF NOT EXISTS lab_session_events (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    block_id    UUID NOT NULL REFERENCES lesson_blocks(id) ON DELETE CASCADE,
    action      VARCHAR(20) NOT NULL CHECK (action IN ('start', 'stop', 'console', 'error')),
    detail      TEXT,                                       -- mensaje libre (error, ticket id, …)
    created_at  TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_lab_session_events_user_id ON lab_session_events(user_id);
CREATE INDEX idx_lab_session_events_block_id ON lab_session_events(block_id);
CREATE INDEX idx_lab_session_events_created_at ON lab_session_events(created_at DESC);
