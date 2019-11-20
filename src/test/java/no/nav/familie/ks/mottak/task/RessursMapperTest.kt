package no.nav.familie.ks.mottak.task

import no.nav.familie.ks.kontrakter.sak.Ressurs
import no.nav.familie.ks.mottak.app.rest.RestTask
import no.nav.familie.ks.mottak.app.task.HentJournalpostIdFraJoarkTask.HENT_JOURNALPOSTID_FRA_JOARK
import no.nav.familie.prosessering.domene.Task
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDateTime

class RessursMapperTest {

    @Test
    fun map_task_til_ressurs_med_rest_task() {
        val task = Task.nyTaskMedStartFremITid(HENT_JOURNALPOSTID_FRA_JOARK, "payload", LocalDateTime.now())
        task.klarTilPlukk("foobar");

        val list = mutableListOf<RestTask>(RestTask(task, "journalpostID", "saksnummer", "søkerFødselsnummer"))

        val successRessurs = Ressurs.success(list, "OK");
        assertThat(successRessurs.data?.first()?.get("journalpostID")?.textValue()).isEqualTo("journalpostID")
        assertThat(successRessurs.data?.first()?.get("saksnummer")?.textValue()).isEqualTo("saksnummer")
        assertThat(successRessurs.data?.first()?.get("søkerFødselsnummer")?.textValue()).isEqualTo("søkerFødselsnummer")
        assertThat(successRessurs.data?.first()?.get("task")?.get("type")?.textValue()).isEqualTo(HENT_JOURNALPOSTID_FRA_JOARK);
        assertThat(successRessurs.data?.first()?.get("task")?.get("logg")?.first()?.get("type")?.textValue()).isEqualTo("KLAR_TIL_PLUKK");

    }
}

