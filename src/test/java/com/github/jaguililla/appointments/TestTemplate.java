package com.github.jaguililla.appointments;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Simplification of RestTemplate for testing. It holds the last received response to ease testing
 * flows.
 */
public final class TestTemplate {

    private static final Logger LOGGER = getLogger(TestTemplate.class);

    @SuppressWarnings("NullableProblems")
    private static final ResponseErrorHandler ERROR_HANDLER = new ResponseErrorHandler() {
        @Override
        public boolean hasError(ClientHttpResponse response) {
            return false;
        }

        @Override
        public void handleError(ClientHttpResponse response) {
            // Let RestTemplate return HTTP error codes without throwing exceptions
        }
    };

    private final ObjectMapper mapper;
    private final RestTemplate client;
    private ResponseEntity<String> lastResponse;

    public TestTemplate(final String rootUri) {

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
        lastResponse = client.getForEntity(path, String.class);
        LOGGER.debug("<- GET {}\n{}", path, lastResponse.getBody());
        return lastResponse;
    }

    @SuppressWarnings("UnusedReturnValue") // This is just a testing helper
    public ResponseEntity<String> delete(final String path) {
        LOGGER.debug("-> DELETE {}", path);
        lastResponse = client.exchange(path, HttpMethod.DELETE, null, String.class);
        LOGGER.debug("<- DELETE {}\n{}", path, lastResponse.getBody());
        return lastResponse;
    }

    @SuppressWarnings("UnusedReturnValue") // This is just a testing helper
    public ResponseEntity<String> post(final String path, final Object body) {
        LOGGER.debug("-> POST {}", path);
        lastResponse = client.postForEntity(path, body, String.class);
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
            final var message = "Error mapping response body to '" + type.getName() + "':\n" + body;
            throw new RuntimeException(message, e);
        }
    }
}
