package no.nav.familie.ks.mottak.app.mottak;

import no.nav.familie.ks.mottak.app.mottak.domene.Henvendelse;
import no.nav.familie.ks.mottak.app.mottak.domene.HenvendelseRepository;
import no.nav.familie.ks.mottak.app.mottak.domene.HenvendelseStatus;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class HenvendelseServiceTest {

    private SøknadService søknadService = mock(SøknadService.class);
    private HenvendelseRepository repository = mock(HenvendelseRepository.class);
    private HenvendelseService henvendelseService = new HenvendelseService(repository, søknadService);

    @Test
    public void skal_bli_ferdigstilt() {
        final var henvendelse1 = new Henvendelse("");
        when(repository.finnAlleHenvendeserKlareForProsessering()).thenReturn(List.of(henvendelse1));
        assertThat(henvendelse1.getStatus()).isEqualTo(HenvendelseStatus.UBEHANDLET);

        henvendelseService.prosesser();

        assertThat(henvendelse1.getStatus()).isEqualTo(HenvendelseStatus.FERDIG);
        assertThat(henvendelse1.getLogg()).hasSize(3);
    }

    @Test
    public void skal_håndtere_feil() throws IOException, InterruptedException {
        final var henvendelse1 = new Henvendelse("");
        when(repository.finnAlleHenvendeserKlareForProsessering()).thenReturn(List.of(henvendelse1));
        assertThat(henvendelse1.getStatus()).isEqualTo(HenvendelseStatus.UBEHANDLET);
        doThrow(new IllegalStateException()).when(søknadService).sendTilSak(any());

        henvendelseService.prosesser();

        assertThat(henvendelse1.getStatus()).isEqualTo(HenvendelseStatus.FEILET);
        assertThat(henvendelse1.getLogg()).hasSize(3);
    }
}