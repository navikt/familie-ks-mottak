package no.nav.familie.ks.mottak.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import no.nav.familie.ks.mottak.api.MottakController;
import no.nav.security.oidc.jaxrs.OidcContainerRequestFilter;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.stereotype.Component;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.ext.ContextResolver;

@Component
@ApplicationPath("/")
public class RestConfiguration extends ResourceConfig  {

    public RestConfiguration() {
        register(JacksonFeature.class);
        register(objectMapperContextResolver());
        //Filter
        register(OidcContainerRequestFilter.class);

        // Controllers
        register(MottakController.class);
    }

    public static ContextResolver<ObjectMapper> objectMapperContextResolver() {
        return new ContextResolver<ObjectMapper>() {
            @Override
            public ObjectMapper getContext(Class<?> type) {
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.registerModule(new JavaTimeModule());
                return objectMapper;
            }
        };
    }

}
