package no.nav.familie.ks.mottak;

import no.nav.familie.ks.mottak.app.domene.Soknad;
import no.nav.familie.ks.mottak.app.domene.SøknadRepository;
import no.nav.familie.ks.mottak.app.mottak.HentJournalpostService;
import no.nav.familie.ks.mottak.app.mottak.SøknadService;
import no.nav.familie.ks.mottak.config.ApplicationConfig;
import no.nav.familie.prosessering.domene.TaskRepository;
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService;
import no.nav.security.token.support.client.spring.ClientConfigurationProperties;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = UnitTestLauncher.class)
@ActiveProfiles("integrasjonstest")
class HentJournalpostServiceTest {

    private static final String JOURNALPOST_ID = "567";
    private static final String SØKNAD_ID = "1234";
    private static final String SAKSNUMMER = "4321";
    private static final String OPPSLAG_BASE_URL = "http://junit/api";
    private static final String HENT_SAKSNUMMER_URL = OPPSLAG_BASE_URL + "/journalpost/" + JOURNALPOST_ID + "/sak";
    private static final String CALL_ID = "CallId";
    private static final String HENT_JOURNAPOST_URL = OPPSLAG_BASE_URL + "/journalpost/kanalreferanseid/" + CALL_ID;


    private HentJournalpostService hentJournalpostService;

    @Mock
    private TaskRepository taskRepository;
    @Mock
    private SøknadRepository søknadRepository;
    @Mock
    private SøknadService søknadService;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private RestTemplateBuilder restTemplateBuilder;

    @Autowired
    private ClientConfigurationProperties clientConfigurationProperties;
    @Autowired
    private OAuth2AccessTokenService oAuth2AccessTokenService;

    @BeforeEach
    void setUp() {
        hentJournalpostService = new HentJournalpostService(OPPSLAG_BASE_URL, restTemplateBuilder, clientConfigurationProperties, oAuth2AccessTokenService, søknadService, søknadRepository);
    }

    @ParameterizedTest
    @NullAndEmptySource
    void hent_saksnummer_skal_kaste_feil_hvis_payload_ikke_er_nummer(String payload) {
        RuntimeException e = assertThrows(RuntimeException.class, () -> hentJournalpostService.hentSaksnummer(payload));

        assertThat(e.getMessage()).isEqualTo("Kan ikke hente Søknad for søknadid=" + payload);
    }

    @Test
    void hent_saksnummer_skal_kaste_feil_hvis_søknad_ikke_eksisterer() {
        when(søknadRepository.findById(Long.valueOf(SØKNAD_ID))).thenReturn(Optional.empty());

        RuntimeException e = assertThrows(RuntimeException.class, () -> {
            hentJournalpostService.hentSaksnummer(SØKNAD_ID);
        });

        assertThat(e.getMessage()).isEqualTo("Finner ikke søknad med id=" + SØKNAD_ID);
    }

    @Test
    void hent_saksnummer_skal_kaste_feil_hvis_søknad_mangler_journalpostID() {
        when(søknadRepository.findById(Long.valueOf(SØKNAD_ID))).thenReturn(Optional.of(new Soknad()));

        RuntimeException e = assertThrows(RuntimeException.class, () -> {
            hentJournalpostService.hentSaksnummer(SØKNAD_ID);
        });

        assertThat(e.getMessage()).isEqualTo("Finner ikke saksnummer for journalpostId=null, søknadId=" + SØKNAD_ID);
    }

    @Test
    void hent_saksnummer_skal_lagre_saksnummer_på_søknad() {
        Soknad søknad = new Soknad();
        søknad.setJournalpostID(JOURNALPOST_ID);

        when(søknadRepository.findById(1234L)).thenReturn(Optional.of(søknad));
        ArgumentCaptor<URI> captorUri = ArgumentCaptor.forClass(URI.class);
        ArgumentCaptor<Soknad> captorSøknad = ArgumentCaptor.forClass(Soknad.class);
        when(restTemplate.exchange(captorUri.capture(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
            .thenReturn(new ResponseEntity<>(SAKSNUMMER, HttpStatus.OK));

        hentJournalpostService.hentSaksnummer(SØKNAD_ID);

        verify(søknadService, times(1)).lagreSøknad(captorSøknad.capture());
        assertThat(captorUri.getValue().toString()).isEqualTo(HENT_SAKSNUMMER_URL);
        assertThat(captorSøknad.getValue().getSaksnummer()).isEqualTo(SAKSNUMMER);
    }

    @Test
    void hent_journalpost_skal_lagre_saksnummer_på_søknad() {
        Soknad søknad = new Soknad();

        when(søknadRepository.findById(1234L)).thenReturn(Optional.of(søknad));
        ArgumentCaptor<URI> captorUri = ArgumentCaptor.forClass(URI.class);
        ArgumentCaptor<Soknad> captorSøknad = ArgumentCaptor.forClass(Soknad.class);
        when(restTemplate.exchange(captorUri.capture(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
            .thenReturn(new ResponseEntity<>(JOURNALPOST_ID, HttpStatus.OK));

        hentJournalpostService.hentJournalpostId(SØKNAD_ID, CALL_ID);

        verify(søknadService, times(1)).lagreSøknad(captorSøknad.capture());
        assertThat(captorUri.getValue().toString()).isEqualTo(HENT_JOURNAPOST_URL);
        assertThat(captorSøknad.getValue().getJournalpostID()).isEqualTo(JOURNALPOST_ID);
    }

    @Test
    void hent_journalpost_skal_kaste_feil_hvis_søknad_mangler_callId() {
        when(søknadRepository.findById(Long.valueOf(SØKNAD_ID))).thenReturn(Optional.of(new Soknad()));

        RuntimeException e = assertThrows(RuntimeException.class, () -> hentJournalpostService.hentJournalpostId(SØKNAD_ID, null));

        assertThat(e.getMessage()).isEqualTo("Finner ikke journalpost for kanalReferanseId=null, søknadId=" + SØKNAD_ID);
    }
}
