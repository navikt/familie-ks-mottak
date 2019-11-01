package no.nav.familie.ks.mottak.app.mottak;

import no.nav.familie.ks.kontrakter.søknad.Søknad;
import no.nav.familie.ks.mottak.app.domene.Soknad;
import no.nav.familie.ks.mottak.app.domene.SøknadRepository;
import no.nav.familie.ks.mottak.app.domene.Vedlegg;
import no.nav.familie.ks.mottak.app.task.HentJournalpostIdFraJoarkTask;
import no.nav.familie.ks.mottak.app.task.JournalførSøknadTask;
import no.nav.familie.ks.mottak.config.BaseService;
import no.nav.familie.prosessering.domene.Task;
import no.nav.familie.prosessering.domene.TaskRepository;
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService;
import no.nav.security.token.support.client.spring.ClientConfigurationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class SøknadService extends BaseService {

    private static final Logger LOG = LoggerFactory.getLogger(SøknadService.class);
    private static final String OAUTH2_CLIENT_CONFIG_KEY = "ks-sak-clientcredentials";

    private final URI sakServiceUri;
    private final SøknadRepository søknadRepository;
    private final TaskRepository taskRepository;

    public SøknadService(
        @Value("${FAMILIE_KS_SAK_API_URL}") URI sakServiceUri,
        RestTemplateBuilder restTemplateBuilder,
        ClientConfigurationProperties clientConfigurationProperties,
        OAuth2AccessTokenService oAuth2AccessTokenService,
        SøknadRepository søknadRepository,
        TaskRepository taskRepository) {

        super(OAUTH2_CLIENT_CONFIG_KEY, restTemplateBuilder, clientConfigurationProperties, oAuth2AccessTokenService);

        this.sakServiceUri = URI.create(sakServiceUri + "/mottak/dokument");
        this.søknadRepository = søknadRepository;
        this.taskRepository = taskRepository;
    }

    @Transactional
    public void lagreSoknadOgLagTask(SøknadDto søknadDto, boolean skalJournalføreSelv) {
        Soknad soknad = new Soknad();
        soknad.setSoknadJson(søknadDto.getSoknad());
        List<Vedlegg> vedlegg = søknadDto.getVedlegg().stream().map(vedleggDto -> {
            var v = new Vedlegg();
            v.setSoknad(soknad);
            v.setData(vedleggDto.getData());
            v.setFilnavn(vedleggDto.getTittel());
            return v;
        }).collect(Collectors.toList());
        soknad.setVedlegg(vedlegg);
        soknad.setFnr(søknadDto.getFnr());

        lagreSøknad(soknad);

        final Task task;
        if (skalJournalføreSelv) {
            task = Task.nyTask(JournalførSøknadTask.JOURNALFØR_SØKNAD, soknad.getId().toString());
        } else {
            task = Task.nyTask(HentJournalpostIdFraJoarkTask.HENT_JOURNALPOSTID_FRA_JOARK, soknad.getId().toString());
        }

        taskRepository.save(task);
    }

    public void sendTilSak(String søknadId) {
        Soknad søknad = hentSoknad(søknadId);
        String søknadJson = søknad.getSoknadJson();
        String saksnummer = søknad.getSaksnummer();
        String journalpostID = søknad.getJournalpostID();
        Objects.requireNonNull(saksnummer, "Saksnummer er null");
        Objects.requireNonNull(journalpostID, "journalpostId er null");

        LOG.info("Sender søknad til {}", sakServiceUri);
        try {
            SendTilSakDto sendTilSakDto = new SendTilSakDto(søknadJson, saksnummer, journalpostID);
            ResponseEntity response = postRequest(sakServiceUri, sendTilSakDto);

            if (response.getStatusCode().isError()) {
                LOG.warn("Innsending til sak feilet. Responskode: {}, body: {}", response.getStatusCode(), response.getBody());

                throw new IllegalStateException("Innsending til sak feilet. Status: " + response.getStatusCode() + ", body: " + response.getBody());
            }
        } catch (RestClientException e) {
            throw new IllegalStateException("Innsending til sak feilet.", e);
        }
    }

    Soknad hentSoknad(String søknadId) {
        try {
            return søknadRepository.findById(Long.valueOf(søknadId)).orElseThrow(() -> new RuntimeException("Finner ikke søknad med id=" + søknadId));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Kan ikke hente Søknad for søknadid=" + søknadId);
        }
    }

    public Soknad lagreSøknad(Soknad søknad) {
        return søknadRepository.saveAndFlush(søknad);
    }
}
