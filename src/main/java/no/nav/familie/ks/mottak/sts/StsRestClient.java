package no.nav.familie.ks.mottak.sts;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.concurrent.ExecutionException;

import static java.time.LocalDate.now;

@Component
public class StsRestClient {
    private ObjectMapper mapper = new ObjectMapper();

    private HttpClient client;
    private URI stsUrl;
    private String stsUsername;
    private String stsPassword;

    private AccessTokenResponse cachedToken;

    public StsRestClient (@Value("${STS_URL}") URI stsUrl, @Value("${CREDENTIAL_USERNAME}") String stsUsername, @Value("${CREDENTIAL_PASSWORD}") String stsPassword) {
        this.client = HttpClient.newHttpClient();
        this.stsUrl = URI.create(stsUrl + "/rest/v1/sts/token?grant_type=client_credentials&scope=openid");
        this.stsUsername = stsUsername;
        this.stsPassword = stsPassword;
    }

    private boolean hasTokenExpired() {
        return Instant.ofEpochMilli(cachedToken.getExpires_in())
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                    .minus(10, ChronoUnit.MINUTES)
                    .isAfter(now());
    }

    public String getSystemOIDCToken() {
        if (!hasTokenExpired()) {
            return cachedToken.getAccess_token();
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(stsUrl)
                .header("Authorization", basicAuth(stsUsername, stsPassword))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(30))
                .build();

        AccessTokenResponse accessTokenResponse;
        try {
            accessTokenResponse = client
                .sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(it -> {
                    try {
                        return mapper.readValue(it, AccessTokenResponse.class);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    return null;
                }).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return "";
        }

        if (accessTokenResponse != null) {
            this.cachedToken = accessTokenResponse;
            return accessTokenResponse.getAccess_token();
        } else {
            return "";
        }
    }

    private static String basicAuth(String username, String password) {
        return "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
    }
}
