package no.nav.familie.ks.mottak.app.mottak;

import no.nav.familie.ks.mottak.app.domene.Task;
import no.nav.familie.ks.mottak.app.domene.TaskRepository;
import no.nav.familie.ks.mottak.app.task.SendSøknadTilSakTask;
import no.nav.security.oidc.api.ProtectedWithClaims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(MottakController.class);

    private TaskRepository taskRepository;

    @Autowired
    public MottakController(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @PostMapping(value = "/soknad", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity mottaSoknad(@RequestBody String soknad) {
        final var task = new Task(SendSøknadTilSakTask.SEND_SØKNAD_TIL_SAK, soknad);
        taskRepository.save(task);
        return new ResponseEntity(HttpStatus.OK);
    }
}
