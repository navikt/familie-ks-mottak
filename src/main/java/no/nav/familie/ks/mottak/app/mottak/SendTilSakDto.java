package no.nav.familie.ks.mottak.app.mottak;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SendTilSakDto {
    private String søknadJson;

    private String saksnummer;

    @JsonCreator
    public SendTilSakDto(@JsonProperty("søknadJson") String søknadJson, @JsonProperty("saksnummer") String saksnummer) {
        this.søknadJson = søknadJson;
        this.saksnummer = saksnummer;
    }


    public String getSøknadJson() {
        return søknadJson;
    }

    public String getSaksnummer() {
        return saksnummer;
    }
}
