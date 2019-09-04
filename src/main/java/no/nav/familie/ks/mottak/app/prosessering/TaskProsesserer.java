package no.nav.familie.ks.mottak.app.prosessering;

import no.nav.familie.ks.mottak.app.domene.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskProsesserer {

    public static final int POLLING_DELAY = 30000;
    private static final Logger log = LoggerFactory.getLogger(TaskProsesserer.class);
    private TaskWorker worker;
    private TaskProsesseringRepository taskProsesseringRepository;

    @Autowired
    public TaskProsesserer(TaskWorker worker, TaskProsesseringRepository taskProsesseringRepository) {
        this.worker = worker;
        this.taskProsesseringRepository = taskProsesseringRepository;
    }

    @Scheduled(fixedDelay = POLLING_DELAY)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void pollAndExecute() {
        log.debug("Poller etter nye tasks");
        final var maxAntall = 5;
        final var henvendelser = taskProsesseringRepository.finnAlleTasksKlareForProsessering(maxAntall);
        log.info("Pollet {} tasks med max {}", henvendelser.size(), maxAntall);

        henvendelser.forEach(this::executeWork);
        log.info("Ferdig med polling, venter {} til neste kjøring.", POLLING_DELAY);
    }

    private void executeWork(Task task) {
        task.plukker();
        taskProsesseringRepository.save(task);
        taskProsesseringRepository.verifiserLås(task.getId());

        worker.doTask(task.getId());
    }
}
