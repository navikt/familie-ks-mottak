package no.nav.familie.prosessering.internal;

import no.nav.familie.ks.mottak.app.domene.SøknadRepository;
import no.nav.familie.ks.mottak.app.mottak.SøknadService;
import no.nav.familie.ks.mottak.app.task.SendSøknadTilSakTask;
import no.nav.familie.ks.mottak.config.ApplicationConfig;
import no.nav.familie.prosessering.domene.Status;
import no.nav.familie.prosessering.domene.Task;
import no.nav.familie.prosessering.domene.TaskRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
    classes = {ApplicationConfig.class})
@DataJpaTest(excludeAutoConfiguration = {FlywayAutoConfiguration.class})
@ActiveProfiles("integrasjonstest")
public class TaskWorkerTest {

    @MockBean
    private SøknadService søknadService;
    @MockBean
    private SendSøknadTilSakTask task;
    @Autowired
    private TaskRepository repository;

    @Autowired
    private SøknadRepository søknadRepository;

    @Autowired
    private TaskStepExecutorService henvendelseService;

    @Autowired
    private TaskWorker worker;

    @Test
    public void skal_behandle_task() {
        var henvendelse1 = Task.Companion.nyTask(SendSøknadTilSakTask.SEND_SØKNAD_TIL_SAK, "{'a'='b'}", new Properties());
        repository.saveAndFlush(henvendelse1);
        assertThat(henvendelse1.getStatus()).isEqualTo(Status.UBEHANDLET);

        worker.doActualWork(henvendelse1.getId());

        henvendelse1 = repository.findById(henvendelse1.getId()).orElseThrow();
        assertThat(henvendelse1.getStatus()).isEqualTo(Status.FERDIG);
        assertThat(henvendelse1.getLogg()).hasSize(3);
    }

    @Test
    public void skal_håndtere_feil() {
        var henvendelse1 = Task.Companion.nyTask(SendSøknadTilSakTask.SEND_SØKNAD_TIL_SAK, "{'a'='b'}", new Properties());
        repository.saveAndFlush(henvendelse1);
        assertThat(henvendelse1.getStatus()).isEqualTo(Status.UBEHANDLET);
        doThrow(new IllegalStateException()).when(task).doTask(any());

        worker.doActualWork(henvendelse1.getId());

        henvendelse1 = repository.findById(henvendelse1.getId()).orElseThrow();
        assertThat(henvendelse1.getStatus()).isEqualTo(Status.KLAR_TIL_PLUKK);
        assertThat(henvendelse1.getLogg()).hasSize(3);

        worker.doActualWork(henvendelse1.getId());

        henvendelse1 = repository.findById(henvendelse1.getId()).orElseThrow();
        assertThat(henvendelse1.getStatus()).isEqualTo(Status.KLAR_TIL_PLUKK);

        worker.doActualWork(henvendelse1.getId());

        henvendelse1 = repository.findById(henvendelse1.getId()).orElseThrow();
        assertThat(henvendelse1.getStatus()).isEqualTo(Status.FEILET);
    }
}
