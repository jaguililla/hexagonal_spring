package com.github.jaguililla.appointments.input.controllers;

import static org.slf4j.LoggerFactory.getLogger;

import com.github.jaguililla.appointments.domain.AppointmentsService;
import com.github.jaguililla.appointments.http.controllers.UsersApi;
import com.github.jaguililla.appointments.http.controllers.messages.UserRequest;
import com.github.jaguililla.appointments.http.controllers.messages.UserResponse;
import org.slf4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
final class UsersController extends BaseController implements UsersApi {

    private static final Logger LOGGER = getLogger(UsersController.class);

    private final AppointmentsService appointmentsService;

    UsersController(final AppointmentsService appointmentsService) {
        this.appointmentsService = appointmentsService;
    }

    @Override
    public ResponseEntity<UserResponse> createUser(final UserRequest userRequest) {
        LOGGER.info("Creating user: {}", userRequest);

        final var user = UsersMapper.user(userRequest);

        final var createdUser = appointmentsService.create(user);

        final var responseUser = UsersMapper.userResponse(createdUser);
        return ResponseEntity.status(201).body(responseUser);
    }
}
