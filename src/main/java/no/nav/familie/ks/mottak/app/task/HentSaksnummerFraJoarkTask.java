package no.nav.familie.ks.mottak.app.task;

import no.nav.familie.ks.mottak.app.mottak.HentJournalpostService;
import no.nav.familie.ks.mottak.app.util.TaskUtil;
import no.nav.familie.prosessering.AsyncTaskStep;
import no.nav.familie.prosessering.TaskStepBeskrivelse;
import no.nav.familie.prosessering.domene.Avvikstype;
import no.nav.familie.prosessering.domene.Loggtype;
import no.nav.familie.prosessering.domene.Task;
import no.nav.familie.prosessering.domene.TaskRepository;
import no.nav.familie.prosessering.internal.RekjørSenereException;

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
    private final TaskRepository taskRepository;
    private final HentJournalpostService hentJournalpostService;


    @Autowired
    public HentSaksnummerFraJoarkTask(TaskRepository taskRepository, HentJournalpostService hentJournalpostService) {
        this.taskRepository = taskRepository;
        this.hentJournalpostService = hentJournalpostService;
    }

    @Override
    public void doTask(Task task) {

        long antallFeilendeForsøk = task.getLogg().stream().filter(t -> t.getType() == Loggtype.FEILET ).count();

        if(antallFeilendeForsøk >= MAX_ANTALL_FEIL - 2) { // -2 for å unngå alarmer når max forsøk er nådd
            task.avvikshåndter(Avvikstype.ANNET,"Oppdaterer ikke oppgave med beslutningsstøtte", "VL");
            taskRepository.saveAndFlush(task);
            return;
        }

        try {
            String saksnummer = hentJournalpostService.hentSaksnummer(task.getPayload());
            task.getMetadata().put("saksnummer", saksnummer);
            taskRepository.saveAndFlush(task);
        } catch (Exception e) {
            if (LocalDateTime.now().getDayOfWeek() == DayOfWeek.SATURDAY ||
                LocalDateTime.now().getDayOfWeek() == DayOfWeek.SUNDAY) {
                throw new RekjørSenereException("Task skal ikke rekjøres i helgene",
                                                TaskUtil.nesteTriggertidEksluderHelg(LocalDateTime.now()));
            }
            throw e;
        }
    }

    @Override
    public void onCompletion(Task task) {
        if (task.getMetadata().contains("saksnummer")) { // ikke fortsett hvis vi ikke har noe saksnummer
            Task nesteTask =
                Task.Companion.nyTask(SendSøknadTilSakTask.SEND_SØKNAD_TIL_SAK, task.getPayload(), task.getMetadata());
            taskRepository.save(nesteTask);
        }
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
