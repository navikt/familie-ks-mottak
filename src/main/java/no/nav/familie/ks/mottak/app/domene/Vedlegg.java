package no.nav.familie.ks.mottak.app.domene;

import javax.persistence.*;

@Entity
@Table(name = "VEDLEGG")
public class Vedlegg {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "vedlegg_seq")
    @SequenceGenerator(name = "vedlegg_seq")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "fk_soknad")
    private Soknad soknad;

    @Column(name="data")
    private byte[] data;

    @Column(name="filnavn")
    private String filnavn;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Soknad getSoknad() {
        return soknad;
    }

    public void setSoknad(Soknad soknad) {
        this.soknad = soknad;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public String getFilnavn() { return filnavn; }

    public void setFilnavn(String filnavn) { this.filnavn = filnavn; }
}
