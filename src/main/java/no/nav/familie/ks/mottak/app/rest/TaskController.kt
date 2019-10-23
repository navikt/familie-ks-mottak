package no.nav.familie.ks.mottak.app.rest

import no.nav.familie.ks.kontrakter.sak.Ressurs
import no.nav.familie.ks.mottak.app.domene.SøknadRepository
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api")
@ProtectedWithClaims(issuer = "azuread")
class TaskController(
        private val taskRepository: TaskRepository,
        private val søknadRepository: SøknadRepository) {

    @GetMapping(path = ["/task/feilede"])
    fun task(): ResponseEntity<Ressurs> {
        logger.info("Henter feilede tasker")

        val ressurs: Ressurs = Result.runCatching {
            taskRepository.finnAlleFeiledeTasksTilFrontend()
                    .map { it.toRestTask(søknadRepository) }
        }
        .fold(
            onSuccess = { Ressurs.success(data = it) },
            onFailure = { e ->
                logger.error("Henting av tasker feilet", e)
                Ressurs.failure("Henting av tasker som har status 'FEILET', feilet.", e)
            }
        )

        return ResponseEntity.ok(ressurs)
    }

    @PutMapping(path = ["/task/rekjor"])
    fun rekjørTask(@RequestParam taskId: Long?): ResponseEntity<Ressurs> {
        return when (taskId) {
            null -> {
                taskRepository.finnAlleFeiledeTasksTilFrontend().map { taskRepository.save(it.klarTilPlukk()) }
                logger.info("Rekjører alle feilede tasks")

                val ressurs: Ressurs = Result.runCatching {
                    taskRepository.finnAlleFeiledeTasksTilFrontend().map {
                        it.toRestTask(søknadRepository)
                    }
                }
                .fold(
                    onSuccess = { Ressurs.success(data = it) },
                    onFailure = { e -> Ressurs.failure("Henting av tasker som har status 'FEILET', feilet.", e) }
                )

                ResponseEntity.ok(ressurs)
            }
            else -> {
                val task: Optional<Task> = taskRepository.findById(taskId)

                return when (task.isPresent) {
                    true -> {
                        taskRepository.save(task.get().klarTilPlukk())
                        logger.info("Rekjører task {}", taskId)

                        val ressurs: Ressurs = Result.runCatching {
                            taskRepository.finnAlleFeiledeTasksTilFrontend()
                                    .map { it.toRestTask(søknadRepository) }
                        }
                        .fold(
                            onSuccess = { Ressurs.success(data = it) },
                            onFailure = { e ->
                                Ressurs.failure("Henting av tasker som har status 'FEILET', feilet.", e)
                            }
                        )

                        ResponseEntity.ok(ressurs)
                    }
                    false -> ResponseEntity.ok(Ressurs.failure("Fant ikke task med task id $taskId"))
                }
            }
        }
    }


    companion object {
        val logger: Logger = LoggerFactory.getLogger(TaskController::class.java)
    }
}
