package com.github.jaguililla.appointments;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import com.github.jaguililla.appointments.http.controllers.messages.AppointmentRequest;
import com.github.jaguililla.appointments.http.controllers.messages.AppointmentResponse;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.kafka.KafkaContainer;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.UUID;

@SpringBootTest(
    classes = {Application.class},
    webEnvironment = RANDOM_PORT
)
class ApplicationIT {

    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");
    static KafkaContainer kafka = new KafkaContainer("apache/kafka:3.7.0");

    private final TestTemplate client;
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    @Autowired
    private ConsumerFactory<String, String> consumerFactory;

    ApplicationIT(@LocalServerPort final int portTest) {
        client = new TestTemplate("http://localhost:" + portTest);
    }

    @BeforeAll
    static void beforeAll() {
        postgres.start();
        kafka.start();
    }

    @AfterAll
    static void afterAll() {
        postgres.stop();
    }

    @DynamicPropertySource
    static void configureProperties(final DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @Test
    void specification_requests_work_as_expected() {
        client.get("/v3/api-docs");
        assertTrue(client.getResponseBody().contains("openapi"));
        assertEquals(200, client.getResponseStatus().value());
    }

    @Test
    void actuator_requests_work_as_expected() {
        client.get("/actuator/health");
        assertTrue(client.getResponseBody().contains("UP"));
        assertEquals(200, client.getResponseStatus().value());
    }

    @Test
    void existing_appointments_can_be_fetched() {
        client.get("/appointments");
        var response = client.getResponseBody(AppointmentResponse[].class);
        assertEquals(4, response.length);
        assertTrue(Arrays.stream(response).allMatch(it -> it.getUsers().size() == 4));
        assertEquals(200, client.getResponseStatus().value());
    }

    @Test
    void appointments_can_be_created_read_and_deleted() {
        try (var consumer = consumerFactory.createConsumer()) {
            consumer.subscribe(List.of("appointments"));
            for (var r : consumer.poll(Duration.ZERO)) {

            }
        }

        client.post("/appointments", new AppointmentRequest()
            .id(UUID.randomUUID())
            .startTimestamp(LocalDateTime.now())
            .endTimestamp(LocalDateTime.now())
        );
        var response = client.getResponseBody(AppointmentResponse.class);
        assertEquals(200, client.getResponseStatus().value());
        var creationMessage = requireNonNull(kafkaTemplate.receive("appointments", 0, 0));
        assertTrue(creationMessage.value().startsWith("Appointment created at"));
        client.get("/appointments/" + response.getId());
        assertEquals(200, client.getResponseStatus().value());
        client.delete("/appointments/" + response.getId());
        assertEquals(200, client.getResponseStatus().value());
        var deletionMessage = requireNonNull(kafkaTemplate.receive("appointments", 0, 1));
        assertTrue(deletionMessage.value().startsWith("Appointment deleted at"));
        client.delete("/appointments/" + response.getId());
        assertEquals(404, client.getResponseStatus().value());
    }
}
