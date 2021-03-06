package no.nav.familie.ks.mottak.config;

import no.nav.familie.kontrakter.felles.Ressurs;
import no.nav.familie.kontrakter.felles.arkivering.ArkiverDokumentRequest;
import no.nav.familie.ks.mottak.app.mottak.SendTilSakDto;
import no.nav.familie.log.NavHttpHeaders;
import no.nav.familie.log.mdc.MDCConstants;
import no.nav.security.token.support.client.core.ClientProperties;
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenResponse;
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService;
import no.nav.security.token.support.client.spring.ClientConfigurationProperties;
import org.slf4j.MDC;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Optional;

public class BaseService {

    private final RestTemplate restTemplate;
    private final ClientProperties clientProperties;
    private final OAuth2AccessTokenService oAuth2AccessTokenService;

    public BaseService(String clientConfigKey, RestTemplateBuilder restTemplateBuilder,
                             ClientConfigurationProperties clientConfigurationProperties,
                             OAuth2AccessTokenService oAuth2AccessTokenService) {

        this.clientProperties = Optional.ofNullable(
            clientConfigurationProperties.getRegistration().get(clientConfigKey))
                                        .orElseThrow(() -> new RuntimeException("could not find oauth2 client config for key="+ clientConfigKey));
        this.restTemplate = restTemplateBuilder
            .interceptors(bearerTokenInterceptor())
            .build();
        this.oAuth2AccessTokenService = oAuth2AccessTokenService;
    }

    protected <T> ResponseEntity<T> getRequest(URI uri, Class<T> responseType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add(NavHttpHeaders.NAV_CALL_ID.asString(), MDC.get(MDCConstants.MDC_CALL_ID));

        return restTemplate.exchange(uri, HttpMethod.GET, new HttpEntity<>(headers), responseType);
    }

    protected <T> ResponseEntity<T> postRequest(URI uri,
                                                ArkiverDokumentRequest requestBody,
                                                Class<T> responseType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add(NavHttpHeaders.NAV_CALL_ID.asString(), MDC.get(MDCConstants.MDC_CALL_ID));

        return restTemplate.exchange(uri, HttpMethod.POST, new HttpEntity<>(requestBody, headers), responseType);
    }

    protected ResponseEntity<Ressurs> postRequest(URI uri, SendTilSakDto requestBody) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add(NavHttpHeaders.NAV_CALL_ID.asString(), MDC.get(MDCConstants.MDC_CALL_ID));

        return restTemplate.exchange(uri, HttpMethod.POST, new HttpEntity<>(requestBody, headers), Ressurs.class);
    }

    private ClientHttpRequestInterceptor bearerTokenInterceptor(){
        return (request, body, execution) -> {
            OAuth2AccessTokenResponse response =
                oAuth2AccessTokenService.getAccessToken(clientProperties);
            request.getHeaders().setBearerAuth(response.getAccessToken());
            return execution.execute(request, body);
        };
    }
}
