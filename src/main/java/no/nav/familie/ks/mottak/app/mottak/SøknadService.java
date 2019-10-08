package no.nav.familie.ks.mottak.app.mottak;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.nav.familie.http.client.HttpClientUtil;
import no.nav.familie.http.client.HttpRequestUtil;
import no.nav.familie.http.sts.StsRestClient;
import no.nav.familie.ks.mottak.app.domene.Soknad;
import no.nav.familie.ks.mottak.app.domene.SøknadRepository;
import no.nav.familie.ks.mottak.app.domene.Vedlegg;
import no.nav.familie.ks.mottak.app.task.JournalførSøknadTask;
import no.nav.familie.ks.mottak.app.task.SendSøknadTilSakTask;
import no.nav.familie.ks.kontrakter.dokarkiv.api.*;
import no.nav.familie.prosessering.domene.Task;
import no.nav.familie.prosessering.domene.TaskRepository;
import org.eclipse.jetty.http.HttpHeader;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SøknadService {

    private static final Logger LOG = LoggerFactory.getLogger(SøknadService.class);
    private static final Logger SECURELOG = LoggerFactory.getLogger("secureLogger");

    private final StsRestClient stsRestClient;
    private final URI sakServiceUri;
    private final URI oppslagServiceUri;
    private final HttpClient client;
    private final SøknadRepository søknadRepository;
    private final TaskRepository taskRepository;
    private final ObjectMapper objectMapper;

    private boolean skalJournalføreSelv = false;

    public SøknadService(@Value("${FAMILIE_KS_SAK_API_URL}") URI sakServiceUri,
                         @Value("${FAMILIE_KS_OPPSLAG_API_URL}") URI oppslagServiceUri,
                         StsRestClient stsRestClient, SøknadRepository søknadRepository,
                         TaskRepository taskRepository, ObjectMapper objectMapper) {
        this.client = HttpClientUtil.create();
        this.sakServiceUri = URI.create(sakServiceUri + "/mottak/dokument");
        this.oppslagServiceUri = URI.create(oppslagServiceUri + "/api/arkiv");
        this.stsRestClient = stsRestClient;
        this.søknadRepository = søknadRepository;
        this.taskRepository = taskRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void lagreSoknadOgLagTask(SøknadDto søknadDto) {
        Soknad soknad = new Soknad();
        soknad.setSoknadJson(søknadDto.getSoknad());
        List<Vedlegg> vedlegg = søknadDto.getVedlegg().stream().map(vedleggDto -> {
            var v = new Vedlegg();
            v.setSoknad(soknad);
            v.setData(vedleggDto.getData());
            v.setFilnavn(vedleggDto.getTittel());
            return v;
        }).collect(Collectors.toList());
        soknad.setVedlegg(vedlegg);
        soknad.setFnr(søknadDto.getFnr());

        søknadRepository.save(soknad);

        final Task task;
        if (skalJournalføreSelv) {
            task = Task.nyTask(JournalførSøknadTask.JOURNALFØR_SØKNAD, soknad.getId().toString());
        } else {
            task = Task.nyTask(SendSøknadTilSakTask.SEND_SØKNAD_TIL_SAK, soknad.getId().toString());
        }

        taskRepository.save(task);
    }

    public void sendTilSak(String payload) {
        String søknadJson;
        String saksnummer = null;
        try {
            Soknad søknad = søknadRepository.findById(Long.valueOf(payload)).orElse(null);
            søknadJson = søknad != null ? søknad.getSoknadJson() : "";
            saksnummer = søknad != null ? søknad.getSaksnummer() : null;
        } catch (NumberFormatException e) {
            søknadJson = payload;
        }

        if (saksnummer == null) { //TODO slettes når vi har tatt over journalføring
            LOG.info("Genererer saksnummer for å støtte journalføring gjennom dokmot");
            saksnummer = Long.toString(System.currentTimeMillis());
        }

        byte[] sendTilSakRequest;
        try {
            sendTilSakRequest = objectMapper.writeValueAsBytes(new SendTilSakDto(søknadJson, saksnummer));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Kan ikke konvertere søknad til request for " + payload);
        }

        HttpRequest request = HttpRequestUtil.createRequest("Bearer " + stsRestClient.getSystemOIDCToken())
            .header(HttpHeader.CONTENT_TYPE.asString(), "application/json")
            .POST(HttpRequest.BodyPublishers.ofByteArray(sendTilSakRequest))
            .uri(sakServiceUri)
            .build();
        LOG.info("Sender søknad til {}", sakServiceUri);

        try {
            HttpResponse response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != HttpStatus.OK.value()) {
                LOG.warn("Innsending til sak feilet. Responskode: {}. Feilmelding: {}", response.statusCode(), response.body());

                throw new IllegalStateException("Innsending til sak feilet.");
            }
        } catch (IOException | InterruptedException e) {
            throw new IllegalStateException("Innsending til sak feilet.", e);
        }
    }

    public void journalførSøknad(String payload) {
        try {
            Soknad søknad = søknadRepository.findById(Long.valueOf(payload)).orElse(null);
            List<Dokument> dokumenter = new ArrayList<>();
            dokumenter.add(dokumentType(søknad));
            søknad.getVedlegg().forEach(vedlegg -> dokumenter.add(dokumentType(vedlegg)));

            var arkiverDokumentRequest = new ArkiverDokumentRequest(søknad.getFnr(), false, dokumenter);
            String journalpostID = sendTilOppslag(arkiverDokumentRequest).getJournalpostId();
            søknad.setJournalpostID(journalpostID);
            søknadRepository.save(søknad);

        } catch (NumberFormatException e) {
            throw new IllegalStateException();
        }
    }

    private ArkiverDokumentResponse sendTilOppslag(ArkiverDokumentRequest arkiverDokumentRequest) {
        String payload = ArkiverDokumentRequestKt.toJson(arkiverDokumentRequest);
        HttpRequest request = HttpRequestUtil.createRequest("Bearer " + stsRestClient.getSystemOIDCToken())
            .header(HttpHeader.CONTENT_TYPE.asString(), "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(payload))
            .uri(oppslagServiceUri)
            .build();
        LOG.info("Sender søknad til " + oppslagServiceUri);
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != HttpStatus.OK.value()) {
                SECURELOG.warn("Innsending til dokarkiv feilet. Responskode: {}. Feilmelding: {}",
                    response.statusCode(), response.headers().firstValue("message"));
                throw new IllegalStateException("Innsending til dokarkiv feilet.");
            } else {
                return ArkiverDokumentResponseKt.toArkiverDokumentResponse(response.body());
            }
        } catch (IOException | InterruptedException e) {
            SECURELOG.warn("Ukjent feil ved tjenestekall mot '" + oppslagServiceUri + "'. " + e.getMessage());
            throw new IllegalStateException("Innsending til dokarkiv feilet.", e);
        }
    }

    @NotNull
    private Dokument dokumentType(Vedlegg vedlegg) {
        return new Dokument(vedlegg.getData(), FilType.PDFA, vedlegg.getFilnavn(), DokumentType.KONTANTSTØTTE_SØKNAD_VEDLEGG);
    }

    @NotNull
    private Dokument dokumentType(Soknad søknad) {
        return new Dokument(søknad.getSoknadJson().getBytes(), FilType.JSON, "soknad", DokumentType.KONTANTSTØTTE_SØKNAD);
    }
}
