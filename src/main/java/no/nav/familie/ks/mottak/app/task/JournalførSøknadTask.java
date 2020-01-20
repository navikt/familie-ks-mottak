package no.nav.familie.ks.mottak.app.task;

import no.nav.familie.ks.mottak.app.mottak.JournalføringService;
import no.nav.familie.ks.mottak.app.mottak.SøknadService;
import no.nav.familie.prosessering.AsyncTask;
import no.nav.familie.prosessering.TaskBeskrivelse;
import no.nav.familie.prosessering.domene.Task;
import no.nav.familie.prosessering.domene.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@TaskBeskrivelse(taskType = JournalførSøknadTask.JOURNALFØR_SØKNAD, beskrivelse = "Jornalfør søknad")
public class JournalførSøknadTask implements AsyncTask {

    public static final String JOURNALFØR_SØKNAD = "journalførSøknad";
    private TaskRepository taskRepository;
    private JournalføringService journalføringService;


    @Autowired
    public JournalførSøknadTask(JournalføringService journalføringService, TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
        this.journalføringService = journalføringService;
    }

    @Override
    public void doTask(Task task) {
        String journlpostId = journalføringService.journalførSøknad(task.getPayload());
        task.getMetadata().put("journalpostID", journlpostId);
        taskRepository.saveAndFlush(task);
    }

    @Override
    public void onCompletion(Task task) {
        Task nesteTask = Task.nyTask(HentSaksnummerFraJoarkTask.HENT_SAKSNUMMER_FRA_JOARK, task.getPayload());
        nesteTask.getMetadata().putAll(task.getMetadata());
        taskRepository.save(nesteTask);
    }
}
