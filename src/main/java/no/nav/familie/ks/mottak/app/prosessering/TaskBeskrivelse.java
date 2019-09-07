package no.nav.familie.ks.mottak.app.prosessering;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface TaskBeskrivelse {

    /**
     * Antall retries på tasken før den settes til feilet
     *
     * @return antall forsøk
     */
    int maxAntallFeil() default 3;

    /**
     * Task typen
     *
     * @return typen
     */
    String taskType();
}
