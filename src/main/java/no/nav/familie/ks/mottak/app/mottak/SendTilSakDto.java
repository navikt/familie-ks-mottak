package no.nav.familie.ks.mottak.app.mottak;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SendTilSakDto {
    private String søknadJson;

    private String saksnummer;

    private String journalpostID;

    @JsonCreator
    public SendTilSakDto(@JsonProperty("søknadJson") String søknadJson, @JsonProperty("saksnummer") String saksnummer,
                         @JsonProperty("journalpostID") String journalpostID) {
        this.søknadJson = søknadJson;
        this.saksnummer = saksnummer;
        this.journalpostID = journalpostID;
    }


    public String getSøknadJson() {
        return søknadJson;
    }

    public String getSaksnummer() {
        return saksnummer;
    }

    public String getJournalpostID() {
        return journalpostID;
    }
}
