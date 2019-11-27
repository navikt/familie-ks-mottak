package no.nav.familie.ks.mottak;

import no.nav.familie.ks.kontrakter.sak.Ressurs;
import no.nav.familie.ks.mottak.app.domene.Soknad;
import no.nav.familie.ks.mottak.app.domene.SøknadRepository;
import no.nav.familie.ks.mottak.app.task.JournalførSøknadTask;
import no.nav.familie.prosessering.domene.Task;
import no.nav.familie.prosessering.domene.TaskRepository;
import no.nav.security.token.support.test.spring.TokenGeneratorConfiguration;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

import static no.nav.familie.ks.mottak.app.task.HentJournalpostIdFraJoarkTask.HENT_JOURNALPOSTID_FRA_JOARK;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {UnitTestLauncher.class, TokenGeneratorConfiguration.class}, properties = {"FAMILIE_KS_OPPSLAG_API_URL=http://localhost:18085/api"})
@ActiveProfiles({"integrasjonstest", "mock-oauth"})
public class JournalførSøknadTaskTest {

    public static final Long SØKNAD_ID = 1L;
    public static final String DOKARKIV_POST_JSON = "{\"fnr\":\"fnr\",\"forsøkFerdigstill\":true,\"dokumenter\":[{\"dokument\":\"q83v\",\"filType\":\"PDFA\",\"filnavn\":\"hovedskjema\",\"dokumentType\":\"KONTANTSTØTTE_SØKNAD\"},{\"dokument\":\"EjRW\",\"filType\":\"PDFA\",\"filnavn\":\"vedlegg\",\"dokumentType\":\"KONTANTSTØTTE_SØKNAD_VEDLEGG\"}]}";
    public static final String DOKARKIV_SUCCESS_RESPONSE = Ressurs.Companion.success(Map.of("journalpostId", "123"), "OK").toJson();
    @Autowired
    private TaskRepository repository;

    @Autowired
    private SøknadRepository søknadRepository;

    @Autowired
    private JournalførSøknadTask journalførSøknadTask;
    private MockWebServer server;


    @Before
    public void setUp() throws IOException {
        server = new MockWebServer();
        server.start(18085);
        server.url("/api/arkiv");
    }

    @After
    public void teardown() throws IOException {
        server.shutdown();
    }

    @Test
    @Sql("classpath:sql-testdata/søknad_med_vedlegg.sql")
    @DirtiesContext
    public void skal_hente_journalpost_id_og_slette_vedlegg() throws IOException, InterruptedException {
        MockResponse response = new MockResponse()
            .addHeader("Content-Type", "application/json; charset=utf-8")
            .setResponseCode(201)
            .setBody(DOKARKIV_SUCCESS_RESPONSE);
        server.enqueue(response);

        var task = repository.saveAndFlush(Task.nyTask(HENT_JOURNALPOSTID_FRA_JOARK, SØKNAD_ID.toString()));

        journalførSøknadTask.doTask(task);

        RecordedRequest request = server.takeRequest();

        Optional<Soknad> byId = søknadRepository.findById(SØKNAD_ID);
        assertThat(byId).isPresent();
        assertThat(byId.get().getVedlegg()).isEmpty();
        assertThat(request.getHeader("Authorization")).isNotNull();
        assertThat(request.getBody().readString(StandardCharsets.UTF_8)).isEqualTo(DOKARKIV_POST_JSON);

    }
}
