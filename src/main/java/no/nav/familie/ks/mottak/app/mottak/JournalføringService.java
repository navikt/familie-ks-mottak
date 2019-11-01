package no.nav.familie.ks.mottak.app.mottak;

import no.nav.familie.http.client.NavHttpHeaders;
import no.nav.familie.http.sts.StsRestClient;
import no.nav.familie.ks.kontrakter.dokarkiv.api.*;
import no.nav.familie.ks.mottak.app.domene.Soknad;
import no.nav.familie.ks.mottak.app.domene.SøknadRepository;
import no.nav.familie.ks.mottak.app.domene.Vedlegg;
import no.nav.familie.ks.mottak.config.BaseService;
import no.nav.familie.log.mdc.MDCConstants;
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService;
import no.nav.security.token.support.client.spring.ClientConfigurationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.http.HttpRequest;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class JournalføringService extends BaseService {
    private static final Logger LOG = LoggerFactory.getLogger(JournalføringService.class);
    private static final String OAUTH2_CLIENT_CONFIG_KEY = "ks-oppslag-clientcredentials";

    private final URI oppslagServiceUri;
    private final SøknadService søknadService;
    private final StsRestClient stsRestClient;
    private final RestTemplate restTemplate;

    public JournalføringService(
        @Value("${FAMILIE_KS_OPPSLAG_API_URL}") String oppslagServiceUri,
        RestTemplateBuilder restTemplateBuilder,
        RestTemplate restTemplate,
        StsRestClient stsRestClient,
        ClientConfigurationProperties clientConfigurationProperties,
        OAuth2AccessTokenService oAuth2AccessTokenService,
        SøknadService søknadService) {

        super(OAUTH2_CLIENT_CONFIG_KEY, restTemplateBuilder, clientConfigurationProperties, oAuth2AccessTokenService);

        this.oppslagServiceUri = URI.create(oppslagServiceUri + "/arkiv");
        this.søknadService = søknadService;
        this.stsRestClient = stsRestClient;
        this.restTemplate = restTemplate;
    }

    public void journalførSøknad(String søknadId) {
        Soknad søknad = søknadService.hentSoknad(søknadId);
        List<Dokument> dokumenter = søknad.getVedlegg().stream()
                                          .map(this::tilDokument)
                                          .collect(Collectors.toList());
        var arkiverDokumentRequest = new ArkiverDokumentRequest(søknad.getFnr(), true, dokumenter);
        String journalpostID = send(arkiverDokumentRequest).getJournalpostId();
        søknad.setJournalpostID(journalpostID);
        søknadService.lagreSøknad(søknad);
    }

    private <T> ResponseEntity<T> postRequest(URI uri, java.net.http.HttpRequest.BodyPublisher requestBody, Class<T> responseType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(stsRestClient.getSystemOIDCToken());
        headers.add(NavHttpHeaders.NAV_CALLID.asString(), MDC.get(MDCConstants.MDC_CALL_ID));

        return restTemplate.exchange(uri, HttpMethod.POST, new HttpEntity<>(requestBody, headers), responseType);
    }

    private ArkiverDokumentResponse send(ArkiverDokumentRequest arkiverDokumentRequest) {
        String payload = ArkiverDokumentRequestKt.toJson(arkiverDokumentRequest);
        LOG.info("Sender søknad til " + oppslagServiceUri);
        try {
            ResponseEntity<String>
                response = postRequest(oppslagServiceUri, HttpRequest.BodyPublishers.ofString(payload), String.class);

            if (response.getStatusCode().isError()) {
                LOG.warn("Innsending til dokarkiv feilet. Responskode: {}, body: {}", response.getStatusCode(), response.getBody());

                throw new IllegalStateException("Innsending til dokarkiv feilet. Responskode: " + response.getStatusCode() + ", body: " + response.getBody());
            } else {
                return ArkiverDokumentResponseKt.toArkiverDokumentResponse(Objects.requireNonNull(response.getBody()));
            }
        } catch (RestClientException e) {
            throw new IllegalStateException("Innsending til dokarkiv feilet.", e);
        }
    }

    private Dokument tilDokument(Vedlegg vedlegg) {
        DokumentType dokumentType = vedlegg.getFilnavn().equalsIgnoreCase("hovedskjema") ?
            DokumentType.KONTANTSTØTTE_SØKNAD : DokumentType.KONTANTSTØTTE_SØKNAD_VEDLEGG;
        return new Dokument(vedlegg.getData(), FilType.PDFA, vedlegg.getFilnavn(), dokumentType);
    }
}
