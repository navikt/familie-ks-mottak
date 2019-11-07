package no.nav.familie.ks.mottak;

import no.nav.familie.ks.mottak.config.ApplicationConfig;
import no.nav.security.token.support.test.spring.TokenGeneratorConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

@SpringBootApplication(exclude = ErrorMvcAutoConfiguration.class)
@Import({ApplicationConfig.class, TokenGeneratorConfiguration.class})
@Profile("integrasjonstest")
public class UnitTestLauncher {

    public static void main(String... args) {
        SpringApplication app = new SpringApplicationBuilder(ApplicationConfig.class)
                .profiles("integrasjonstest")
                .build();
        app.run(args);
    }
}
