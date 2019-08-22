package no.nav.familie.ks.mottak.app.mottak.domene;

import javax.persistence.*;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.time.LocalDateTime;

@Entity
@Table(name = "HENVENDELSE_LOGG")
public class HenvendelseLogg {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "henvendelse_logg_id_seq")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "henvendelse_id")
    private Henvendelse henvendelse;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, updatable = false)
    private LoggType type;

    @Column(name = "node")
    private String node;

    @Column(name = "opprettet_tid", nullable = false, updatable = false)
    private LocalDateTime opprettetTidspunkt; // NOSONAR

    HenvendelseLogg() {
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

    public HenvendelseLogg(Henvendelse henvendelse, LoggType type) {
        this();
        this.henvendelse = henvendelse;
        this.type = type;
    }

    @PrePersist
    protected void onCreate() {
        this.opprettetTidspunkt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "HenvendelseLogg{" +
                "id=" + id +
                ", type=" + type +
                ", node='" + node + '\'' +
                ", opprettetTidspunkt=" + opprettetTidspunkt +
                '}';
    }
}
