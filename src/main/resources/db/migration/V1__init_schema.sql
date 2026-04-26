-- Table des utilisateurs (candidats + admins)
CREATE TABLE users (
    id         BIGSERIAL    PRIMARY KEY,
    nom        VARCHAR(100) NOT NULL,
    email      VARCHAR(150) NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    role       VARCHAR(20)  NOT NULL DEFAULT 'CANDIDATE', -- CANDIDATE | ADMIN
    actif      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- Table des CV uploadés par les candidats
CREATE TABLE cvs (
    id          BIGSERIAL    PRIMARY KEY,
    user_id     BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    file_path   VARCHAR(500) NOT NULL,
    uploaded_at TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- Table des sessions d'entretien
CREATE TABLE interview_sessions (
    id          BIGSERIAL   PRIMARY KEY,
    user_id     BIGINT      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    poste       VARCHAR(150),
    type        VARCHAR(50),  -- TECHNIQUE | RH | COMPORTEMENTAL | MIXTE
    difficulte  VARCHAR(20),  -- JUNIOR | MID | SENIOR
    langue      VARCHAR(10),  -- FR | EN
    nb_questions INT,
    score_global NUMERIC(4,2),
    statut      VARCHAR(20)  NOT NULL DEFAULT 'EN_COURS', -- EN_COURS | TERMINE
    created_at  TIMESTAMP   NOT NULL DEFAULT NOW()
);

-- Table des questions posées lors d'une session
CREATE TABLE interview_questions (
    id          BIGSERIAL    PRIMARY KEY,
    session_id  BIGINT       NOT NULL REFERENCES interview_sessions(id) ON DELETE CASCADE,
    question    TEXT         NOT NULL,
    reponse     TEXT,
    score       NUMERIC(4,2),
    feedback    TEXT,
    ordre       INT          NOT NULL
);