package no.nav.familie.ks.mottak.task

import no.nav.familie.ks.kontrakter.sak.Ressurs
import no.nav.familie.ks.mottak.app.task.JournalførSøknadTask.JOURNALFØR_SØKNAD
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.rest.RestTask
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDateTime

class RessursMapperTest {

    @Test
    fun `map task til ressurs med rest task`() {
        val task = Task.Companion.nyTaskMedTriggerTid(JOURNALFØR_SØKNAD, "payload", LocalDateTime.now())
        task.klarTilPlukk("foobar")

        val list = mutableListOf(RestTask(task, "journalpostID", "saksnummer", "søkerFødselsnummer"))

        val successRessurs = Ressurs.success(list, "OK")
        assertThat(successRessurs.data?.first()?.get("journalpostId")?.textValue()).isEqualTo("journalpostID")
        assertThat(successRessurs.data?.first()?.get("saksnummer")?.textValue()).isEqualTo("saksnummer")
        assertThat(successRessurs.data?.first()?.get("søkerFødselsnummer")?.textValue()).isEqualTo("søkerFødselsnummer")
        assertThat(successRessurs.data?.first()?.get("task")?.get("taskStepType")?.textValue()).isEqualTo(JOURNALFØR_SØKNAD)
        assertThat(successRessurs.data?.first()?.get("task")?.get("logg")?.first()?.get("type")?.textValue()).isEqualTo("KLAR_TIL_PLUKK")

    }
}

