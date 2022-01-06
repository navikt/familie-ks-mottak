package no.nav.familie.ks.mottak.app.task;

import no.nav.familie.ks.mottak.app.mottak.SøknadService;
import no.nav.familie.prosessering.AsyncTaskStep;
import no.nav.familie.prosessering.TaskStepBeskrivelse;
import no.nav.familie.prosessering.domene.Task;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@TaskStepBeskrivelse(taskStepType = SendSøknadTilSakTask.SEND_SØKNAD_TIL_SAK, beskrivelse = "Send søknad til sak")
public class SendSøknadTilSakTask implements AsyncTaskStep {

    public static final String SEND_SØKNAD_TIL_SAK = "sendSøknadTilSak";
    private SøknadService søknadService;


    @Autowired
    public SendSøknadTilSakTask(SøknadService søknadService) {
        this.søknadService = søknadService;
    }

    @Override
    public void doTask(Task task) {
        søknadService.sendTilSak(task.getPayload());
    }

    @Override
    public void onCompletion(Task task) {
        //NOP
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
