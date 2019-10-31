package no.nav.familie.ks.mottak.app.mottak;

import no.nav.familie.ks.kontrakter.dokarkiv.api.*;
import no.nav.familie.ks.mottak.app.domene.Soknad;
import no.nav.familie.ks.mottak.app.domene.SøknadRepository;
import no.nav.familie.ks.mottak.app.domene.Vedlegg;
import no.nav.familie.ks.mottak.config.BaseService;
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService;
import no.nav.security.token.support.client.spring.ClientConfigurationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

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
    private final SøknadRepository søknadRepository;

    public JournalføringService(
        @Value("${FAMILIE_KS_OPPSLAG_API_URL}") URI oppslagServiceUri,
        RestTemplateBuilder restTemplateBuilder,
        ClientConfigurationProperties clientConfigurationProperties,
        OAuth2AccessTokenService oAuth2AccessTokenService,
        SøknadRepository søknadRepository,
        SøknadService søknadService) {

        super(OAUTH2_CLIENT_CONFIG_KEY, restTemplateBuilder, clientConfigurationProperties, oAuth2AccessTokenService);

        this.oppslagServiceUri = URI.create(oppslagServiceUri + "/arkiv");
        this.søknadService = søknadService;
        this.søknadRepository = søknadRepository;
    }

    public void journalførSøknad(String søknadId) {
        Soknad søknad = søknadService.hentSoknad(søknadId);
        List<Dokument> dokumenter = søknad.getVedlegg().stream()
                                          .map(this::tilDokument)
                                          .collect(Collectors.toList());
        var arkiverDokumentRequest = new ArkiverDokumentRequest(søknad.getFnr(), true, dokumenter);
        String journalpostID = send(arkiverDokumentRequest).getJournalpostId();
        søknad.setJournalpostID(journalpostID);
        søknadRepository.save(søknad);
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
