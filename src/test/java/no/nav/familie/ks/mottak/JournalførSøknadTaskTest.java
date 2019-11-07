package no.nav.familie.ks.mottak;

import com.google.common.net.MediaType;
import no.nav.familie.ks.mottak.app.domene.Soknad;
import no.nav.familie.ks.mottak.app.domene.SøknadRepository;
import no.nav.familie.ks.mottak.app.task.JournalførSøknadTask;
import no.nav.familie.prosessering.domene.Task;
import no.nav.familie.prosessering.domene.TaskRepository;
import no.nav.security.token.support.test.spring.TokenGeneratorConfiguration;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockserver.junit.MockServerRule;
import org.mockserver.model.Header;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.mockserver.model.StringBody;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;

import static no.nav.familie.ks.mottak.app.task.HentJournalpostIdFraJoarkTask.HENT_JOURNALPOSTID_FRA_JOARK;
import static no.nav.familie.log.mdc.MDCConstants.MDC_CALL_ID;
import static org.assertj.core.api.Assertions.assertThat;

@Ignore
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = { UnitTestLauncher.class, TokenGeneratorConfiguration.class }, properties = {"FAMILIE_KS_OPPSLAG_API_URL=http://localhost:18085/api"})
@ActiveProfiles("integrasjonstest")
public class JournalførSøknadTaskTest {

    public static final Long SØKNAD_ID = 1L;
    public static final String DOKARKIV_POST_JSON = "{\"fnr\":\"fnr\",\"forsøkFerdigstill\":true,\"dokumenter\":[{\"dokument\":\"q83v\",\"filType\":\"PDFA\",\"filnavn\":\"hovedskjema\",\"dokumentType\":\"KONTANTSTØTTE_SØKNAD\"},{\"dokument\":\"EjRW\",\"filType\":\"PDFA\",\"filnavn\":\"vedlegg\",\"dokumentType\":\"KONTANTSTØTTE_SØKNAD_VEDLEGG\"}]}";

    @Autowired
    private TaskRepository repository;

    @Autowired
    private SøknadRepository søknadRepository;

    @Autowired
    private JournalførSøknadTask journalførSøknadTask;

    @Rule
    public MockServerRule mockServerRule = new MockServerRule(this, 18085);

    @Test
    @Sql("classpath:søknad_med_vedlegg.sql")
    public void skal_hente_journalpost_id_og_slette_vedlegg() {
        mockServerRule.getClient()
            .when(
                HttpRequest
                    .request()
                    .withMethod("POST")
                    .withPath("/api/arkiv")
                    .withHeader("Content-Type","application/json; charset=utf-8")
                    .withBody(new StringBody(DOKARKIV_POST_JSON, MediaType.JSON_UTF_8))
            )
            .respond(
                HttpResponse.response("{\"journalpostId\":\"123\"}").withStatusCode(201).withHeaders(
                    new Header("Content-Type", "application/json; charset=utf-8"))
            );

        MDC.put(MDC_CALL_ID, "CallId_TEST");
        var task = repository.saveAndFlush(Task.nyTask(HENT_JOURNALPOSTID_FRA_JOARK, SØKNAD_ID.toString()));

        journalførSøknadTask.doTask(task);

        Optional<Soknad> byId = søknadRepository.findById(SØKNAD_ID);
        assertThat(byId).isPresent();
        assertThat(byId.get().getVedlegg()).isEmpty();
    }
}
