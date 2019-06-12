package no.nav.familie.ks.mottak.api;

import no.nav.security.oidc.api.ProtectedWithClaims;
import no.nav.security.oidc.api.Unprotected;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api")
@ProtectedWithClaims(issuer = "selvbetjening", claimMap = { "acr=Level4" })
public class MottakController {


    @PostMapping(value = "/soknad", consumes = MediaType.APPLICATION_JSON_VALUE)
    public String mottaSoknad(@RequestBody String soknad) {
        return soknad;
    }

    @GetMapping("/ping")
    //@Unprotected
    public String ping() {
        return "OK";
    }
}
