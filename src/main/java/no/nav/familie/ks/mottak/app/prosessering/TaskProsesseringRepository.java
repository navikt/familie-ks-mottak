package no.nav.familie.ks.mottak.app.prosessering;

import no.nav.familie.ks.mottak.app.domene.Task;
import no.nav.familie.ks.mottak.app.domene.TaskRepository;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.List;

@Repository
public class TaskProsesseringRepository {

    @PersistenceContext
    private EntityManager em;
    private TaskRepository taskRepository;

    public TaskProsesseringRepository(EntityManager em, TaskRepository taskRepository) {
        this.em = em;
        this.taskRepository = taskRepository;
    }

    List<Task> finnAlleTasksKlareForProsessering(int antall) {
        TypedQuery<Task> query = em.createQuery("SELECT h FROM Task h " +
            "WHERE h.status IN ('KLAR_TIL_PLUKK', 'UBEHANDLET') " +
            "ORDER BY h.opprettetTidspunkt DESC", Task.class);

        query.setLockMode(LockModeType.PESSIMISTIC_WRITE)
            .setMaxResults(antall);

        return query.getResultList();
    }

    public List<Task> finnAlleFeiledeTasks() {
        TypedQuery<Task> query = em.createQuery("SELECT h FROM Task h " +
            "WHERE h.status IN ('FEILET')", Task.class);

        query.setLockMode(LockModeType.PESSIMISTIC_WRITE);

        return query.getResultList();
    }

    public void save(Task task) {
        taskRepository.saveAndFlush(task);
    }

    public Task verifiserLås(Long id) {
        LockModeType lockMode = LockModeType.PESSIMISTIC_FORCE_INCREMENT;
        Task entity = em.find(Task.class, id);
        if (entity == null) {
            throw new IllegalArgumentException("Fant ikke ");
        } else {
            em.lock(entity, lockMode);
        }
        return entity;
    }
}
