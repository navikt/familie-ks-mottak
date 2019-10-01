CREATE TABLE SOKNAD (
    id             bigint PRIMARY KEY,
    soknad_json    text   not null,
    journalpost_id VARCHAR,
    saksnummer     VARCHAR
);

CREATE SEQUENCE SOKNAD_SEQ INCREMENT BY 50 START WITH 1000000 NO CYCLE;
