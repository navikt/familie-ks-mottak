DROP TABLE IF EXISTS MOTTAK;

CREATE TABLE HENVENDELSE
(
    id            bigint PRIMARY KEY,
    payload       json        not null,
    status        varchar(15) not null default 'UBEHANDLET',
    VERSJON       bigint               DEFAULT 0,
    OPPRETTET_TID TIMESTAMP(3)         DEFAULT localtimestamp
);
CREATE SEQUENCE HENVENDELSE_SEQ INCREMENT BY 50 START WITH 1000000 NO CYCLE;

create index ON HENVENDELSE (status);

CREATE TABLE HENVENDELSE_LOGG
(
    id             bigint PRIMARY KEY,
    henvendelse_id bigint REFERENCES HENVENDELSE NOT NULL,
    type           varchar(15)                   not null,
    node           varchar(100)                  not null,
    OPPRETTET_TID  TIMESTAMP(3) DEFAULT localtimestamp
);

create index ON HENVENDELSE_LOGG (henvendelse_id);
CREATE SEQUENCE HENVENDELSE_LOGG_SEQ INCREMENT BY 50 START WITH 1000000 NO CYCLE;
