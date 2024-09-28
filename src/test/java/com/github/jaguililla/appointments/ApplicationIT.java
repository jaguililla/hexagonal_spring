package com.github.jaguililla.appointments;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import com.github.jaguililla.appointments.http.controllers.messages.AppointmentRequest;
import com.github.jaguililla.appointments.http.controllers.messages.AppointmentResponse;
import java.time.Duration;
import java.util.List;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.kafka.core.ConsumerFactory;
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
@TestMethodOrder(OrderAnnotation.class)
class ApplicationIT {

    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");
    static KafkaContainer kafka = new KafkaContainer("apache/kafka:3.8.0");

    private final TestTemplate client;
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
    @Order(1)
    void specification_requests_work_as_expected() {
        client.get("/v3/api-docs");
        assertTrue(client.getResponseBody().contains("openapi"));
        assertEquals(200, client.getResponseStatus().value());
    }

    @Test
    @Order(2)
    void actuator_requests_work_as_expected() {
        client.get("/actuator/health");
        assertTrue(client.getResponseBody().contains("UP"));
        assertEquals(200, client.getResponseStatus().value());
    }

    @Test
    @Order(3)
    void existing_appointments_can_be_fetched() {
        client.get("/appointments");
        var response = client.getResponseBody(AppointmentResponse[].class);
        assertEquals(4, response.length);
        assertTrue(Arrays.stream(response).allMatch(it -> it.getUsers().size() == 4));
        assertEquals(200, client.getResponseStatus().value());
    }

    @Test
    @Order(4)
    void appointments_can_be_created_read_and_deleted() {
        client.post("/appointments", new AppointmentRequest()
            .id(UUID.randomUUID())
            .startTimestamp(LocalDateTime.now())
            .endTimestamp(LocalDateTime.now())
        );
        var response = client.getResponseBody(AppointmentResponse.class);
        assertEquals(201, client.getResponseStatus().value());
        assertTrue(getLastMessage().startsWith("Appointment created at"));
        client.get("/appointments/" + response.getId());
        assertEquals(200, client.getResponseStatus().value());
        client.delete("/appointments/" + response.getId());
        assertEquals(200, client.getResponseStatus().value());
        assertTrue(getLastMessage().startsWith("Appointment deleted at"));
        client.delete("/appointments/" + response.getId());
        assertEquals(404, client.getResponseStatus().value());
    }

    private String getLastMessage() {
        try (var consumer = consumerFactory.createConsumer()) {
            consumer.assign(List.of(new TopicPartition("appointments", 0)));
            var record = consumer.poll(Duration.ofMillis(250)).iterator().next().value();
            consumer.commitSync();
            return record;
        }
    }
}
