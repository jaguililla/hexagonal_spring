package com.github.jaguililla.appointments;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

import io.gatling.javaapi.core.*;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

public class GatlingSimulation extends Simulation {

    private ChainBuilder appointmentsCrud = exec(
        http("Create")
            .post("/appointments")
            .header("Content-Type", "application/json")
            .body(StringBody("""
                {
                  "id": "#{id}",
                  "startTimestamp": "2024-09-28T21:28:00",
                  "endTimestamp": "2024-09-28T21:28:00"
                }
                """
            ))
            .check(status().is(201))
            .check(jmesPath("id").saveAs("id")),
        pause(1),
        http("Read")
            .get("/appointments/#{id}")
            .check(status().is(200)),
        pause(1),
        http("Delete")
            .delete("/appointments/#{id}")
            .check(status().is(200))
    );

    {
        var baseUrl = "http://localhost:18080";
        var httpProtocol = http.baseUrl(baseUrl);
        var feeder = Stream
            .generate(UUID::randomUUID)
            .map(UUID::toString)
            .<Map<String, Object>>map(it -> Map.of("id", it))
            .iterator();

        var users = scenario("Appointments").feed(feeder).exec(appointmentsCrud);

        setUp(users.injectOpen(rampUsers(10).during(10))).protocols(httpProtocol);
    }
}
