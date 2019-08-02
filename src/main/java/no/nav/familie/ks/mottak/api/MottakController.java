package no.nav.familie.ks.mottak.api;

import no.nav.familie.ks.mottak.sts.StsRestClient;
import no.nav.security.oidc.OIDCConstants;
import no.nav.security.oidc.api.ProtectedWithClaims;
import no.nav.security.oidc.context.OIDCValidationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@RestController
@RequestMapping("/api")
@ProtectedWithClaims(issuer = "selvbetjening", claimMap = {"acr=Level4"})
public class MottakController {

    private HttpClient client;
    private URI sakServiceUri;
    private StsRestClient stsRestClient;
    private static final String SELVBETJENING = "selvbetjening";
    private static final Logger LOG = LoggerFactory.getLogger(MottakController.class);

    @Autowired
    public MottakController(@Value("${FAMILIE_KS_SAK_API_URL}") URI sakServiceUri, @Autowired StsRestClient stsRestClient) {
        this.client = HttpClient.newHttpClient();
        this.sakServiceUri = URI.create(sakServiceUri + "/mottak/dokument");
        this.stsRestClient = stsRestClient;
    }

    @PostMapping(value = "/soknad", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity mottaSoknad(@RequestBody String soknad) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(sakServiceUri)
            .POST(HttpRequest.BodyPublishers.ofString(soknad))
            .header("Authorization", "Bearer " + stsRestClient.getSystemOIDCToken())
            .header("Content-Type", "application/json")
            .header("Nav-Personident", hentFnrFraToken())
            .timeout(Duration.ofMinutes(2))
            .build();
        LOG.info("Sender søknad til " + sakServiceUri);

        HttpResponse response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != HttpStatus.OK.value()) {
            LOG.warn("Innsending til sak feilet. Responskode: {}. Feilmelding: {}", response.statusCode(), response.body());
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity(HttpStatus.OK);
    }

    private static String hentFnrFraToken() {
        OIDCValidationContext context = (OIDCValidationContext) RequestContextHolder.currentRequestAttributes().getAttribute(OIDCConstants.OIDC_VALIDATION_CONTEXT, RequestAttributes.SCOPE_REQUEST);
        context = context != null ? context : new OIDCValidationContext();
        return context.getClaims(SELVBETJENING).getClaimSet().getSubject();
    }
}
