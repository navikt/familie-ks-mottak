package no.nav.familie.ks.mottak.config.filter;

import java.util.UUID;

public final class IdUtils {
    private IdUtils() {
    }

    public static String generateId() {
        UUID uuid = UUID.randomUUID();
        return Long.toHexString(uuid.getMostSignificantBits()) + Long.toHexString(uuid.getLeastSignificantBits());
    }
}

