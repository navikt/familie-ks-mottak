package no.nav.familie.ks.mottak.app.mottak;

import no.nav.familie.kontrakter.felles.Ressurs;
import no.nav.familie.ks.mottak.app.domene.Soknad;
import no.nav.familie.ks.mottak.app.domene.SøknadRepository;
import no.nav.familie.ks.mottak.config.BaseService;
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService;
import no.nav.security.token.support.client.spring.ClientConfigurationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;

import java.net.URI;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

@Service
public class HentJournalpostService extends BaseService {

    private static final Logger LOG = LoggerFactory.getLogger(HentJournalpostService.class);
    private static final String OAUTH2_CLIENT_CONFIG_KEY = "integrasjoner-clientcredentials";

    private final String integrasjonServiceUri;
    private final SøknadService søknadService;
    private final SøknadRepository søknadRepository;

    public HentJournalpostService(@Value("${FAMILIE_INTEGRASJONER_API_URL}") String integrasjonServiceUri,
                                  RestTemplateBuilder restTemplateBuilderMedProxy,
                                  ClientConfigurationProperties clientConfigurationProperties,
                                  OAuth2AccessTokenService oAuth2AccessTokenService,
                                  SøknadService søknadService,
                                  SøknadRepository søknadRepository) {

        super(OAUTH2_CLIENT_CONFIG_KEY, restTemplateBuilderMedProxy, clientConfigurationProperties, oAuth2AccessTokenService);

        this.integrasjonServiceUri = integrasjonServiceUri;
        this.søknadService = søknadService;
        this.søknadRepository = søknadRepository;
    }

    private Soknad hentSoknad(String søknadId) {
        try {
            return søknadRepository.findById(Long.valueOf(søknadId))
                                   .orElseThrow(() -> new RuntimeException("Finner ikke søknad med id=" + søknadId));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Kan ikke hente Søknad for søknadid=" + søknadId);
        }
    }

    public String hentSaksnummer(String søknadId) {
        Soknad søknad = hentSoknad(søknadId);
        String journalpostID = søknad.getJournalpostID();
        Optional<String> saksnummer =
            hentFraUrl(integrasjonServiceUri + "/journalpost/sak?journalpostId=%s", "saksnummer", journalpostID);

        søknad.setSaksnummer(saksnummer.orElseThrow(() -> new HentSaksnummerException(
            "Finner ikke saksnummer for journalpostId=" + journalpostID + ", søknadId=" + søknadId)));
        søknadService.lagreSøknad(søknad);
        return saksnummer.get();
    }

    private Optional<String> hentFraUrl(String urlformat, String fieldName, Object... searchParams) {
        if (searchParams == null || Arrays.asList(searchParams).contains(null)) {
            return Optional.empty();
        }
        URI uri = URI.create(String.format(urlformat, searchParams));

        try {
            ResponseEntity<Ressurs> response = getRequest(uri, Ressurs.class);
            Assert.notNull(response.getBody(), "Finner ikke ressurs");
            Assert.notNull(response.getBody().getData(), "Ressurs mangler data");
            Assert.isTrue(response.getBody().getStatus().equals(Ressurs.Status.SUKSESS),
                          String.format("Ressurs returnerer %s men har http status kode %s",
                                        response.getBody().getStatus(),
                                        response.getStatusCode()));
            Map<String, String> body = (Map) response.getBody().getData();
            return Optional.ofNullable(body.get(fieldName));
        } catch (HttpClientErrorException.NotFound notFound) {
            throw notFound;
        } catch (HttpStatusCodeException e) {
            LOG.warn("Feil mot {} {} {}", uri, e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException(String.format("Error mot %s status=%s body=%s",
                                                     uri,
                                                     e.getStatusCode(),
                                                     e.getResponseBodyAsString()), e);
        }
    }
}
