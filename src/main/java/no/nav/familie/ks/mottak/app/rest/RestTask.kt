package no.nav.familie.ks.mottak.app.rest

import no.nav.familie.ks.mottak.app.domene.Soknad
import no.nav.familie.prosessering.domene.Task

data class RestTask(
    val task: Task,
    val journalpostID: String?,
    val saksnummer: String?,
    val søkerFødselsnummer: String)

fun Task.toRestTask(søknad: Soknad?) = RestTask(
    task = this,
    journalpostID = søknad?.journalpostID,
    saksnummer = søknad?.saksnummer,
    søkerFødselsnummer = søknad?.fnr?:"ukjent"
)
