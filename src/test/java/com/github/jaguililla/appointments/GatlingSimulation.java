package com.github.jaguililla.appointments;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;
import static java.lang.Integer.parseInt;
import static java.lang.Long.parseLong;
import static java.lang.System.getProperty;

import io.gatling.javaapi.core.*;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

public class GatlingSimulation extends Simulation {

    private static final long SCENARIO_SECONDS = parseLong(getProperty("scenario.seconds", "30"));
    private static final int MAX_USERS = parseInt(getProperty("max.users", "20"));
    private static final int USERS_RAMP_SECONDS = parseInt(getProperty("users.ramp.seconds", "5"));
    private static final String BASE_URL = getProperty("base.url", "http://localhost:18080");

    private final ChainBuilder appointmentsList = during(SCENARIO_SECONDS).on(
        http("List")
            .get("/appointments")
            .check(status().is(200))
    );

    private final ChainBuilder appointmentsCrud = during(SCENARIO_SECONDS).on(
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
        http("Read")
            .get("/appointments/#{id}")
            .check(status().is(200)),
        http("Delete")
            .delete("/appointments/#{id}")
            .check(status().is(200))
    );

    {
        var httpProtocol = http.baseUrl(BASE_URL);
        var feeder = Stream
            .generate(UUID::randomUUID)
            .map(UUID::toString)
            .<Map<String, Object>>map(it -> Map.of("id", it))
            .iterator();

        var crud = scenario("Appointments CRUD").feed(feeder).exec(appointmentsCrud);
        var list = scenario("Appointments List").exec(appointmentsList);

        setUp(
            crud.injectOpen(rampUsers(MAX_USERS).during(USERS_RAMP_SECONDS)),
            list.injectOpen(rampUsers(MAX_USERS).during(USERS_RAMP_SECONDS))
        )
        .protocols(httpProtocol);
    }
}
