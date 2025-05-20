package com.github.jaguililla.appointments;

import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jaguililla.appointments.it.JwtTokenManager;
import org.slf4j.Logger;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

/**
 * Simplification of RestTemplate for testing. It holds the last received response to ease testing
 * flows.
 */
public final class TestTemplate {

    private static final Logger LOGGER = getLogger(TestTemplate.class);
    private static final ResponseErrorHandler ERROR_HANDLER = ignore -> false;

    private final ObjectMapper mapper;
    private final RestTemplate client;
    private final String rootUri;
    private final JwtTokenManager tokenManager;
    private ResponseEntity<String> lastResponse;

    public TestTemplate(final String rootUri) {

        this.rootUri = rootUri;
        this.tokenManager = new JwtTokenManager();
        final var restTemplateBuilder = new RestTemplateBuilder();

        mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        client = restTemplateBuilder
            .rootUri(rootUri)
            .errorHandler(ERROR_HANDLER)
            .build();
    }

    @SuppressWarnings("UnusedReturnValue") // This is just a testing helper
    public ResponseEntity<String> get(final String path) {
        LOGGER.debug("-> GET {}", path);
        lastResponse = client.exchange(createRequest(GET, path), String.class);
        LOGGER.debug("<- GET {}\n{}", path, lastResponse.getBody());
        return lastResponse;
    }

    @SuppressWarnings("UnusedReturnValue") // This is just a testing helper
    public ResponseEntity<String> delete(final String path) {
        LOGGER.debug("-> DELETE {}", path);
        lastResponse = client.exchange(createRequest(DELETE, path), String.class);
        LOGGER.debug("<- DELETE {}\n{}", path, lastResponse.getBody());
        return lastResponse;
    }

    @SuppressWarnings("UnusedReturnValue") // This is just a testing helper
    public ResponseEntity<String> post(final String path, final Object body) {
        LOGGER.debug("-> POST {}", path);
        lastResponse = client.exchange(createRequest(POST, path, body), String.class);
        LOGGER.debug("<- POST {}\n{}", path, lastResponse.getBody());
        return lastResponse;
    }

    public HttpStatusCode getResponseStatus() {
        return lastResponse.getStatusCode();
    }

    public String getResponseBody() {
        return lastResponse.getBody();
    }

    public <T> T getResponseBody(final Class<T> type) {

        final var body = getResponseBody();

        try {
            return mapper.readValue(body, type);
        }
        catch (final JsonProcessingException e) {
            final var message = "Error mapping response body to '%s':\n%s"
                .formatted(type.getName(), body);
            throw new RuntimeException(message, e);
        }
    }

    private RequestEntity<Object> createRequest(HttpMethod method, String path) {
        return createRequest(method, path, null);
    }

    private RequestEntity<Object> createRequest(HttpMethod method, String path, Object body) {
        final var headers = new HttpHeaders();
        final var uri = URI.create(rootUri + path);
        final var request = new RequestEntity<>(body, headers, method, uri);
        final var token = tokenManager.createToken("http://localhost:9876/realms/appointments");

        headers.setBearerAuth(token);
        return request;
    }
}
