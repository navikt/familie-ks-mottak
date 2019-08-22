package no.nav.familie.ks.mottak.app.mottak.domene;

import java.util.List;
import java.util.Objects;

public enum LoggType {
    UBEHANDLET, KLAR_TIL_PLUKK, PLUKKET, BEHANDLER, FERDIG, FEILET;

    public static LoggType neste(LoggType type) {
        if (List.of(UBEHANDLET, KLAR_TIL_PLUKK).contains(type)) {
            return PLUKKET;
        }
        if (Objects.equals(PLUKKET, type)) {
            return BEHANDLER;
        }
        if (Objects.equals(BEHANDLER, type)) {
            return FERDIG;
        }
        if (Objects.equals(FEILET, type)) {
            return KLAR_TIL_PLUKK;
        }
        if (Objects.equals(FERDIG, type)) {
            return FERDIG;
        }
        throw new IllegalStateException("Ukjent type " + type);
    }
}
