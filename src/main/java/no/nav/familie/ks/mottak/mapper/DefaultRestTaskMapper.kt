package no.nav.familie.ba.mottak.mapper

import no.nav.familie.ks.mottak.app.domene.SøknadRepository
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.rest.RestTask
import no.nav.familie.prosessering.rest.RestTaskMapper
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class DefaultRestTaskMapper(
        private val søknadRepository: SøknadRepository
) : RestTaskMapper {
    override fun toDto(task: Task): RestTask {
        val søknad = søknadRepository.findByIdOrNull(task?.payload?.toLong());

        return RestTask(task, søknad?.journalpostID, søknad?.saksnummer, søknad?.fnr ?: "ukjent")
    }
}
