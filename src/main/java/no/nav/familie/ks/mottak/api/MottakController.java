package no.nav.familie.ks.mottak.api;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api")
public class MottakController {


    @PostMapping(value = "/soknad", consumes = MediaType.APPLICATION_JSON_VALUE)
    public String mottaSoknad(@RequestBody String soknad) {
        return soknad;
    }

    @GetMapping("/ping")
    public String ping() {
        return "OK";
    }
}
