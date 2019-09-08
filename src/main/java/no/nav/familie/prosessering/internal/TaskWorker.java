package no.nav.familie.prosessering.internal;

import no.nav.familie.ks.mottak.app.domene.TaskRepository;
import no.nav.familie.prosessering.AsyncTask;
import no.nav.familie.prosessering.TaskBeskrivelse;
import no.nav.familie.prosessering.TaskFeil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
class TaskWorker {

    private static final Logger log = LoggerFactory.getLogger(TaskWorker.class);
    private final TaskRepository taskRepository;
    private GenericApplicationContext context;
    private Map<String, AsyncTask> tasktypeMap = new HashMap<>();
    private Map<String, Integer> maxAntallFeilMap = new HashMap<>();

    @Autowired
    public TaskWorker(GenericApplicationContext context, TaskRepository taskRepository, List<AsyncTask> taskTyper) {
        this.context = context;
        this.taskRepository = taskRepository;
        taskTyper.forEach(this::kategoriserTask);
    }

    private void kategoriserTask(AsyncTask task) {
        final Class<?> aClass = AopProxyUtils.ultimateTargetClass(task);
        final var annotation = AnnotationUtils.findAnnotation(aClass, TaskBeskrivelse.class);
        Objects.requireNonNull(annotation, "annotasjon mangler");
        tasktypeMap.put(annotation.taskType(), task);
        maxAntallFeilMap.put(annotation.taskType(), annotation.maxAntallFeil());
    }


    @Async("taskProsesseringExecutor")
    void doTask(Long henvendelseId) {
        Objects.requireNonNull(henvendelseId, "id kan ikke være null");
        context.getBean(TaskWorker.class).doActualWork(henvendelseId);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void doActualWork(Long henvendelseId) {
        final var startTidspunkt = System.currentTimeMillis();
        var henvendelse = taskRepository.findById(henvendelseId).orElseThrow();
        log.info("Behandler task='{}'", henvendelse);
        Integer maxAntallFeil = 0;
        try {
            henvendelse.behandler();
            henvendelse = taskRepository.save(henvendelse);

            // finn tasktype
            AsyncTask task = finnTask(henvendelse.getType());
            maxAntallFeil = finnMaxAntallFeil(henvendelse.getType());

            // execute
            task.preCondition(henvendelse);
            task.doTask(henvendelse);
            task.postCondition(henvendelse);
            task.onCompletion(henvendelse);

            henvendelse.ferdigstill();
            log.info("Ferdigstiller task='{}'", henvendelse);

        } catch (Exception e) {
            log.warn("Kjøring av task='{}' feilet med feilmelding={}", henvendelse, e.getMessage());
            henvendelse.feilet(new TaskFeil(henvendelse, e), maxAntallFeil);
        }
        taskRepository.save(henvendelse);
        log.info("Fullført kjøring av task '{}', kjøretid={} ms", henvendelse, (System.currentTimeMillis() - startTidspunkt));
    }

    private AsyncTask finnTask(String taskType) {
        if (!tasktypeMap.containsKey(taskType)) {
            throw new IllegalArgumentException("Ukjent tasktype " + taskType);
        }
        return tasktypeMap.get(taskType);
    }

    private Integer finnMaxAntallFeil(String taskType) {
        if (!maxAntallFeilMap.containsKey(taskType)) {
            throw new IllegalArgumentException("Ukjent tasktype " + taskType);
        }
        return maxAntallFeilMap.get(taskType);
    }
}
