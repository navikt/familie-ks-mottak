package no.nav.familie.ks.mottak.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api")
public class MottakController {


    @PostMapping("/soknad")
    public String mottaSoknad(String soknad) {
        return soknad;
    }

    @GetMapping("/ping")
    public String ping() {
        return "OK";
    }
}
