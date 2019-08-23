package no.nav.familie.ks.mottak.app.mottak.domene;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "HENVENDELSE")
@NamedQuery(name = "Henvendelse.finnAlleHenvendeserKlareForProsessering",
        query = "SELECT h FROM Henvendelse h WHERE h.status IN ('KLAR_TIL_PLUKK', 'UBEHANDLET') ORDER BY opprettetTidspunkt DESC ",
        lockMode = LockModeType.PESSIMISTIC_WRITE)
public class Henvendelse {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "henvendelse_seq")
    @SequenceGenerator(name = "henvendelse_seq")
    private Long id;

    @Lob
    @Column(name = "payload", nullable = false, updatable = false, columnDefinition = "text")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private HenvendelseStatus status = HenvendelseStatus.UBEHANDLET;

    @Column(name = "opprettet_tid", nullable = false, updatable = false)
    private LocalDateTime opprettetTidspunkt; // NOSONAR

    @Version
    private Long versjon;

    @OneToMany(mappedBy = "henvendelse", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    private List<HenvendelseLogg> logg = new ArrayList<>();

    Henvendelse() {
    }

    public Henvendelse(String payload) {
        this.payload = payload;
    }

    @PrePersist
    protected void onCreate() {
        this.opprettetTidspunkt = LocalDateTime.now();
        this.logg.add(new HenvendelseLogg(this, LoggType.UBEHANDLET));
    }

    public String getPayload() {
        return payload;
    }

    public Henvendelse setPayload(String payload) {
        this.payload = payload;
        return this;
    }

    public Henvendelse behandler() {
        this.status = HenvendelseStatus.BEHANDLER;
        this.logg.add(new HenvendelseLogg(this, LoggType.BEHANDLER));
        return this;
    }

    public Henvendelse klarTilPlukk() {
        this.status = HenvendelseStatus.KLAR_TIL_PLUKK;
        this.logg.add(new HenvendelseLogg(this, LoggType.KLAR_TIL_PLUKK));
        return this;
    }

    public Henvendelse plukker() {
        this.logg.add(new HenvendelseLogg(this, LoggType.PLUKKET));
        return this;
    }

    public Long getId() {
        return id;
    }

    public Henvendelse ferdigstill() {
        this.status = HenvendelseStatus.FERDIG;
        this.logg.add(new HenvendelseLogg(this, LoggType.FERDIG));
        return this;
    }

    public List<HenvendelseLogg> getLogg() {
        return logg;
    }

    public HenvendelseStatus getStatus() {
        return status;
    }

    public Henvendelse feilet() {
        this.status = HenvendelseStatus.FEILET;
        this.logg.add(new HenvendelseLogg(this, LoggType.FEILET));
        return this;
    }

    @Override
    public String toString() {
        return "Henvendelse{" +
                "id=" + id +
                "status=" + status +
                ", opprettetTidspunkt=" + opprettetTidspunkt +
                ", versjon=" + versjon +
                '}';
    }
}
