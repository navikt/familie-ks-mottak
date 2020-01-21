package no.nav.familie.ks.mottak.task

import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.ks.mottak.app.task.JournalførSøknadTask.JOURNALFØR_SØKNAD
import no.nav.familie.prosessering.domene.Loggtype
import no.nav.familie.prosessering.domene.Task
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDateTime

class RessursMapperTest {

    @Test
    fun `map task til ressurs med rest task`() {
        val task = Task.Companion.nyTaskMedTriggerTid(JOURNALFØR_SØKNAD, "payload", LocalDateTime.now())
        task.klarTilPlukk("foobar")
        task.metadata.apply {
            put("journalpostID", "journalpostID")
        }


        val list = mutableListOf(task)

        val successRessurs = Ressurs.success(list, "OK")
        assertThat(successRessurs.data?.first()?.metadata?.get("journalpostID")).isEqualTo("journalpostID")
        assertThat(successRessurs.data?.first()?.taskStepType).isEqualTo(JOURNALFØR_SØKNAD)
        assertThat(successRessurs.data?.first()?.logg?.first()?.type).isEqualTo(Loggtype.KLAR_TIL_PLUKK)

    }
}

