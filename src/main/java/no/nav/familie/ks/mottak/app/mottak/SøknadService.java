package no.nav.familie.ks.mottak.app.mottak;

import no.nav.familie.http.client.HttpClientUtil;
import no.nav.familie.http.client.HttpRequestUtil;
import no.nav.familie.http.sts.StsRestClient;
import org.eclipse.jetty.http.HttpHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class SøknadService {

    private static final Logger LOG = LoggerFactory.getLogger(SøknadService.class);

    private final StsRestClient stsRestClient;
    private final URI sakServiceUri;
    private final HttpClient client;

    public SøknadService(@Value("${FAMILIE_KS_SAK_API_URL}") URI sakServiceUri, StsRestClient stsRestClient) {
        this.client = HttpClientUtil.create();
        this.sakServiceUri = URI.create(sakServiceUri + "/mottak/dokument");
        this.stsRestClient = stsRestClient;
    }

    public void sendTilSak(String søknad) throws IOException, InterruptedException {
        HttpRequest request = HttpRequestUtil.createRequest("Bearer " + stsRestClient.getSystemOIDCToken())
                .header(HttpHeader.CONTENT_TYPE.asString(), "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(søknad))
                .uri(sakServiceUri)
                .build();
        LOG.info("Sender søknad til " + sakServiceUri);

        HttpResponse response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != HttpStatus.OK.value()) {
            LOG.warn("Innsending til sak feilet. Responskode: {}. Feilmelding: {}", response.statusCode(), response.body());

            throw new IllegalStateException("Innsending til sak feilet.");
        }
    }
}
