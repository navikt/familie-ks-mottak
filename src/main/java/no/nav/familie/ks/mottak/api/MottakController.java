package no.nav.familie.ks.mottak.api;

import no.nav.familie.ks.mottak.sts.StsRestClient;
import no.nav.security.oidc.api.ProtectedWithClaims;
import no.nav.security.oidc.api.Unprotected;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;


@RestController
@RequestMapping("/api")
@ProtectedWithClaims(issuer = "intern")
public class MottakController {

    private HttpClient client;
    private URI sakServiceUri;
    private StsRestClient stsRestClient;

    @Autowired
    public MottakController(@Value("${SOKNAD_KONTANTSTOTTE_SAK_API_URL}") URI sakServiceUri, @Autowired StsRestClient stsRestClient) {
        this.client = HttpClient.newHttpClient();
        this.sakServiceUri = URI.create(sakServiceUri + "/mottak/dokument");
        this.stsRestClient = stsRestClient;
    }

    @PostMapping(value = "/soknad", consumes = MediaType.APPLICATION_JSON_VALUE)
    public String mottaSoknad(@RequestBody String soknad) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(sakServiceUri)
                .POST(HttpRequest.BodyPublishers.ofString(soknad))
                .header("Authorization", "Bearer " + stsRestClient.getSystemOIDCToken())
                .header("Content-Type", "application/json")
                .timeout(Duration.ofMinutes(2))
                .build();

        try {
            client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return "OK";
    }

    @GetMapping("/ping")
    @Unprotected
    public String ping() {
        return "OK";
    }
}
