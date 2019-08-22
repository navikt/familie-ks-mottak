package no.nav.familie.ks.mottak.app.mottak;

import no.nav.familie.ks.mottak.app.mottak.domene.Henvendelse;
import no.nav.familie.ks.mottak.app.mottak.domene.HenvendelseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class HenvendelseService {

    private static final Logger log = LoggerFactory.getLogger(HenvendelseService.class);

    private HenvendelseRepository henvendelseRepository;
    private SøknadService søknadService;

    @Autowired
    public HenvendelseService(HenvendelseRepository henvendelseRepository, SøknadService søknadService) {
        this.henvendelseRepository = henvendelseRepository;
        this.søknadService = søknadService;
    }

    public void prosesser() {
        final var henvendelser = henvendelseRepository.finnAlleHenvendeserKlareForProsessering();

        henvendelser.forEach(henvendelse -> {
            try {
                henvendelse.plukker();
                prosseser(henvendelse);
                henvendelse.ferdigstill();
                henvendelseRepository.saveAndFlush(henvendelse);
            } catch (Exception e) {
                log.info("Behandling av henvendelse='{}' feilet.", henvendelse, e);
                henvendelse.feilet();
                henvendelseRepository.saveAndFlush(henvendelse);
            }
        });
    }

    private void prosseser(Henvendelse henvendelse) throws IOException, InterruptedException {
        henvendelse.behandler();
        søknadService.sendTilSak(henvendelse.getPayload());
    }
}
