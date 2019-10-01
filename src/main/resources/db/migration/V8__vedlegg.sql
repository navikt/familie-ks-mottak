CREATE TABLE VEDLEGG (
    id             bigint PRIMARY KEY,
    fk_soknad      bigint REFERENCES SOKNAD NOT NULL,
    filnavn        VARCHAR  not null,
    type           VARCHAR,
    data           bytea
);

CREATE SEQUENCE VEDLEGG_SEQ INCREMENT BY 50 START WITH 1000000 NO CYCLE;
