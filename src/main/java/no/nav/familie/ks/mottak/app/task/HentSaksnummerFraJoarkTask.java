package no.nav.familie.ks.mottak.app.task;

import no.nav.familie.ks.mottak.app.mottak.HentJournalpostService;
import no.nav.familie.prosessering.AsyncTaskStep;
import no.nav.familie.prosessering.TaskStepBeskrivelse;
import no.nav.familie.prosessering.domene.Task;
import no.nav.familie.prosessering.domene.TaskRepository;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.time.LocalDateTime;


@Service
@TaskStepBeskrivelse(taskStepType = HentSaksnummerFraJoarkTask.HENT_SAKSNUMMER_FRA_JOARK, maxAntallFeil = 100, beskrivelse = "Hent saksnummer fra joark")
public class HentSaksnummerFraJoarkTask implements AsyncTaskStep {
    private static final Logger LOG = LoggerFactory.getLogger(HentSaksnummerFraJoarkTask.class);

    public static final String HENT_SAKSNUMMER_FRA_JOARK = "hentSaksnummerFraJoark";
    private TaskRepository taskRepository;
    private HentJournalpostService hentJournalpostService;


    @Autowired
    public HentSaksnummerFraJoarkTask(TaskRepository taskRepository, HentJournalpostService hentJournalpostService) {
        this.taskRepository = taskRepository;
        this.hentJournalpostService = hentJournalpostService;
    }

    @Override
    public void doTask(Task task) {
        try {
            String saksnummer = hentJournalpostService.hentSaksnummer(task.getPayload());
            task.getMetadata().put("saksnummer", saksnummer);
            taskRepository.saveAndFlush(task);
        } catch (HttpClientErrorException.NotFound notFound) {
            LOG.info("Hent saksnummer returnerte 404 responsebody={}", notFound.getResponseBodyAsString());
            task.setTriggerTid(LocalDateTime.now().plusMinutes(15));
            taskRepository.save(task);
            throw notFound;
        }
    }

    @Override
    public void onCompletion(Task task) {
        Task nesteTask = Task.Companion.nyTask(SendSøknadTilSakTask.SEND_SØKNAD_TIL_SAK, task.getPayload());
        nesteTask.getMetadata().putAll(task.getMetadata());
        taskRepository.save(nesteTask);
    }

    @Override
    public void postCondition(@NotNull Task task) {
        //NOP
    }

    @Override
    public void preCondition(@NotNull Task task) {
        //NOP
    }
}
