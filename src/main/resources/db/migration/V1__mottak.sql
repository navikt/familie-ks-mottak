DROP TABLE IF EXISTS MOTTAK;

CREATE TABLE HENVENDELSE
(
    id            SERIAL PRIMARY KEY,
    payload       json        not null,
    status        varchar(15) not null default 'UBEHANDLET',
    VERSJON       bigint               DEFAULT 0,
    OPPRETTET_TID TIMESTAMP(3)         DEFAULT localtimestamp
);

create index ON HENVENDELSE (status);

CREATE TABLE HENVENDELSE_LOGG
(
    id             SERIAL PRIMARY KEY,
    henvendelse_id bigint REFERENCES HENVENDELSE NOT NULL,
    type           varchar(15)                   not null,
    node           varchar(100)                  not null,
    OPPRETTET_TID  TIMESTAMP(3) DEFAULT localtimestamp
);

create index ON HENVENDELSE_LOGG (henvendelse_id);
