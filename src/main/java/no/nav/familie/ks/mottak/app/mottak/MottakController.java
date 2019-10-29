package no.nav.familie.ks.mottak.app.mottak;

import no.nav.security.token.support.core.api.ProtectedWithClaims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;

@RestController
@RequestMapping("/api")
@ProtectedWithClaims(issuer = "selvbetjening", claimMap = {"acr=Level4"})
@Transactional
public class MottakController {

    private SøknadService søknadService;

    @Autowired
    public MottakController(SøknadService søknadService) {
        this.søknadService = søknadService;
    }

    @PostMapping(value = "/soknadmedvedlegg", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity mottaSoknadMedVedlegg(@RequestBody SøknadDto søknad,
                                                @Nullable
                                                @RequestHeader("journalforSelv") String journalførSelv) {
        søknadService.lagreSoknadOgLagTask(søknad, Boolean.parseBoolean(journalførSelv));
        return new ResponseEntity(HttpStatus.OK);
    }

    @GetMapping(value = "/test")
    public void getTest() {
        søknadService.getTest();
    }
}
