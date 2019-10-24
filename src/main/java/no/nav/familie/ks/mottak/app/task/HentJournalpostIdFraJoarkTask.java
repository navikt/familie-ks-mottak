package no.nav.familie.ks.mottak.app.task;

import no.nav.familie.ks.mottak.app.mottak.HentJournalpostService;
import no.nav.familie.prosessering.AsyncTask;
import no.nav.familie.prosessering.TaskBeskrivelse;
import no.nav.familie.prosessering.domene.Task;
import no.nav.familie.prosessering.domene.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
@TaskBeskrivelse(taskType = HentJournalpostIdFraJoarkTask.HENT_JOURNALPOSTID_FRA_JOARK, beskrivelse = "Hent journapostId fra joark basert på kanalreferanseId")
public class HentJournalpostIdFraJoarkTask implements AsyncTask {

    public static final String HENT_JOURNALPOSTID_FRA_JOARK = "hentJournalpostIdFraJoarkTask";
    private final TaskRepository taskRepository;
    private final HentJournalpostService hentJournalpostService;

    @Autowired
    public HentJournalpostIdFraJoarkTask(TaskRepository taskRepository, HentJournalpostService hentJournalpostService) {
        this.taskRepository = taskRepository;
        this.hentJournalpostService = hentJournalpostService;
    }

    @Override
    public void doTask(Task task) {
        hentJournalpostService.hentJournalpostId(task.getPayload(), task.getCallId());
    }

    @Override
    public void onCompletion(Task task) {
        Task nesteTask = Task.nyTask(HentSaksnummerFraJoarkTask.HENT_SAKSNUMMER_FRA_JOARK, task.getPayload());
        taskRepository.save(nesteTask);
    }
}
