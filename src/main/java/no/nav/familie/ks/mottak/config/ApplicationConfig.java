package no.nav.familie.ks.mottak.config;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.web.embedded.jetty.JettyServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@SpringBootConfiguration
@ComponentScan({ "no.nav.familie.ks.mottak" })
public class ApplicationConfig {

    @Bean
    ServletWebServerFactory servletWebServerFactory() {

        JettyServletWebServerFactory serverFactory = new JettyServletWebServerFactory();

        serverFactory.setPort(8082);

        return serverFactory;
    }
}
