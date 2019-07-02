package no.nav.familie.ks.mottak.config;

import no.nav.security.oidc.test.support.jersey.TestTokenGeneratorResource;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import javax.ws.rs.ApplicationPath;

@Primary
@Component
@ApplicationPath("/")
public class LocalDevelopmentResources extends RestConfiguration {

    public LocalDevelopmentResources() {
        register(TestTokenGeneratorResource.class);
    }
}
