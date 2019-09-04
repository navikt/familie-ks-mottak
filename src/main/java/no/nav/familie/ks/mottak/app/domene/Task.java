package no.nav.familie.ks.mottak.app.domene;

import no.nav.familie.ks.mottak.app.prosessering.TaskFeil;

import javax.persistence.*;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "HENVENDELSE")
public class Task {

    @Transient
    private Integer MAX_ANTALL_FEIL = 3;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "henvendelse_seq")
    @SequenceGenerator(name = "henvendelse_seq")
    private Long id;

    @Lob
    @Column(name = "payload", updatable = false, columnDefinition = "text")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status = Status.UBEHANDLET;

    @Column(name = "opprettet_tid", nullable = false, updatable = false)
    private LocalDateTime opprettetTidspunkt; // NOSONAR

    @Column(name = "type", nullable = false, updatable = false)
    private String type;

    @Version
    private Long versjon;

    // Setter fetch til eager fordi asynctask ikke får lastet disse hvis ikke den er prelastet.
    @OneToMany(mappedBy = "task", fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    private List<TaskLogg> logg = new ArrayList<>();

    Task() {
    }

    public Task(String type, String payload) {
        this.type = type;
        this.payload = payload;
    }

    @PrePersist
    protected void onCreate() {
        this.opprettetTidspunkt = LocalDateTime.now();
        this.logg.add(new TaskLogg(this, LoggType.UBEHANDLET));
    }

    public String getPayload() {
        return payload;
    }

    public Task setPayload(String payload) {
        this.payload = payload;
        return this;
    }

    public Task behandler() {
        this.status = Status.BEHANDLER;
        this.logg.add(new TaskLogg(this, LoggType.BEHANDLER));
        return this;
    }

    public Task klarTilPlukk() {
        this.status = Status.KLAR_TIL_PLUKK;
        this.logg.add(new TaskLogg(this, LoggType.KLAR_TIL_PLUKK));
        return this;
    }

    public Task plukker() {
        this.status = Status.PLUKKET;
        this.logg.add(new TaskLogg(this, LoggType.PLUKKET));
        return this;
    }

    public Long getId() {
        return id;
    }

    public Task ferdigstill() {
        this.status = Status.FERDIG;
        this.logg.add(new TaskLogg(this, LoggType.FERDIG));
        return this;
    }

    public List<TaskLogg> getLogg() {
        return logg;
    }

    public Status getStatus() {
        return status;
    }

    public Task feilet(TaskFeil feil) {
        try {
            this.logg.add(new TaskLogg(this, LoggType.FEILET, feil.writeValueAsString()));
        } catch (IOException e) {
            this.logg.add(new TaskLogg(this, LoggType.FEILET));
        }
        final var antallFeilendeForsøk = logg.stream().filter(it -> it.getType().equals(LoggType.FEILET)).count();
        if (MAX_ANTALL_FEIL > antallFeilendeForsøk) {
            this.status = Status.KLAR_TIL_PLUKK;
        } else {
            this.status = Status.FEILET;
        }
        return this;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return "Task{" +
            "id=" + id +
            ", status=" + status +
            ", opprettetTidspunkt=" + opprettetTidspunkt +
            ", versjon=" + versjon +
            '}';
    }
}
