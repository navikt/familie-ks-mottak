package no.nav.familie.ks.mottak.app.mottak;

import no.nav.familie.ks.mottak.app.task.JournalførSøknadTask;
import no.nav.familie.prosessering.domene.Task;
import no.nav.familie.prosessering.domene.TaskRepository;
import no.nav.security.oidc.api.ProtectedWithClaims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;

@RestController
@RequestMapping("/api")
@ProtectedWithClaims(issuer = "selvbetjening", claimMap = {"acr=Level4"})
@Transactional
public class MottakController {

    private TaskRepository taskRepository;
    private SøknadService søknadService;

    @Autowired
    public MottakController(TaskRepository taskRepository, SøknadService søknadService) {
        this.taskRepository = taskRepository;
        this.søknadService = søknadService;
    }

    @Deprecated
    @PostMapping(value = "/soknad", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity mottaSoknad(@RequestBody String soknad) {
        final var task = Task.nyTask(JournalførSøknadTask.JOURNALFØR_SØKNAD, soknad);
        taskRepository.save(task);
        return new ResponseEntity(HttpStatus.OK);
    }

    @PostMapping(value = "/soknadmedvedlegg", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity mottaSoknadMedVedlegg(@RequestBody SøknadDto søknad) {
        søknadService.lagreSoknadOgLagTask(søknad);
        return new ResponseEntity(HttpStatus.OK);
    }
}
