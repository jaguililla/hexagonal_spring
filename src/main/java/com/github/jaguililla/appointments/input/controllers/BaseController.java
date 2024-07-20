package com.github.jaguililla.appointments.input.controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;

import static org.slf4j.LoggerFactory.getLogger;

@Controller
class BaseController {

    private static final Logger LOGGER = getLogger(BaseController.class);

    @ExceptionHandler(Exception.class)
    ResponseEntity<String> handleException(
        final HttpServletRequest request, final Exception exception
    ) {
        final var url = request.getRequestURL();
        final var message = exception.getMessage();
        final var resultMessage = "Request: %s raised %s".formatted(url, message);

        LOGGER.error(resultMessage, exception);

        return ResponseEntity.status(500).body(resultMessage);
    }
}
