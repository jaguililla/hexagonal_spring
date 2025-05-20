package com.github.jaguililla.appointments.it;

import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import java.net.InetSocketAddress;
import java.util.Map;

import static java.net.HttpURLConnection.HTTP_OK;
import static java.nio.charset.StandardCharsets.UTF_8;

public class OpenIdMock {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenIdMock.class);
    private static final String JSON = "application/json; charset=" + UTF_8.name();

    static final String CONTEXT = "/realms/appointments";
    static final int PORT = 9876;

    private HttpServer httpServer;

    final JwtTokenManager jwtTokenManager;

    public OpenIdMock() {
        try {
            jwtTokenManager = new JwtTokenManager();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void start() {
        try {
            if (httpServer != null)
                return;

            httpServer = HttpServer.create(new InetSocketAddress(PORT), 0);

            var resources = Map.of(
                ".well-known/openid-configuration", "/jwt/openid-configuration.json",
                "protocol/openid-connect/certs", "/jwt/certs.json"
            );

            httpServer.createContext(CONTEXT, exchange -> {
                var uriPath = exchange.getRequestURI().getPath();
                var path = uriPath.replaceFirst("^" + CONTEXT + "/?", "");
                var host = exchange.getRequestHeaders().getFirst("Host");
                var binding = "http://" + host + CONTEXT;

                var responsePath = resources.get(path);
                var responseTemplate = new ClassPathResource(responsePath).getContentAsByteArray();
                var responseText = new String(responseTemplate).replaceAll("<binding>", binding);
                var response = responseText.getBytes();

                exchange.getResponseHeaders().set("Content-Type", JSON);
                exchange.sendResponseHeaders(HTTP_OK, response.length);
                exchange.getResponseBody().write(response);
                exchange.close();
            });

            httpServer.start();
            LOGGER.info("OpenID configuration server started");
        }
        catch (Exception e) {
            LOGGER.error("Error starting OpenID configuration server mock", e);
        }
    }

    public void stop() {
        if (httpServer != null) {
            httpServer.stop(0);
            httpServer = null;
        }
    }
}
