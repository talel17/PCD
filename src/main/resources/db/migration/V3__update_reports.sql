-- Supprimer la table interview_questions
DROP TABLE IF EXISTS interview_questions;

-- Ajouter les colonnes rapport IA dans reports
ALTER TABLE reports
    ADD COLUMN IF NOT EXISTS candidate_name       VARCHAR(200),
    ADD COLUMN IF NOT EXISTS overall_score        NUMERIC(4,2),
    ADD COLUMN IF NOT EXISTS score_justification  TEXT,
    ADD COLUMN IF NOT EXISTS strengths            TEXT,
    ADD COLUMN IF NOT EXISTS weaknesses           TEXT,
    ADD COLUMN IF NOT EXISTS tips                 TEXT,
    ADD COLUMN IF NOT EXISTS summary              TEXT,
    ADD COLUMN IF NOT EXISTS room_name            VARCHAR(200),
    ADD COLUMN IF NOT EXISTS interview_date       VARCHAR(50);