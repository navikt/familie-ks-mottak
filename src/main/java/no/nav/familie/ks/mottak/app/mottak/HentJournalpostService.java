package no.nav.familie.ks.mottak.app.mottak;

import no.nav.familie.http.sts.StsRestClient;
import no.nav.familie.ks.mottak.app.domene.Soknad;
import no.nav.familie.ks.mottak.app.domene.SøknadRepository;
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
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Arrays;
import java.util.Optional;

@Service
public class HentJournalpostService {
    private static final Logger LOG = LoggerFactory.getLogger(HentJournalpostService.class);

    private final StsRestClient stsRestClient;
    private final String oppslagUrl;
    private final SøknadRepository søknadRepository;
    private final RestTemplate restTemplate;

    @Autowired
    public HentJournalpostService(@Value("${FAMILIE_KS_OPPSLAG_API_URL}") String oppslagUrl,
                                  StsRestClient stsRestClient, SøknadRepository søknadRepository, RestTemplate restTemplate) {
        this.oppslagUrl = oppslagUrl;
        this.stsRestClient = stsRestClient;
        this.søknadRepository = søknadRepository;
        this.restTemplate = restTemplate;
    }


    public void hentSaksnummer(String søknadId) {
        Soknad søknad = hentSoknad(søknadId);
        String journalpostID = søknad.getJournalpostID();
        Optional<String> saksnummer = hentFraUrl(oppslagUrl + "/journalpost/%s/sak", journalpostID);

        søknad.setSaksnummer(saksnummer.orElseThrow(() -> new RuntimeException("Finner ikke saksnummer for journalpostId=" + journalpostID + ", søknadId=" + søknadId)));
        søknadRepository.save(søknad);
    }

    public void hentJournalpostId(String søknadId, String callId) {
        Soknad søknad = hentSoknad(søknadId);

        try {
            Optional<String> journalpostId = hentFraUrl(oppslagUrl + "/journalpost/kanalreferanseid/%s", callId);
            søknad.setJournalpostID(journalpostId.orElseThrow(() -> new RuntimeException("Finner ikke journalpost for kanalReferanseId=" + callId + ", søknadId=" + søknadId)));
            søknadRepository.save(søknad);
        } catch (RestClientException e) { //FIXME slett try catch block når CallId er propargert fra web til joark
            LOG.warn("Midlertidig ignorerer feil ved henting av journalpostId ved bruk av CallId");
        }
    }

    @Deprecated(forRemoval = true) ////FIXME slett når CallId er propargert fra web til joark
    public boolean harJournalpostId(String søknadId) {
        Soknad søknad = hentSoknad(søknadId);
        return søknad.getJournalpostID() != null;
    }

    private Soknad hentSoknad(String søknadId) {
        Soknad søknad;
        try {
            søknad = søknadRepository.findById(Long.valueOf(søknadId)).orElseThrow(() -> new RuntimeException("Finner ikke søknad med id " + søknadId));
        } catch (NumberFormatException e) {
            throw new RuntimeException("Kan ikke hente Søknad for payload");
        }
        return søknad;
    }

    private Optional<String> hentFraUrl(String urlformat, Object... searchParams) {
        if (searchParams == null || Arrays.asList(searchParams).contains(null)) {
            return Optional.empty();
        }
        URI uri = URI.create(String.format(urlformat, searchParams));

        HttpEntity entity = lagRequestEntityMedSikkerhetsheader();
        ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);

        return Optional.ofNullable(response.getBody());
    }

    @NotNull
    private HttpEntity lagRequestEntityMedSikkerhetsheader() {
        String systembrukerToken = stsRestClient.getSystemOIDCToken();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(systembrukerToken);
        return new HttpEntity(headers);
    }
}
