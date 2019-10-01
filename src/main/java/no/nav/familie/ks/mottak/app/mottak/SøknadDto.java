package no.nav.familie.ks.mottak.app.mottak;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class SøknadDto {

    @JsonProperty
    private final String fnr;

    @JsonProperty
    private final String soknad;

    @JsonProperty
    private final List<VedleggDto> vedlegg;

    public SøknadDto(@JsonProperty("fnr") String fnr, @JsonProperty("soknad") String søknad, @JsonProperty("vedlegg") List<VedleggDto> vedlegg) {
        this.fnr = fnr;
        this.soknad = søknad;
        this.vedlegg = vedlegg;
    }

    public String getFnr() {
        return this.fnr;
    }

    public String getSoknad() { return this.soknad; }

    public List<VedleggDto> getVedlegg() { return this.vedlegg; }
}
