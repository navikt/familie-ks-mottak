package no.nav.familie.ks.mottak;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jwt.SignedJWT;
import no.nav.familie.http.client.HttpClientUtil;
import no.nav.familie.ks.mottak.app.domene.Soknad;
import no.nav.familie.ks.mottak.app.domene.SøknadRepository;
import no.nav.familie.ks.mottak.app.domene.Vedlegg;
import no.nav.familie.ks.mottak.app.mottak.SøknadDto;
import no.nav.familie.ks.mottak.app.mottak.VedleggDto;
import no.nav.familie.ks.mottak.app.task.JournalførSøknadTask;
import no.nav.familie.ks.mottak.app.task.SendSøknadTilSakTask;
import no.nav.familie.ks.mottak.config.ApplicationConfig;
import no.nav.familie.prosessering.domene.Task;
import no.nav.familie.prosessering.domene.TaskRepository;
import no.nav.security.oidc.OIDCConstants;
import no.nav.security.oidc.test.support.JwtTokenGenerator;
import no.nav.security.oidc.test.support.spring.TokenGeneratorConfiguration;
import org.eclipse.jetty.http.HttpHeader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;


import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("dev")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {ApplicationConfig.class, TokenGeneratorConfiguration.class})
public class MottaSøknadIntegrasjonsTest {

    private static final String INNLOGGET_BRUKER = "12345678911";
    private static boolean setupIsDone = false;
    private HttpResponse<String> response;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SøknadRepository søknadRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Value("${local.server.port}")
    private int port;


    @Before
    public void setUp() {
        if (setupIsDone) {
            return;
        }

        response = utførRequest(lagSøknadDtoMedHoveddokOgVedlegg());
        setupIsDone = true;
    }

    @Test
    public void mottak_av_søknad_gir_respons_ok() {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    public void søknad_lagres_med_fnr_og_json() {
        List<Soknad> søknader = søknadRepository.findAll();

        assertThat(søknader.size()).isEqualTo(1);
        assertThat(søknader.get(0).getFnr()).isEqualTo(INNLOGGET_BRUKER);
        assertThat(søknader.get(0).getSoknadJson()).isEqualTo("{}");
    }

    @Test
    public void vedlegg_lagres_med_ref_til_søknad() {
        List<Soknad> søknader = søknadRepository.findAll();
        List<Vedlegg> vedlegg = søknader.get(0).getVedlegg();

        assertThat(vedlegg.size()).isEqualTo(2);
        assertThat(vedlegg.get(0).getFilnavn()).isEqualTo("Hoveddokument");
        assertThat(vedlegg.get(1).getFilnavn()).isEqualTo("Vedlegg");
        assertThat(vedlegg.get(0).getData()).isEqualTo("TEST 123".getBytes());
        assertThat(vedlegg.get(1).getData()).isEqualTo("TEST 456".getBytes());
        assertThat(vedlegg.get(0).getSoknad()).isEqualTo(søknader.get(0));
        assertThat(vedlegg.get(1).getSoknad()).isEqualTo(søknader.get(0));
    }

    @Test
    public void mottak_av_søknad_genererer_sak_task() {
        List<Task> tasks = taskRepository.findAll();

        assertThat(tasks.size()).isEqualTo(1);
        assertThat(tasks.get(0).getType()).isEqualTo(SendSøknadTilSakTask.SEND_SØKNAD_TIL_SAK);
    }

    @DirtiesContext
    @Test
    public void mottak_av_søknad_med_journalføring_togglet_på_genererer_journal_task() {
        response = utførRequest(lagSøknadDtoMedHoveddokOgVedlegg(), true);
        List<Task> tasks = taskRepository.findAll();

        assertThat(tasks.size()).isEqualTo(2);
        assertThat(tasks.get(1).getType()).isEqualTo(JournalførSøknadTask.JOURNALFØR_SØKNAD);
        setupIsDone = false;
    }

    private HttpResponse<String> utførRequest(SøknadDto input, boolean skalJournalFøreSelv) {
        HttpClient client = HttpClientUtil.create();

        SignedJWT signedJWT = JwtTokenGenerator.createSignedJWT(INNLOGGET_BRUKER);

        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/soknadmedvedlegg"))
                .header(HttpHeader.CONTENT_TYPE.asString(), MediaType.APPLICATION_JSON)
                .header(OIDCConstants.AUTHORIZATION_HEADER, "Bearer " + signedJWT.serialize())
                .header("journalforSelv", Boolean.toString(skalJournalFøreSelv))
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(input)))
                .build();

            return client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

    private HttpResponse<String> utførRequest(SøknadDto input) {
        return utførRequest(input, false);
    }

    private SøknadDto lagSøknadDtoMedHoveddokOgVedlegg() {
        VedleggDto hovedDok = new VedleggDto("TEST 123".getBytes(), "Hoveddokument");
        VedleggDto vedlegg = new VedleggDto("TEST 456".getBytes(), "Vedlegg");
        return new SøknadDto(INNLOGGET_BRUKER, "{}", Arrays.asList(hovedDok, vedlegg));
    }

}
