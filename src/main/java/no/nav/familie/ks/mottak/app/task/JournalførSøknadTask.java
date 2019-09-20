package no.nav.familie.ks.mottak.app.task;

import no.nav.familie.ks.mottak.app.mottak.SøknadService;
import no.nav.familie.prosessering.AsyncTask;
import no.nav.familie.prosessering.TaskBeskrivelse;
import no.nav.familie.prosessering.domene.Task;
import no.nav.familie.prosessering.domene.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@TaskBeskrivelse(taskType = JournalførSøknadTask.JOURNALFØR_SØKNAD)
public class JournalførSøknadTask implements AsyncTask {

    public static final String JOURNALFØR_SØKNAD = "journalførSøknad";
    private TaskRepository taskRepository;
    private SøknadService søknadService;


    @Autowired
    public JournalførSøknadTask(SøknadService søknadService, TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
        this.søknadService = søknadService;
    }

    @Override
    public void doTask(Task task) {
        //søknadService.sendTilSak(task.getPayload().getBytes());
    }

    @Override
    public void onCompletion(Task task){
        Task nesteTask = Task.nyTaskMedStartFremITid(HentSaksnummerFraJoarkTask.HENT_SAKSNUMMER_FRA_JOARK,task.getPayload(), LocalDateTime.now().plusMinutes(15));
        taskRepository.save(nesteTask);
    }
 }
