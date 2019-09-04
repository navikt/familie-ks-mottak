package no.nav.familie.ks.mottak.app.prosessering;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Transactional(propagation = Propagation.REQUIRES_NEW)
public @interface TaskBeskrivelse {

    /**
     * Task typen
     *
     * @return typen
     */
    String taskType();
}
