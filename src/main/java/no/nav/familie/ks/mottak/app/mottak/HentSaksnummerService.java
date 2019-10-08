package no.nav.familie.ks.mottak.app.mottak;

import no.nav.familie.http.sts.StsRestClient;
import no.nav.familie.ks.mottak.app.domene.Soknad;
import no.nav.familie.ks.mottak.app.domene.SøknadRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Optional;

@Service
public class HentSaksnummerService {
    private static final Logger LOG = LoggerFactory.getLogger(HentSaksnummerService.class);

    private final StsRestClient stsRestClient;
    private final String oppslagUrl;
    private final SøknadRepository søknadRepository;
    private final RestTemplate restTemplate;

    @Autowired
    public HentSaksnummerService(@Value("${FAMILIE_KS_OPPSLAG_API_URL}") String oppslagUrl,
                                 StsRestClient stsRestClient, SøknadRepository søknadRepository, RestTemplate restTemplate) {
        this.oppslagUrl = oppslagUrl;
        this.stsRestClient = stsRestClient;
        this.søknadRepository = søknadRepository;
        this.restTemplate = restTemplate;
    }


    public void hentSaksnummer(String payload) {
        Soknad søknad = null;
        try {
            søknad = søknadRepository.findById(Long.valueOf(payload)).orElseThrow(() -> new RuntimeException("Finner ikke søknad for " + payload));
        } catch (NumberFormatException e) {
            throw new RuntimeException("Kan ikke hente Søknad for payload");
        }
        String journalpostID = søknad.getJournalpostID();
        Optional<String> saksnummer = hentSaksnummerFraOppslag(journalpostID);

        søknad.setSaksnummer(saksnummer.orElseThrow(() -> new RuntimeException("Finner ikke saksnummer for journalpostId=" + journalpostID + " payload=" + payload)));
        søknadRepository.save(søknad);
    }

    private Optional<String> hentSaksnummerFraOppslag(String journalpostId) {
        if (journalpostId == null) {
            return Optional.empty();
        }
        URI hentSaksnummerUri = URI.create(oppslagUrl + "/journalpost/" + journalpostId + "/sak");

        String systembrukerToken = stsRestClient.getSystemOIDCToken();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(systembrukerToken);
        HttpEntity entity = new HttpEntity(headers);


        ResponseEntity<String> response = restTemplate.exchange(hentSaksnummerUri, HttpMethod.GET, entity, String.class);

        return Optional.ofNullable(response.getBody());
    }
}
