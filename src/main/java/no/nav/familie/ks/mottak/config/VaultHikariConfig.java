package no.nav.familie.ks.mottak.config;

import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.vault.config.databases.VaultDatabaseProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.vault.core.lease.LeaseEndpoints;
import org.springframework.vault.core.lease.SecretLeaseContainer;
import org.springframework.vault.core.lease.domain.RequestedSecret;
import org.springframework.vault.core.lease.event.SecretLeaseCreatedEvent;

import java.util.Map;

import static org.springframework.vault.core.lease.domain.RequestedSecret.rotating;

@Configuration
@ConditionalOnProperty(value = "spring.cloud.vault.database.enabled")
public class VaultHikariConfig implements InitializingBean {
    private static final Logger LOGGER = LoggerFactory.getLogger(VaultHikariConfig.class);

    private final SecretLeaseContainer container;
    private final HikariDataSource hikariDataSource;
    private final VaultDatabaseProperties props;

    public VaultHikariConfig(SecretLeaseContainer container,
                             HikariDataSource hikariDataSource,
                             VaultDatabaseProperties props) {
        this.container = container;
        this.hikariDataSource = hikariDataSource;
        this.props = props;
    }

    @Override
    public void afterPropertiesSet() {

        RequestedSecret secret = rotating(props.getBackend() + "/creds/" + props.getRole());
        container.addLeaseListener(leaseEvent -> {
            if (leaseEvent.getSource() == secret && leaseEvent instanceof SecretLeaseCreatedEvent) {
                LOGGER.info("Rotating creds for path: " + leaseEvent.getSource().getPath());
                SecretLeaseCreatedEvent slce = (SecretLeaseCreatedEvent) leaseEvent;
                String username = slce.getSecrets().get("username").toString();
                String password = slce.getSecrets().get("password").toString();
                hikariDataSource.setUsername(username);
                hikariDataSource.setPassword(password);
                hikariDataSource.getHikariConfigMXBean().setUsername(username);
                hikariDataSource.getHikariConfigMXBean().setPassword(password);
                hikariDataSource.getHikariPoolMXBean().softEvictConnections();
            }
        });
        container.addRequestedSecret(secret);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [container=" + container + ", hikariDataSource=" + hikariDataSource + ", props=" + props + "]";
    }
}

