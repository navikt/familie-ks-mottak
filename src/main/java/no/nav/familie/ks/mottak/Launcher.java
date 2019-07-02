package no.nav.familie.ks.mottak;

import no.nav.familie.ks.mottak.config.ApplicationConfig;
import no.nav.familie.ks.mottak.config.DelayedShutdownHook;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication(exclude = ErrorMvcAutoConfiguration.class)
public class Launcher {

    public static void main(String... args) {
        SpringApplication app = new SpringApplicationBuilder(ApplicationConfig.class)
                .web(WebApplicationType.SERVLET)
                .build();
        app.setRegisterShutdownHook(false);
        ConfigurableApplicationContext applicationContext = app.run(args);
        Runtime.getRuntime().addShutdownHook(new DelayedShutdownHook(applicationContext));
    }

}
