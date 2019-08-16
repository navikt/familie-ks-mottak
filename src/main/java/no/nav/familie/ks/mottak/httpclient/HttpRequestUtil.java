package no.nav.familie.ks.mottak.httpclient;

import no.nav.familie.log.IdUtils;
import no.nav.familie.log.mdc.MDCConstants;
import org.eclipse.jetty.http.HttpHeader;
import org.slf4j.MDC;

import java.net.http.HttpRequest;
import java.time.Duration;

public final class HttpRequestUtil {
    private HttpRequestUtil() {
    }

    public static HttpRequest.Builder createRequest(String authorizationHeader) {
        return HttpRequest.newBuilder()
                .header(HttpHeader.AUTHORIZATION.asString(), authorizationHeader)
                .header(NavHttpHeaders.NAV_CALLID.asString(), hentEllerOpprettCallId())
                .timeout(Duration.ofSeconds(30));
    }

    private static String hentEllerOpprettCallId() {
        final var callId = MDC.get(MDCConstants.MDC_CALL_ID);
        if (callId == null) {
            return IdUtils.generateId();
        }
        return callId;
    }
}
