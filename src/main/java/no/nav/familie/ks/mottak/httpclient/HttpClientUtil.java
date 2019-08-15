package no.nav.familie.ks.mottak.httpclient;

import java.net.http.HttpClient;
import java.time.Duration;

public final class HttpClientUtil {

    private HttpClientUtil() {
    }

    public static HttpClient create() {
        return HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }
}
