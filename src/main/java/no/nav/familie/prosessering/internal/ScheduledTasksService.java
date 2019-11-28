package no.nav.familie.prosessering.internal;

import no.nav.familie.prosessering.domene.Task;
import no.nav.familie.prosessering.domene.TaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static no.nav.familie.prosessering.domene.TaskLogg.BRUKERNAVN_NÅR_SIKKERHETSKONTEKST_IKKE_FINNES;

@Service
public class ScheduledTasksService {
    private static final String CRON_DAILY_8AM = "0 0 8 1/1 * ?";
    private static final String CRON_DAILY_9AM = "0 0 9 1/1 * ?";
    private static final Logger logger = LoggerFactory.getLogger(ScheduledTasksService.class);

    private TaskRepository taskRepository;

    public ScheduledTasksService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Scheduled(cron = CRON_DAILY_8AM)
    @Transactional
    public void retryFeilendeTask() {
        final var tasks = taskRepository.finnAlleFeiledeTasks();

        tasks.forEach(it -> taskRepository.save(it.klarTilPlukk(BRUKERNAVN_NÅR_SIKKERHETSKONTEKST_IKKE_FINNES)));
    }

    @Scheduled(cron = CRON_DAILY_9AM)
    @Transactional
    public void slettTasksKlarForSletting() {
        List<Task> klarForSletting = taskRepository.finnTasksKlarForSletting(LocalDateTime.now().minusWeeks(2));
        klarForSletting.forEach(it -> {
            logger.info("Task klar for sletting. {}", it);
            taskRepository.delete(it);
        });
    }
}
