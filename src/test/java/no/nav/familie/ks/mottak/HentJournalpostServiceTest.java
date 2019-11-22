package no.nav.familie.ks.mottak;

import no.nav.familie.ks.mottak.app.domene.Soknad;
import no.nav.familie.ks.mottak.app.domene.SøknadRepository;
import no.nav.familie.ks.mottak.app.mottak.HentJournalpostService;
import no.nav.familie.ks.mottak.app.mottak.SøknadService;
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService;
import no.nav.security.token.support.client.spring.ClientConfigurationProperties;
import no.nav.security.token.support.test.spring.TokenGeneratorConfiguration;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = {UnitTestLauncher.class, TokenGeneratorConfiguration.class}, properties = {"FAMILIE_KS_OPPSLAG_API_URL=http://localhost:18085/api"})
@ActiveProfiles({"integrasjonstest", "mock-oauth"})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class HentJournalpostServiceTest {

    private static final String JOURNALPOST_ID = "567";
    private static final String SØKNAD_ID = "1234";
    private static final String SAKSNUMMER = "4321";
    private static final String OPPSLAG_BASE_URL = "http://localhost:18085/api";
    private static final String CALL_ID = "CallId";


    private HentJournalpostService hentJournalpostService;
    @Mock
    private SøknadRepository søknadRepository;
    @Mock
    private SøknadService søknadService;
    @Autowired
    private RestTemplateBuilder restTemplateBuilder;
    @Autowired
    private ClientConfigurationProperties clientConfigurationProperties;
    @Autowired
    private OAuth2AccessTokenService oAuth2AccessTokenService;

    private MockWebServer server;

    @BeforeEach
    void setUp() {
        hentJournalpostService = new HentJournalpostService(OPPSLAG_BASE_URL, restTemplateBuilder, clientConfigurationProperties, oAuth2AccessTokenService, søknadService, søknadRepository);
    }

    @AfterAll
    void tearDown() throws IOException {

        server.shutdown();
    }

    @BeforeAll
    void inti() throws IOException {
        server = new MockWebServer();
        server.start(18085);
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
        MockResponse response = new MockResponse()
            .addHeader("Content-Type", "application/json; charset=utf-8")
            .setResponseCode(200)
            .setBody("{\"data\": {\"saksnummer\": \"4321\"},\"status\": \"SUKSESS\",\"melding\": \"OK\"}");


        server.enqueue(response);

        Soknad søknad = new Soknad();
        søknad.setJournalpostID(JOURNALPOST_ID);

        when(søknadRepository.findById(1234L)).thenReturn(Optional.of(søknad));
        ArgumentCaptor<Soknad> captorSøknad = ArgumentCaptor.forClass(Soknad.class);

        hentJournalpostService.hentSaksnummer(SØKNAD_ID);

        verify(søknadService, times(1)).lagreSøknad(captorSøknad.capture());
        assertThat(captorSøknad.getValue().getSaksnummer()).isEqualTo(SAKSNUMMER);
    }

    @Test
    void hent_journalpost_skal_lagre_journalpostid_på_søknad() {
        MockResponse response = new MockResponse()
            .addHeader("Content-Type", "application/json; charset=utf-8")
            .setResponseCode(200)
            .setBody("{\"data\": {\"journalpostId\": \"567\"},\"status\": \"SUKSESS\",\"melding\": \"OK\"}");

        server.enqueue(response);

        when(søknadRepository.findById(1234L)).thenReturn(Optional.of(new Soknad()));
        ArgumentCaptor<Soknad> captorSøknad = ArgumentCaptor.forClass(Soknad.class);

        hentJournalpostService.hentJournalpostId(SØKNAD_ID, CALL_ID);

        verify(søknadService, times(1)).lagreSøknad(captorSøknad.capture());
        assertThat(captorSøknad.getValue().getJournalpostID()).isEqualTo(JOURNALPOST_ID);
    }

    @Test
    void hent_journalpost_skal_kaste_feil_hvis_søknad_mangler_callId() {
        when(søknadRepository.findById(Long.valueOf(SØKNAD_ID))).thenReturn(Optional.of(new Soknad()));


        RuntimeException e = assertThrows(RuntimeException.class, () -> hentJournalpostService.hentJournalpostId(SØKNAD_ID, null));

        assertThat(e.getMessage()).isEqualTo("Finner ikke journalpost for kanalReferanseId=null, søknadId=" + SØKNAD_ID);
    }
}
