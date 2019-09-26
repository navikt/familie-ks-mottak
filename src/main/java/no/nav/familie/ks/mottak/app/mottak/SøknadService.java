package no.nav.familie.ks.mottak.app.mottak;

import no.nav.familie.http.client.HttpClientUtil;
import no.nav.familie.http.client.HttpRequestUtil;
import no.nav.familie.http.sts.StsRestClient;
import no.nav.familie.ks.mottak.app.domene.Soknad;
import no.nav.familie.ks.mottak.app.domene.SøknadRepository;
import no.nav.familie.ks.mottak.app.domene.Vedlegg;
import no.nav.familie.ks.mottak.app.task.SendSøknadTilSakTask;
import no.nav.familie.prosessering.domene.Task;
import no.nav.familie.prosessering.domene.TaskRepository;
import org.eclipse.jetty.http.HttpHeader;
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
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SøknadService {

    private static final Logger LOG = LoggerFactory.getLogger(SøknadService.class);

    private final StsRestClient stsRestClient;
    private final URI sakServiceUri;
    private final HttpClient client;
    private final SøknadRepository søknadRepository;
    private final TaskRepository taskRepository;

    public SøknadService(@Value("${FAMILIE_KS_SAK_API_URL}") URI sakServiceUri, StsRestClient stsRestClient, SøknadRepository søknadRepository, TaskRepository taskRepository) {
        this.client = HttpClientUtil.create();
        this.sakServiceUri = URI.create(sakServiceUri + "/mottak/dokument");
        this.stsRestClient = stsRestClient;
        this.søknadRepository = søknadRepository;
        this.taskRepository = taskRepository;
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

        final var task = Task.nyTask(SendSøknadTilSakTask.SEND_SØKNAD_TIL_SAK, soknad.getId().toString());

        taskRepository.save(task);


    }

    public void sendTilSak(String søknadId) {
        Soknad søknad = søknadRepository.findById(Long.valueOf(søknadId)).orElse(null);
        String søknadJson = søknad != null ? søknad.getSoknadJson() : "";

        HttpRequest request = HttpRequestUtil.createRequest("Bearer " + stsRestClient.getSystemOIDCToken())
            .header(HttpHeader.CONTENT_TYPE.asString(), "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(søknadJson))
            .uri(sakServiceUri)
            .build();
        LOG.info("Sender søknad til " + sakServiceUri);

        HttpResponse response = null;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != HttpStatus.OK.value()) {
                LOG.warn("Innsending til sak feilet. Responskode: {}. Feilmelding: {}", response.statusCode(), response.body());

                throw new IllegalStateException("Innsending til sak feilet.");
            }
        } catch (IOException | InterruptedException e) {
            throw new IllegalStateException("Innsending til sak feilet.", e);
        }
    }
}
