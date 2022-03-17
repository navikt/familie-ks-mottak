CREATE TABLE IF NOT EXISTS "task" (
    id BIGINT NOT NULL CONSTRAINT "henvendelse_pkey" PRIMARY KEY AUTO_INCREMENT,
    payload TEXT NOT NULL,
    status VARCHAR DEFAULT 'UBEHANDLET':: CHARACTER VARYING NOT NULL,
    versjon BIGINT DEFAULT 0,
    opprettet_tid TIMESTAMP(3) DEFAULT LOCALTIMESTAMP,
    type          VARCHAR NOT NULL,
    "metadata"      VARCHAR,
    trigger_tid   TIMESTAMP DEFAULT LOCALTIMESTAMP,
    avvikstype    VARCHAR
);

CREATE INDEX IF NOT EXISTS "henvendelse_status_idx" ON "task" (status);

CREATE TABLE IF NOT EXISTS "task_logg" (
    id BIGINT NOT NULL CONSTRAINT "henvendelse_logg_pkey" PRIMARY KEY AUTO_INCREMENT,
    "task_id" BIGINT NOT NULL CONSTRAINT "henvendelse_logg_henvendelse_id_fkey"
            REFERENCES "task",
    type VARCHAR  NOT NULL,
    node          VARCHAR NOT NULL,
    opprettet_tid TIMESTAMP(3) DEFAULT LOCALTIMESTAMP,
    melding       TEXT,
    endret_av     VARCHAR DEFAULT 'VL':: CHARACTER VARYING
);

CREATE INDEX IF NOT EXISTS "henvendelse_logg_henvendelse_id_idx"
    ON "task_logg" ("task_id");