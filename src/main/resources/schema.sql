CREATE TABLE IF NOT EXISTS app_user (
    id       TEXT PRIMARY KEY,
    name     TEXT NOT NULL,
    lastname TEXT NOT NULL,
    email    TEXT NOT NULL UNIQUE,
    password TEXT NOT NULL,
    role     TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS paciente (
    id   INTEGER PRIMARY KEY AUTOINCREMENT,
    nome TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS falta (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    paciente_id     INTEGER NOT NULL REFERENCES paciente (id),
    agendamento_id  INTEGER NOT NULL,
    data_referencia TEXT    NOT NULL
);

CREATE TABLE IF NOT EXISTS procedimento (
    id   INTEGER PRIMARY KEY AUTOINCREMENT,
    nome TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS agendamento (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    paciente_id     INTEGER NOT NULL REFERENCES paciente (id),
    profissional_id INTEGER NOT NULL,
    inicio          TEXT    NOT NULL,
    fim             TEXT    NOT NULL,
    status          TEXT    NOT NULL,
    realizado_em    TEXT
);

CREATE TABLE IF NOT EXISTS agendamento_procedimento (
    agendamento_id  INTEGER NOT NULL REFERENCES agendamento (id),
    procedimento_id INTEGER NOT NULL REFERENCES procedimento (id),
    ordem           INTEGER NOT NULL,
    executado       INTEGER NOT NULL DEFAULT 0,
    PRIMARY KEY (agendamento_id, procedimento_id)
);

CREATE INDEX IF NOT EXISTS idx_agendamento_profissional ON agendamento (profissional_id);
CREATE INDEX IF NOT EXISTS idx_agendamento_paciente     ON agendamento (paciente_id);
CREATE INDEX IF NOT EXISTS idx_falta_paciente           ON falta (paciente_id);
