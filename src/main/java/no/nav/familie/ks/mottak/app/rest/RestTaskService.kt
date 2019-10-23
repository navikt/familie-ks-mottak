package no.nav.familie.ks.mottak.app.rest

import no.nav.familie.ks.kontrakter.sak.Ressurs
import no.nav.familie.ks.mottak.app.domene.SøknadRepository
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class RestTaskService(
    private val taskRepository: TaskRepository,
    private val søknadRepository: SøknadRepository) {

    fun hentFeiledeTasks(): Ressurs {
        logger.info("Henter feilede tasker")

        return Result.runCatching {
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
    }

    @Transactional
    fun rekjørTask(taskId: Long?): Ressurs {
        return when (taskId) {
            null -> {
                taskRepository.finnAlleFeiledeTasksTilFrontend().map { taskRepository.saveAndFlush(it.klarTilPlukk()) }
                logger.info("Rekjører alle feilede tasks")

                Result.runCatching {
                    taskRepository.finnAlleFeiledeTasksTilFrontend().map {
                        it.toRestTask(søknadRepository)
                    }
                }
                .fold(
                    onSuccess = { Ressurs.success(data = it) },
                    onFailure = { e -> Ressurs.failure("Henting av tasker som har status 'FEILET', feilet.", e) }
                )
            }
            else -> {
                val task: Optional<Task> = taskRepository.findById(taskId)

                return when (task.isPresent) {
                    true -> {
                        taskRepository.saveAndFlush(task.get().klarTilPlukk())
                        logger.info("Rekjører task {}", taskId)

                        Result.runCatching {
                            taskRepository.finnAlleFeiledeTasksTilFrontend()
                                    .map { it.toRestTask(søknadRepository) }
                        }
                        .fold(
                            onSuccess = { Ressurs.success(data = it) },
                            onFailure = { e ->
                                Ressurs.failure("Henting av tasker som har status 'FEILET', feilet.", e)
                            }
                        )
                    }
                    false -> Ressurs.failure("Fant ikke task med task id $taskId")
                }
            }
        }
    }

    companion object {
        val logger: Logger = LoggerFactory.getLogger(RestTaskService::class.java)
    }
}
