DELETE FROM vedlegg
WHERE fk_soknad IN (
    SELECT cast(payload AS bigint) FROM TASK
    WHERE type in ('journalførSøknad', 'hentJournalpostIdFraJoarkTask')
      AND status = 'FERDIG'
);
