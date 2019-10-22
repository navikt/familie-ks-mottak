package no.nav.familie.ks.mottak.util;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Objects;

public final class LocalSts {
    public static String getSystemOIDCToken(RestTemplate restTemplate) {
        return Objects.requireNonNull(restTemplate.exchange(URI.create("http://localhost:8085/local/sts"), HttpMethod.GET, HttpEntity.EMPTY, JsonNode.class).getBody()).get("access_token").asText();
    }
}
