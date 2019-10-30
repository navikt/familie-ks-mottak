package no.nav.familie.ks.mottak.app.domene;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "SOKNAD")
public class Soknad {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "soknad_seq")
    @SequenceGenerator(name = "soknad_seq")
    private Long id;

    @Column(name = "soknad_json", updatable = false, columnDefinition = "text")
    private String soknadJson;

    @Column(name = "journalpost_id")
    private String journalpostID;

    @Column(name = "saksnummer")
    private String saksnummer;

    @Column(name = "fnr")
    private String fnr;

    @OneToMany(mappedBy = "soknad", fetch = FetchType.EAGER, cascade = CascadeType.ALL,orphanRemoval = true)
    @OrderBy("id asc")
    private List<Vedlegg> vedlegg = new ArrayList<>();


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSoknadJson() {
        return soknadJson;
    }

    public void setSoknadJson(String soknadJson) {
        this.soknadJson = soknadJson;
    }

    public String getJournalpostID() {
        return journalpostID;
    }

    public void setJournalpostID(String journalpostID) {
        this.journalpostID = journalpostID;
    }

    public String getSaksnummer() {
        return saksnummer;
    }

    public void setSaksnummer(String saksnummer) {
        this.saksnummer = saksnummer;
    }

    public List<Vedlegg> getVedlegg() {
        return vedlegg;
    }

    public void setVedlegg(List<Vedlegg> vedlegg) {
        this.vedlegg = vedlegg;
    }

    public String getFnr() {
        return fnr;
    }

    public void setFnr(String fnr) {
        this.fnr = fnr;
    }
}
