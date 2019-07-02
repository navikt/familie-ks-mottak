package no.nav.familie.ks.mottak.api;

import no.nav.familie.ks.mottak.config.RestConfiguration;
import no.nav.sbl.rest.ClientLogFilter;
import no.nav.security.oidc.jaxrs.OidcClientRequestFilter;
import org.glassfish.jersey.logging.LoggingFeature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.net.URI;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/")
@Component
public class MottakController {
    private URI sakServiceUri;

    private final Client client;

    @Autowired
    public MottakController(@Value("${SOKNAD_KONTANTSTOTTE_SAK_API_URL}") URI sakServiceUri) {
        this.client = ClientBuilder.newBuilder()
                .register(new ClientLogFilter(ClientLogFilter.ClientLogFilterConfig.builder()
                        .metricName("soknad-kontantstotte-mottak").build()))
                .register(OidcClientRequestFilter.class)
                .register(RestConfiguration.objectMapperContextResolver())
                .register(new LoggingFeature())
                .build();
        this.sakServiceUri = sakServiceUri;
    }

    @POST
    @Path("soknad")
    public Response mottaSoknad(@RequestBody String soknad) {
        return client.target(sakServiceUri)
                .path("behandling/start")
                .request()
                .buildPost(Entity.entity(soknad, APPLICATION_JSON))
                .invoke();
    }
}
