package no.nav.familie.ks.mottak.app.mottak;

import no.nav.familie.ks.mottak.app.mottak.domene.Henvendelse;
import no.nav.familie.ks.mottak.app.mottak.domene.HenvendelseRepository;
import no.nav.security.oidc.api.ProtectedWithClaims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.transaction.Transactional;

@RestController
@RequestMapping("/api")
@ProtectedWithClaims(issuer = "selvbetjening", claimMap = {"acr=Level4"})
@Transactional
public class MottakController {

    private HenvendelseService henvendelseService;
    private HenvendelseRepository henvendelseRepository;

    @Autowired
    public MottakController(HenvendelseService henvendelseService, HenvendelseRepository henvendelseRepository) {
        this.henvendelseService = henvendelseService;
        this.henvendelseRepository = henvendelseRepository;
    }

    @PostMapping(value = "/soknad", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity mottaSoknad(@RequestBody String soknad) {
        henvendelseRepository.saveAndFlush(new Henvendelse(soknad));
        henvendelseService.prosesser();
        return new ResponseEntity(HttpStatus.OK);
    }
}
