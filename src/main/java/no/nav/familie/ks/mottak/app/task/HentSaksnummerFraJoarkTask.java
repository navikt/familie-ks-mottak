package no.nav.familie.ks.mottak.app.task;

import no.nav.familie.ks.mottak.app.mottak.HentJournalpostService;
import no.nav.familie.ks.mottak.app.mottak.HentSaksnummerException;
import no.nav.familie.ks.mottak.app.util.TaskUtil;
import no.nav.familie.prosessering.AsyncTaskStep;
import no.nav.familie.prosessering.TaskStepBeskrivelse;
import no.nav.familie.prosessering.domene.Task;
import no.nav.familie.prosessering.domene.TaskRepository;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;


@Service
@TaskStepBeskrivelse(taskStepType = HentSaksnummerFraJoarkTask.HENT_SAKSNUMMER_FRA_JOARK,
                     maxAntallFeil = 100,
                     beskrivelse = "Hent saksnummer fra joark",
                     triggerTidVedFeilISekunder = 60 * 15)
public class HentSaksnummerFraJoarkTask implements AsyncTaskStep {

    public static final String HENT_SAKSNUMMER_FRA_JOARK = "hentSaksnummerFraJoark";
    private static final Logger LOG = LoggerFactory.getLogger(HentSaksnummerFraJoarkTask.class);
    private final TaskRepository taskRepository;
    private final HentJournalpostService hentJournalpostService;


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
        } catch (HentSaksnummerException e) {
            task.setTriggerTid(TaskUtil.nesteTriggertidEksluderHelg(LocalDateTime.now()));
            taskRepository.save(task);
            throw e;
        }

    }

    @Override
    public void onCompletion(Task task) {
        Task nesteTask = Task.Companion.nyTask(SendSøknadTilSakTask.SEND_SØKNAD_TIL_SAK, task.getPayload(), task.getMetadata());
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
