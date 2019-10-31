package no.nav.familie.ks.mottak.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.nav.familie.http.azure.AccessTokenClient;
import no.nav.familie.http.sts.StsRestClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.time.Duration;

@Configuration
public class IntegrasjonConfig {

    @Bean
    @Autowired
    public StsRestClient stsRestClient(ObjectMapper objectMapper,
                                       @Value("${STS_URL}") URI stsUrl,
                                       @Value("${CREDENTIAL_USERNAME}") String stsUsername,
                                       @Value("${CREDENTIAL_PASSWORD}") String stsPassword) {

        final var stsFullUrl = URI.create(stsUrl + "/rest/v1/sts/token?grant_type=client_credentials&scope=openid");

        return new StsRestClient(objectMapper, stsFullUrl, stsUsername, stsPassword);
    }

    @Bean
    @Autowired
    public AccessTokenClient accessTokenClient(RestTemplate restTemplateMedProxy,
                                               @Value("${AZURE_TOKEN_URL}") String aadAccessTokenUrl,
                                               @Value("${CLIENT_ID}") String clientId,
                                               @Value("${CLIENT_SECRET}") String clientSecret) {
        final var aadAccessTokenUri = URI.create(aadAccessTokenUrl);

        return new AccessTokenClient(aadAccessTokenUri, clientId, clientSecret, restTemplateMedProxy);
    }
}
