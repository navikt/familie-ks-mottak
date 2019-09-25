package no.nav.familie.ks.mottak.app.mottak;

import com.fasterxml.jackson.annotation.JsonProperty;

public class VedleggDto {

    @JsonProperty
    private final String tittel;

    @JsonProperty
    private final byte[] data;

    public VedleggDto(
        @JsonProperty("data") byte[] data,
        @JsonProperty("tittel") String tittel) {
        this.data = data;
        this.tittel = tittel;
    }

    public byte[] getData() {
        return data;
    }

    public String getTittel() {
        return tittel;
    }
}
