package no.nav.familie.ks.mottak.app.task;

import no.nav.familie.ks.mottak.app.mottak.SøknadService;
import no.nav.familie.prosessering.AsyncTask;
import no.nav.familie.prosessering.TaskBeskrivelse;
import no.nav.familie.prosessering.domene.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@TaskBeskrivelse(taskType = SendMeldingTilDittNavTask.SEND_MELDING_TIL_DITT_NAV)
public class SendMeldingTilDittNavTask implements AsyncTask {

    public static final String SEND_MELDING_TIL_DITT_NAV= "sendMeldingTilDittNav";
    private SøknadService søknadService;


    @Autowired
    public SendMeldingTilDittNavTask(SøknadService søknadService) {
        this.søknadService = søknadService;
    }

    @Override
    public void doTask(Task task) {
        //søknadService.sendTilSak(task.getPayload().getBytes());
    }

    @Override
    public void onCompletion(Task task){
        // Dette er siste Task i mottaks flyten.
    }
}
