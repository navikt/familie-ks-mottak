package no.nav.familie.prosessering.internal;

import no.nav.familie.prosessering.domene.TaskRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ScheduledTasksService {

    private TaskRepository taskRepository;

    public ScheduledTasksService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Scheduled(cron = "0 0 8 1/1 * ?")
    @Transactional
    public void retryFeilendeTask() {
        final var tasks = taskRepository.finnAlleFeiledeTasks();

        tasks.forEach(it -> taskRepository.save(it.klarTilPlukk()));
    }
}
