INSERT INTO soknad(id,fnr) VALUES (1,'fnr');
INSERT INTO vedlegg(id, fk_soknad, filnavn, data) VALUES (1,1,'hovedskjema',X'abcdef');
INSERT INTO vedlegg(id, fk_soknad, filnavn, data) VALUES (2,1,'vedlegg',X'123456');
