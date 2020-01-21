package no.nav.familie.ks.mottak.app.task;

import no.nav.familie.ks.mottak.app.mottak.SøknadService;
import no.nav.familie.prosessering.AsyncTaskStep;
import no.nav.familie.prosessering.TaskStepBeskrivelse;
import no.nav.familie.prosessering.domene.Task;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@TaskStepBeskrivelse(taskStepType = SendMeldingTilDittNavTask.SEND_MELDING_TIL_DITT_NAV, beskrivelse = "Send melding til ditt nav")
public class SendMeldingTilDittNavTask implements AsyncTaskStep {

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

    @Override
    public void postCondition(@NotNull Task task) {
        //NOP
    }

    @Override
    public void preCondition(@NotNull Task task) {
        //NOP
    }
}
