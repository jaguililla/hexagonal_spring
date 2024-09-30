package com.github.jaguililla.appointments;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

import io.gatling.javaapi.core.*;

public class GatlingSimulation extends Simulation {

    private ChainBuilder appointmentsList = exec(
        http("List")
            .get("/appointments")
            .check(status().is(200))
    );

    private ChainBuilder appointmentsCrud = exec(
        http("Create")
            .post("/appointments")
            .header("Content-Type", "application/json")
            .body(StringBody("""
                {
                  "startTimestamp": "2024-09-28T21:28:00.419367341",
                  "endTimestamp": "2024-09-28T21:28:00.4193957"
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
        var crud = scenario("Appointments CRUD").exec(appointmentsCrud);
        var list = scenario("Appointments List").exec(appointmentsList);

        setUp(
            crud.injectOpen(rampUsers(10).during(10)),
            list.injectOpen(rampUsers(10).during(10))
        )
        .protocols(httpProtocol);
    }
}
