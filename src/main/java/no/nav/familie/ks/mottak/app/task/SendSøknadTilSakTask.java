package no.nav.familie.ks.mottak.app.task;

import no.nav.familie.ks.mottak.app.mottak.SøknadService;
import no.nav.familie.prosessering.AsyncTask;
import no.nav.familie.prosessering.TaskBeskrivelse;
import no.nav.familie.prosessering.domene.Task;
import no.nav.familie.prosessering.domene.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@TaskBeskrivelse(taskType = SendSøknadTilSakTask.SEND_SØKNAD_TIL_SAK, beskrivelse = "Send søknad til sak")
public class SendSøknadTilSakTask implements AsyncTask {

    public static final String SEND_SØKNAD_TIL_SAK = "sendSøknadTilSak";
    private TaskRepository taskRepository;
    private SøknadService søknadService;


    @Autowired
    public SendSøknadTilSakTask(SøknadService søknadService, TaskRepository taskRepository) {
        this.søknadService = søknadService;
        this.taskRepository = taskRepository;
    }

    @Override
    public void doTask(Task task) {
        søknadService.sendTilSak(task.getPayload());
    }

    @Override
    public void onCompletion(Task task){
        taskRepository.save(Task.nyTask(SendMeldingTilDittNavTask.SEND_MELDING_TIL_DITT_NAV, task.getPayload()));
    }
}
