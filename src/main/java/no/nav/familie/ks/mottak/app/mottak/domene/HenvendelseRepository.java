package no.nav.familie.ks.mottak.app.mottak.domene;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;

public interface HenvendelseRepository extends JpaRepository<Henvendelse, Long> {

    Collection<Henvendelse> finnAlleHenvendeserKlareForProsessering();
}
