package no.nav.familie.ks.mottak.app.task;

import no.nav.familie.ks.mottak.app.mottak.HentJournalpostService;
import no.nav.familie.ks.mottak.app.util.TaskUtil;
import no.nav.familie.prosessering.AsyncTaskStep;
import no.nav.familie.prosessering.TaskStepBeskrivelse;
import no.nav.familie.prosessering.domene.Loggtype;
import no.nav.familie.prosessering.domene.Task;
import no.nav.familie.prosessering.domene.TaskLogg;
import no.nav.familie.prosessering.domene.TaskRepository;
import no.nav.familie.prosessering.error.RekjørSenereException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;


@Service
@TaskStepBeskrivelse(taskStepType = HentSaksnummerFraJoarkTask.HENT_SAKSNUMMER_FRA_JOARK,
                     maxAntallFeil = HentSaksnummerFraJoarkTask.MAX_ANTALL_FEIL,
                     beskrivelse = "Hent saksnummer fra joark",
                     triggerTidVedFeilISekunder = 60 * 15)
public class HentSaksnummerFraJoarkTask implements AsyncTaskStep {

    public static final int MAX_ANTALL_FEIL = 200;
    public static final String HENT_SAKSNUMMER_FRA_JOARK = "hentSaksnummerFraJoark";
    private static final Logger LOG = LoggerFactory.getLogger(HentSaksnummerFraJoarkTask.class);
    private static final Logger secureLogger = LoggerFactory.getLogger("secureLogger");
    private final TaskRepository taskRepository;
    private final HentJournalpostService hentJournalpostService;


    @Autowired
    public HentSaksnummerFraJoarkTask(TaskRepository taskRepository, HentJournalpostService hentJournalpostService) {
        this.taskRepository = taskRepository;
        this.hentJournalpostService = hentJournalpostService;
    }

    @Override
    public void doTask(Task task) {

        long antallFeilendeForsøk = task.getLogg().stream().filter(t -> t.getType() == Loggtype.FEILET).count();

        if (antallFeilendeForsøk >= MAX_ANTALL_FEIL - 2) { // -2 for å unngå alarmer når max forsøk er nådd
            task.getMetadata().put("avsluttet", "ja");
            task.getLogg().add(new TaskLogg(0,
                                            "VL", Loggtype.AVVIKSHÅNDTERT,
                                            "node1",
                                            "Oppdaterer ikke oppgave med beslutningsstøtte",
                                            LocalDateTime.now()
            ));
            secureLogger.warn("HentSaksnummerFraJoarkTask {} har feilet og avsluttet, antallFeilendeForsøk = {}",
                              task.getId(),
                              antallFeilendeForsøk);
        } else {
            try {
                String saksnummer = hentJournalpostService.hentSaksnummer(task.getPayload());
                task.getMetadata().put("saksnummer", saksnummer);
                Task nesteTask =
                    new Task(SendSøknadTilSakTask.SEND_SØKNAD_TIL_SAK, task.getPayload(), task.getMetadata());
                taskRepository.save(nesteTask);
            } catch (Exception e) {
                if (LocalDateTime.now().getDayOfWeek() == DayOfWeek.SATURDAY ||
                    LocalDateTime.now().getDayOfWeek() == DayOfWeek.SUNDAY) {
                    throw new RekjørSenereException("Task skal ikke rekjøres i helgene",
                                                    TaskUtil.nesteTriggertidEksluderHelg(LocalDateTime.now()));
                }
                throw e;
            }
        }
    }

    @Override
    public void onCompletion(Task task) {
        //NOP
    }

    @Override
    public void postCondition(@NotNull Task task) {
        //NOP
    }

    @Override
    public void preCondition(@NotNull Task task) {
        //NOP
    }
}
