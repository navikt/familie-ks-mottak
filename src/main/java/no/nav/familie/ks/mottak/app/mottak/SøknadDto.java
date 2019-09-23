package no.nav.familie.ks.mottak.app.mottak;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class SøknadDto {

    @JsonProperty
    private final String soknad;

    @JsonProperty
    private final List<VedleggDto> vedlegg;

    public SøknadDto(@JsonProperty("soknad") String søknad, @JsonProperty("vedlegg") List<VedleggDto> vedlegg) {
        this.soknad = søknad;
        this.vedlegg = vedlegg;
    }

    public String getSoknad() { return this.soknad; }

    public List<VedleggDto> getVedlegg() { return this.vedlegg; }
}
