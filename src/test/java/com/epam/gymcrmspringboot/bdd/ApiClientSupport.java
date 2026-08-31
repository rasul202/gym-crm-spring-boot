package com.epam.gymcrmspringboot.bdd;

import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Component
public class ApiClientSupport {

    private final Environment environment;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public ApiClientSupport(Environment environment, ObjectMapper objectMapper) {
        this.environment = environment;
        this.objectMapper = objectMapper;
    }

    public ResponseEntity<String> post(String path, Object requestBody) {
        return exchange(path, requestBody, null);
    }

    public ResponseEntity<String> postWithBearer(String path, Object requestBody, String token) {
        return exchange(path, requestBody, token);
    }

    private ResponseEntity<String> exchange(String path, Object requestBody, String token) {
        String port = environment.getProperty("local.server.port");
        if (port == null || port.isBlank()) {
            throw new IllegalStateException("local.server.port is not available");
        }

        try {
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:" + port + path))
                    .header("Content-Type", MediaType.APPLICATION_JSON_VALUE);

            if (token != null && !token.isBlank()) {
                requestBuilder.header("Authorization", "Bearer " + token);
            }

            String payload = requestBody == null ? "" : objectMapper.writeValueAsString(requestBody);
            HttpRequest request = requestBuilder.POST(HttpRequest.BodyPublishers.ofString(payload)).build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            String responseBody = response.body();
            if (responseBody != null && responseBody.isBlank()) {
                responseBody = null;
            }

            return new ResponseEntity<>(responseBody, HttpStatusCode.valueOf(response.statusCode()));
        } catch (Exception ex) {
            throw new IllegalStateException("HTTP request failed for path " + path, ex);
        }
    }
}
