package no.nav.familie.ks.mottak.app.task;

import no.nav.familie.ks.mottak.app.mottak.SøknadService;
import no.nav.familie.prosessering.AsyncTask;
import no.nav.familie.prosessering.TaskBeskrivelse;
import no.nav.familie.prosessering.domene.Task;
import no.nav.familie.prosessering.domene.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
@TaskBeskrivelse(taskType = HentSaksnummerFraJoarkTask.HENT_SAKSNUMMER_FRA_JOARK, maxAntallFeil = 20, beskrivelse = "Hent saksnummer fra joark")
public class HentSaksnummerFraJoarkTask implements AsyncTask {

    public static final String HENT_SAKSNUMMER_FRA_JOARK = "hentSaksnummerFraJoark";
    private TaskRepository taskRepository;
    private SøknadService søknadService;


    @Autowired
    public HentSaksnummerFraJoarkTask(SøknadService søknadService, TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
        this.søknadService = søknadService;
    }

    @Override
    public void doTask(Task task) {
        //søknadService.sendTilSak(task.getPayload().getBytes());
    }

    @Override
    public void onCompletion(Task task){
        Task nesteTask = Task.nyTask(SendSøknadTilSakTask.SEND_SØKNAD_TIL_SAK, task.getPayload());
        taskRepository.save(nesteTask);
    }
}
