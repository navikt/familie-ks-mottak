package no.nav.familie.ks.mottak.httpclient;

import no.nav.familie.ks.mottak.config.filter.LogFilter;

public enum NavHttpHeaders {
    NAV_PERSONIDENT("Nav-Personident"),
    NAV_CALLID(LogFilter.PREFERRED_NAV_CALL_ID_HEADER_NAME),
    NAV_CONSUMER_ID(LogFilter.CONSUMER_ID_HEADER_NAME);

    private final String header;

    NavHttpHeaders(String header) {
        this.header = header;
    }

    public String asString() {
        return header;
    }
}
