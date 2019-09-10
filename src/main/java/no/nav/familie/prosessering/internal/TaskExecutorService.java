package no.nav.familie.prosessering.internal;

import no.nav.familie.prosessering.domene.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskExecutorService {

    public static final int POLLING_DELAY = 30000;
    private static final Logger log = LoggerFactory.getLogger(TaskExecutorService.class);
    private TaskWorker worker;
    private TaskExecutor taskExecutor;
    private TaskProsesseringRepository taskProsesseringRepository;

    @Autowired
    public TaskExecutorService(TaskWorker worker,
                               @Qualifier("taskExecutor") TaskExecutor taskExecutor,
                               TaskProsesseringRepository taskProsesseringRepository) {
        this.worker = worker;
        this.taskExecutor = taskExecutor;
        this.taskProsesseringRepository = taskProsesseringRepository;
    }

    @Scheduled(fixedDelay = POLLING_DELAY)
    @Transactional
    public void pollAndExecute() {
        log.debug("Poller etter nye tasks");
        final var maxAntall = 10;
        final var pollingSize = calculatePollingSize(maxAntall);

        final var minCapacity = 2;
        if (pollingSize > minCapacity) {
            final var henvendelser = taskProsesseringRepository.finnAlleTasksKlareForProsessering(pollingSize);
            log.info("Pollet {} tasks med max {}", henvendelser.size(), maxAntall);

            henvendelser.forEach(this::executeWork);
        } else {
            log.info("Pollet ingen tasks siden kapasiteten var {} < {}", pollingSize, minCapacity);
        }
        log.info("Ferdig med polling, venter {} ms til neste kjøring.", POLLING_DELAY);
    }

    private int calculatePollingSize(int maxAntall) {
        final var remainingCapacity = ((ThreadPoolTaskExecutor) taskExecutor).getThreadPoolExecutor().getQueue().remainingCapacity();
        final var pollingSize = Math.min(remainingCapacity, maxAntall);
        log.info("Ledig kapasitet i kø {}, poller etter {}", remainingCapacity, pollingSize);
        return pollingSize;
    }

    private void executeWork(Task task) {
        task.plukker();
        taskProsesseringRepository.save(task);
        taskProsesseringRepository.verifiserLås(task.getId());

        worker.doTask(task.getId());
    }
}
