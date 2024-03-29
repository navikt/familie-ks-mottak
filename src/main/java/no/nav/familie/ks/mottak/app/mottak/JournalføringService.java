package no.nav.familie.ks.mottak.app.mottak;

import no.nav.familie.kontrakter.felles.Ressurs;
import no.nav.familie.kontrakter.felles.arkivering.ArkiverDokumentRequest;
import no.nav.familie.kontrakter.felles.dokarkiv.Dokument;
import no.nav.familie.kontrakter.felles.dokarkiv.Dokumenttype;
import no.nav.familie.kontrakter.felles.dokarkiv.FilType;
import no.nav.familie.ks.mottak.app.domene.Soknad;
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
import org.springframework.web.client.RestClientResponseException;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class JournalføringService extends BaseService {
    private static final Logger LOG = LoggerFactory.getLogger(JournalføringService.class);
    private static final String OAUTH2_CLIENT_CONFIG_KEY = "integrasjoner-clientcredentials";

    private final URI integrasjonServiceUri;
    private final SøknadService søknadService;

    public JournalføringService(
        @Value("${FAMILIE_INTEGRASJONER_API_URL}") String integrasjonServiceUri,
        RestTemplateBuilder restTemplateBuilderMedProxy,
        ClientConfigurationProperties clientConfigurationProperties,
        OAuth2AccessTokenService oAuth2AccessTokenService,
        SøknadService søknadService) {

        super(OAUTH2_CLIENT_CONFIG_KEY, restTemplateBuilderMedProxy, clientConfigurationProperties, oAuth2AccessTokenService);

        this.integrasjonServiceUri = URI.create(integrasjonServiceUri + "/arkiv/v2");
        this.søknadService = søknadService;
    }

    public String journalførSøknad(String søknadId) {
        Soknad søknad = søknadService.hentSoknad(søknadId);
        List<Dokument> dokumenter = søknad.getVedlegg().stream()
                                          .map(this::tilDokument)
                                          .collect(Collectors.toList());
        var arkiverDokumentRequest = new ArkiverDokumentRequest(søknad.getFnr(), true, dokumenter, null, null);
        String journalpostID = send(arkiverDokumentRequest);
        søknad.setJournalpostID(journalpostID);
        søknad.getVedlegg().clear();
        søknadService.lagreSøknad(søknad);
        return journalpostID;
    }

    private String send(ArkiverDokumentRequest arkiverDokumentRequest) {
        LOG.info("Sender søknad til {}", integrasjonServiceUri);
        try {
            ResponseEntity<Ressurs>
                response = postRequest(integrasjonServiceUri, arkiverDokumentRequest, Ressurs.class);

            if (response != null && response.getBody() != null && response.getBody().getData() != null) {
                return ((Map<String, String>) response.getBody().getData()).get("journalpostId");
            } else {
                throw new RuntimeException("Response mottat ved arkivering er null");
            }
        } catch (RestClientResponseException e) {
            LOG.warn("Innsending til dokarkiv feilet. Responskode: {}, body: {}", e.getRawStatusCode(), e.getResponseBodyAsString());
            throw new IllegalStateException("Innsending til dokarkiv feilet. Status: " + e.getRawStatusCode() + ", body: " + e.getResponseBodyAsString(), e);
        } catch (RestClientException e) {
            throw new IllegalStateException("Innsending til dokarkiv feilet.", e);
        }
    }

    private Dokument tilDokument(Vedlegg vedlegg) {
        Dokumenttype dokumentType = vedlegg.getFilnavn().equalsIgnoreCase("hovedskjema") ?
            Dokumenttype.KONTANTSTØTTE_SØKNAD : Dokumenttype.KONTANTSTØTTE_SØKNAD_VEDLEGG;
        return new Dokument(vedlegg.getData(), FilType.PDFA, vedlegg.getFilnavn(), null, dokumentType);
    }
}
