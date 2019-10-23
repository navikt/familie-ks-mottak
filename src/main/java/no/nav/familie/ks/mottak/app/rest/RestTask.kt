package no.nav.familie.ks.mottak.app.rest

import no.nav.familie.ks.mottak.app.domene.Soknad
import no.nav.familie.ks.mottak.app.domene.SøknadRepository
import no.nav.familie.prosessering.domene.Task
import java.util.*

data class RestTask(
        val task: Task,
        val journalpostID: String?,
        val saksnummer: String?,
        val søkerFødselsnummer: String)

fun Task.toRestTask(søknadRepository: SøknadRepository): RestTask {
    val søknad: Optional<Soknad> = søknadRepository.findById(this.id)

    return when (søknad.isPresent) {
        true -> RestTask(
                task = this,
                journalpostID = søknad.get().journalpostID,
                saksnummer = søknad.get().saksnummer,
                søkerFødselsnummer = søknad.get().fnr ?: "ukjent"
        )
        else -> RestTask(
                task = this,
                journalpostID = null,
                saksnummer = null,
                søkerFødselsnummer = ""
        )
    }
}
