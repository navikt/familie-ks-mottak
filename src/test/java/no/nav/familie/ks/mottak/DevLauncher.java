package no.nav.familie.ks.mottak;

import no.nav.familie.ks.mottak.config.ApplicationConfig;
import no.nav.security.token.support.test.spring.TokenGeneratorConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.EnableTransactionManagement;


@SpringBootApplication(exclude = ErrorMvcAutoConfiguration.class)
@EnableTransactionManagement
@AutoConfigureDataJpa
@Import({ApplicationConfig.class, TokenGeneratorConfiguration.class})
public class DevLauncher {

    public static void main(String... args) {
        new SpringApplicationBuilder(ApplicationConfig.class)
            //.profiles("postgresql")
            .profiles("integrasjonstest")
            .run(args);
    }
}
