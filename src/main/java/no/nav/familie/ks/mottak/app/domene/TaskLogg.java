package no.nav.familie.ks.mottak.app.domene;

import javax.persistence.*;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.time.LocalDateTime;

@Entity
@Table(name = "HENVENDELSE_LOGG")
public class TaskLogg {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "henvendelse_logg_seq")
    @SequenceGenerator(name = "henvendelse_logg_seq")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "henvendelse_id")
    private Task task;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, updatable = false)
    private LoggType type;

    @Column(name = "node")
    private String node;

    @Lob
    @Column(name = "feilmelding", updatable = false, columnDefinition = "text")
    private String feilmelding = "";

    @Column(name = "opprettet_tid", nullable = false, updatable = false)
    private LocalDateTime opprettetTidspunkt; // NOSONAR

    TaskLogg() {
        String hostname = System.getenv("hostname");
        if (hostname == null || hostname.isEmpty()) {
            try {
                hostname = Inet4Address.getLocalHost().getHostName();
            } catch (UnknownHostException ignore) {
                hostname = "N/A";
            }
        }
        this.node = hostname;
    }

    public TaskLogg(Task task, LoggType type) {
        this();
        this.task = task;
        this.type = type;
    }

    public TaskLogg(Task task, LoggType type, String feilmelding) {
        this(task, type);
        this.feilmelding = feilmelding;
    }

    public String getFeilmelding() {
        return feilmelding;
    }

    public LoggType getType() {
        return type;
    }

    @PrePersist
    protected void onCreate() {
        this.opprettetTidspunkt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "TaskLogg{" +
            "id=" + id +
            ", type=" + type +
            ", node='" + node + '\'' +
            ", opprettetTidspunkt=" + opprettetTidspunkt +
            '}';
    }
}
