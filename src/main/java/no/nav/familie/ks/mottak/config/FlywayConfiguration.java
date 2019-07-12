package no.nav.familie.ks.mottak.config;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.flyway.FlywayConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty("spring.flyway.enabled")
public class FlywayConfiguration {

    private static final Logger log = LoggerFactory.getLogger(FlywayConfiguration.class);

    @Bean
    public FlywayConfigurationCustomizer flywayConfig(@Value("${spring.cloud.vault.database.role}") String role) {
        log.info(String.format("SET ROLE \"%s\"", role));
        return c -> c.initSql(String.format("SET ROLE \"%s\"", role));
    }
}
