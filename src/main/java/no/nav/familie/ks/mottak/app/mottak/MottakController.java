package no.nav.familie.ks.mottak.app.mottak;

import no.nav.security.token.support.core.api.ProtectedWithClaims;
import no.nav.security.token.support.core.api.Unprotected;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.transaction.Transactional;

@RestController
@RequestMapping("/api")
@ProtectedWithClaims(issuer = "tokenx", claimMap = {"acr=Level4"})
@Transactional
public class MottakController {

    private SøknadService søknadService;

    @Autowired
    public MottakController(SøknadService søknadService) {
        this.søknadService = søknadService;
    }

    @PostMapping(value = "/soknadmedvedlegg", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity mottaSoknadMedVedlegg(@RequestBody SøknadDto søknad) {
        søknadService.lagreSoknadOgLagTask(søknad);
        return new ResponseEntity(HttpStatus.OK);
    }

    @GetMapping(value = "/ping", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> ping(){
        return new ResponseEntity<>("Pong", HttpStatus.OK);
    }
}
