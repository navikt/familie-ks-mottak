package no.nav.familie.ks.mottak;

import no.nav.familie.ks.mottak.config.ApplicationConfig;
import no.nav.security.oidc.test.support.spring.TokenGeneratorConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.context.annotation.Import;


@SpringBootApplication(exclude = ErrorMvcAutoConfiguration.class)
@Import({ ApplicationConfig.class, TokenGeneratorConfiguration.class })
public class DevLauncher {

    public static void main(String... args) {
        SpringApplication.run(ApplicationConfig.class, args);
    }
}
