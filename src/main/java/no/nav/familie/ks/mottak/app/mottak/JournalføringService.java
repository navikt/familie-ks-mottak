package no.nav.familie.ks.mottak.app.mottak;

import no.nav.familie.http.client.NavHttpHeaders;
import no.nav.familie.ks.kontrakter.dokarkiv.api.*;
import no.nav.familie.ks.kontrakter.sak.Ressurs;
import no.nav.familie.ks.mottak.app.domene.Soknad;
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
import org.springframework.web.client.RestClientResponseException;

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

    public JournalføringService(
        @Value("${FAMILIE_KS_OPPSLAG_API_URL}") String oppslagServiceUri,
        RestTemplateBuilder restTemplateBuilderMedProxy,
        ClientConfigurationProperties clientConfigurationProperties,
        OAuth2AccessTokenService oAuth2AccessTokenService,
        SøknadService søknadService) {

        super(OAUTH2_CLIENT_CONFIG_KEY, restTemplateBuilderMedProxy, clientConfigurationProperties, oAuth2AccessTokenService);

        this.oppslagServiceUri = URI.create(oppslagServiceUri + "/arkiv/v1");
        this.søknadService = søknadService;
    }

    public void journalførSøknad(String søknadId) {
        Soknad søknad = søknadService.hentSoknad(søknadId);
        List<Dokument> dokumenter = søknad.getVedlegg().stream()
                                          .map(this::tilDokument)
                                          .collect(Collectors.toList());
        var arkiverDokumentRequest = new ArkiverDokumentRequest(søknad.getFnr(), true, dokumenter);
        String journalpostID = send(arkiverDokumentRequest).getJournalpostId();
        søknad.setJournalpostID(journalpostID);
        søknad.getVedlegg().clear();
        søknadService.lagreSøknad(søknad);
    }

    private ArkiverDokumentResponse send(ArkiverDokumentRequest arkiverDokumentRequest) {
        LOG.info("Sender søknad til " + oppslagServiceUri);
        try {
            ResponseEntity<Ressurs>
                response = postRequest(oppslagServiceUri, arkiverDokumentRequest, Ressurs.class);

            return response.getBody().convert(ArkiverDokumentResponse.class);
        } catch (RestClientResponseException e) {
            LOG.warn("Innsending til dokarkiv feilet. Responskode: {}, body: {}", e.getRawStatusCode(), e.getResponseBodyAsString());
            throw new IllegalStateException("Innsending til dokarkiv feilet. Status: " + e.getRawStatusCode() + ", body: " + e.getResponseBodyAsString(), e);
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
