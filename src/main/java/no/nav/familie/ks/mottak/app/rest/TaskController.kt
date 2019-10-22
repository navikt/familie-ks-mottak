package no.nav.familie.ks.mottak.app.rest

import no.nav.familie.prosessering.domene.TaskRepository
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.security.token.support.core.api.Unprotected
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
@ProtectedWithClaims(issuer = "azuread" )
class TaskController (
    private val taskRepository: TaskRepository) {

    @GetMapping(path = ["/task/feilede"])
    fun fagsak(): ResponseEntity<Ressurs> {
        logger.info("Henter feilede tasker")

        val ressurs: Ressurs = Result.runCatching { taskRepository.finnAlleFeiledeTasksTilFrontend() }
            .fold(
                onSuccess = { Ressurs.success(data = it) },
                onFailure = { e -> Ressurs.failure("Henting av tasker som har status 'FEILET', feilet.", e) }
            )

        return ResponseEntity.ok(ressurs)
    }

    companion object {
        val logger: Logger = LoggerFactory.getLogger(TaskController::class.java)
    }
}
