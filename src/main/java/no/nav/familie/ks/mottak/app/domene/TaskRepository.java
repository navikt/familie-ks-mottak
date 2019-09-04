package no.nav.familie.ks.mottak.app.domene;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import javax.persistence.LockModeType;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Task> findById(Long id);

    @SuppressWarnings("unchecked")
    @Lock(LockModeType.PESSIMISTIC_FORCE_INCREMENT)
    Task save(Task task);
}
