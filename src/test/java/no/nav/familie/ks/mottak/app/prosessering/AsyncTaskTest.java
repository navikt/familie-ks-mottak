package no.nav.familie.ks.mottak.app.prosessering;

import no.nav.familie.http.sts.StsRestClient;
import no.nav.familie.ks.mottak.app.mottak.SøknadService;
import no.nav.familie.ks.mottak.config.ApplicationConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
    classes = {ApplicationConfig.class},
    loader = AnnotationConfigContextLoader.class)
@DataJpaTest(excludeAutoConfiguration = {FlywayAutoConfiguration.class})
public class AsyncTaskTest {
    @MockBean
    private SøknadService søknadService;
    @MockBean
    private StsRestClient stsRestClient;

    @Autowired
    private List<AsyncTask> tasker;

    @Test
    public void skal_ha_annotasjon() {
        assertThat(tasker.stream().anyMatch(it -> AnnotationUtils.isAnnotationDeclaredLocally(TaskBeskrivelse.class, it.getClass()))).isFalse();
    }
}
