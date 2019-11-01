package no.nav.familie.ks.mottak.app.mottak;

import no.nav.familie.http.sts.StsRestClient;
import no.nav.familie.ks.mottak.app.domene.Soknad;
import no.nav.familie.ks.mottak.app.domene.SøknadRepository;
import no.nav.familie.ks.mottak.util.LocalSts;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Arrays;
import java.util.Optional;

@Service
public class HentJournalpostService {
    private static final Logger LOG = LoggerFactory.getLogger(HentJournalpostService.class);

    private final String oppslagUrl;
    private final StsRestClient stsRestClient;
    private final SøknadService søknadService;
    private final RestTemplate restTemplate;

    @Autowired
    public HentJournalpostService(@Value("${FAMILIE_KS_OPPSLAG_API_URL}") String oppslagUrl,
                                  StsRestClient stsRestClient,
                                  SøknadService søknadService,
                                  RestTemplate restTemplate) {
        this.oppslagUrl = oppslagUrl;
        this.stsRestClient = stsRestClient;
        this.søknadService = søknadService;
        this.restTemplate = restTemplate;
    }


    public void hentSaksnummer(String søknadId) {
        Soknad søknad = søknadService.hentSoknad(søknadId);
        String journalpostID = søknad.getJournalpostID();
        Optional<String> saksnummer = hentFraUrl(oppslagUrl + "/journalpost/%s/sak", journalpostID);

        søknad.setSaksnummer(saksnummer.orElseThrow(() -> new RuntimeException("Finner ikke saksnummer for journalpostId=" + journalpostID + ", søknadId=" + søknadId)));
        søknadService.lagreSøknad(søknad);
    }

    public void hentJournalpostId(String søknadId, String callId) {
        Soknad søknad = søknadService.hentSoknad(søknadId);

        Optional<String> journalpostId = hentFraUrl(oppslagUrl + "/journalpost/kanalreferanseid/%s", callId);
        søknad.setJournalpostID(journalpostId.orElseThrow(() -> new RuntimeException("Finner ikke journalpost for kanalReferanseId=" + callId + ", søknadId=" + søknadId)));
        søknadService.lagreSøknad(søknad);
    }

    private Optional<String> hentFraUrl(String urlformat, Object... searchParams) {
        if (searchParams == null || Arrays.asList(searchParams).contains(null)) {
            return Optional.empty();
        }
        URI uri = URI.create(String.format(urlformat, searchParams));

        HttpEntity entity = lagRequestEntityMedSikkerhetsheader();
        try {
            ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);
            return Optional.ofNullable(response.getBody());
        } catch (HttpClientErrorException.NotFound notFound) {
            throw notFound;
        } catch (HttpStatusCodeException e) {
            LOG.warn("Feil mot {} {} {}", uri, e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException(String.format("Error mot %s status=%s body=%s", uri, e.getStatusCode(), e.getResponseBodyAsByteArray()), e);
        }
    }

    @NotNull
    private HttpEntity lagRequestEntityMedSikkerhetsheader() {
        String systembrukerToken = stsRestClient.getSystemOIDCToken();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(systembrukerToken);
        return new HttpEntity(headers);
    }
}
