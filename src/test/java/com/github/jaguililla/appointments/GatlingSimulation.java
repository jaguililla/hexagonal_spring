package com.github.jaguililla.appointments;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

import io.gatling.javaapi.core.*;

public class GatlingSimulation extends Simulation {

    private ChainBuilder appointmentsCrud = exec(
        http("Create").post("/appointments").check(status().is(201)),
        pause(1),
        http("Read").get("/appointments").check(status().is(200)),
        pause(1),
        http("Delete").delete("/appointments").check(status().is(200)),
        pause(1)
    );

    {
        var baseUrl = "http://localhost:18080";
        var httpProtocol = http.baseUrl(baseUrl);
        var users = scenario("Appointments").exec(appointmentsCrud);

        setUp(users.injectOpen(rampUsers(10).during(10))).protocols(httpProtocol);
    }
}
