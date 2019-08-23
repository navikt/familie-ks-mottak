package no.nav.familie.ks.mottak.app.mottak;

import no.nav.familie.http.sts.StsRestClient;
import no.nav.familie.ks.mottak.app.mottak.domene.Henvendelse;
import no.nav.familie.ks.mottak.app.mottak.domene.HenvendelseRepository;
import no.nav.familie.ks.mottak.app.mottak.domene.HenvendelseStatus;
import no.nav.familie.ks.mottak.config.ApplicationConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        classes = {ApplicationConfig.class},
        loader = AnnotationConfigContextLoader.class)
@DataJpaTest(excludeAutoConfiguration = {FlywayAutoConfiguration.class})
public class HenvendelseServiceJpaTest {

    @MockBean
    private SøknadService søknadService;
    @MockBean
    private StsRestClient stsRestClient;
    @Autowired
    private HenvendelseRepository repository;

    @Autowired
    private HenvendelseService henvendelseService;

    @Test
    public void skal_bli_ferdigstilt() {
        final var henvendelse1 = new Henvendelse("{'a'='b'}");
        repository.saveAndFlush(henvendelse1);
        assertThat(henvendelse1.getStatus()).isEqualTo(HenvendelseStatus.UBEHANDLET);

        henvendelseService.prosesser();

        assertThat(henvendelse1.getStatus()).isEqualTo(HenvendelseStatus.FERDIG);
        assertThat(henvendelse1.getLogg()).hasSize(4);
    }

    @Test
    public void skal_håndtere_feil() throws IOException, InterruptedException {
        final var henvendelse1 = new Henvendelse("");
        repository.saveAndFlush(henvendelse1);
        assertThat(henvendelse1.getStatus()).isEqualTo(HenvendelseStatus.UBEHANDLET);
        doThrow(new IllegalStateException()).when(søknadService).sendTilSak(any());

        henvendelseService.prosesser();

        assertThat(henvendelse1.getStatus()).isEqualTo(HenvendelseStatus.FEILET);
        assertThat(henvendelse1.getLogg()).hasSize(4);
    }
}