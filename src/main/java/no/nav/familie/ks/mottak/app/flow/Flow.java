package no.nav.familie.ks.mottak.app.flow;

import no.nav.familie.ks.mottak.app.task.HentSaksnummerFraJoarkTask;
import no.nav.familie.ks.mottak.app.task.SendMeldingTilDittNavTask;
import no.nav.familie.ks.mottak.app.task.SendSøknadTilSakTask;
import no.nav.familie.prosessering.domene.Task;

public enum Flow {

    JOURNALFOR {
        @Override
        public Task nextTask(Task task) {
            return new Task(HentSaksnummerFraJoarkTask.HENT_SAKSNUMMER_FRA_JOARK, task.getPayload());
        }
    },
    HENTSAKSNUMMERFRAJOARK {
        @Override
        public Task nextTask(Task task) {
            return new Task(SendSøknadTilSakTask.SEND_SØKNAD_TIL_SAK, task.getPayload());
        }
    },
    SENDSOKNADTILSAK {
        @Override
        public Task nextTask(Task task) {
            return new Task(SendMeldingTilDittNavTask.SEND_MELDING_TIL_DITT_NAV, task.getPayload());
        }
    },
    SENDMELDINGTILDITTNAV {
        @Override
        public Task nextTask(Task task) {
            return null; //finito
        }
    };

    public abstract Task nextTask(Task task);
}
