package no.nav.familie.ks.mottak.app.task;

import no.nav.familie.ks.mottak.app.prosessering.TaskProsesseringRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class RetryFeilendeTasks {

    private TaskProsesseringRepository taskRepository;

    public RetryFeilendeTasks(TaskProsesseringRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Scheduled(cron = "0 0 8 1/1 * ?")
    public void retryFeilendeTask() {
        final var tasks = taskRepository.finnAlleFeiledeTasks();

        tasks.forEach(it -> taskRepository.save(it.klarTilPlukk()));
    }
}
