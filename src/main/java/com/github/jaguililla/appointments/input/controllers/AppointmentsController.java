package com.github.jaguililla.appointments.input.controllers;

import static org.slf4j.LoggerFactory.getLogger;

import com.github.jaguililla.appointments.domain.AppointmentsService;
import com.github.jaguililla.appointments.http.controllers.AppointmentsApi;
import com.github.jaguililla.appointments.http.controllers.messages.AppointmentRequest;
import com.github.jaguililla.appointments.http.controllers.messages.AppointmentResponse;
import com.github.jaguililla.appointments.http.controllers.messages.IdResponse;

import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
final class AppointmentsController extends BaseController implements AppointmentsApi {

    private static final Logger LOGGER = getLogger(AppointmentsController.class);

    private final AppointmentsService appointmentsService;

    AppointmentsController(final AppointmentsService appointmentsService) {
        this.appointmentsService = appointmentsService;
    }

    @Override
    public ResponseEntity<AppointmentResponse> createAppointment(
        final AppointmentRequest appointmentRequest
    ) {
        LOGGER.info("Creating appointment: {}", appointmentRequest);

        final var appointment = AppointmentsMapper.appointment(appointmentRequest);
        final var users = appointmentRequest.getUsers();

        final var createdAppointment = appointmentsService.create(appointment, users);

        final var responseAppointment = AppointmentsMapper.appointmentResponse(createdAppointment);
        return ResponseEntity.ofNullable(responseAppointment);
    }

    @Override
    public ResponseEntity<IdResponse> deleteAppointment(final String id) {
        LOGGER.info("Deleting appointment with id: {}", id);

        final var uuid = UUID.fromString(id);

        final var delete = appointmentsService.delete(uuid);

        return ResponseEntity.ofNullable(delete? new IdResponse(uuid) : null);
    }

    @Override
    public ResponseEntity<AppointmentResponse> readAppointment(final String id) {
        LOGGER.info("Reading appointment with id: {}", id);

        final var uuid = UUID.fromString(id);

        final var appointment = appointmentsService.get(uuid);

        final var response = AppointmentsMapper.appointmentResponse(appointment);
        return ResponseEntity.ofNullable(response);
    }

    @Override
    public ResponseEntity<List<AppointmentResponse>> readAppointments() {
        LOGGER.info("Reading all appointments");

        final var appointments = appointmentsService.getAll();

        final var responses = AppointmentsMapper.appointmentResponses(appointments);
        return ResponseEntity.ofNullable(responses);
    }
}
