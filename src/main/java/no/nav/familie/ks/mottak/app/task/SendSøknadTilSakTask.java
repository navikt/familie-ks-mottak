package no.nav.familie.ks.mottak.app.task;

import no.nav.familie.ks.mottak.app.mottak.SøknadService;
import no.nav.familie.prosessering.AsyncTask;
import no.nav.familie.prosessering.TaskBeskrivelse;
import no.nav.familie.prosessering.domene.Task;
import org.springframework.stereotype.Service;

@Service
@TaskBeskrivelse(taskType = SendSøknadTilSakTask.SEND_SØKNAD_TIL_SAK)
public class SendSøknadTilSakTask implements AsyncTask {

    public static final String SEND_SØKNAD_TIL_SAK = "sendSøknadTilSak";
    private SøknadService søknadService;

    public SendSøknadTilSakTask(SøknadService søknadService) {
        this.søknadService = søknadService;
    }

    @Override
    public void doTask(Task task) {
        søknadService.sendTilSak(task.getPayload().getBytes());
    }
}
