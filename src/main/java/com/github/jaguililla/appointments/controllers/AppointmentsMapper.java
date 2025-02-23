package com.github.jaguililla.appointments.controllers;

import static java.util.stream.Collectors.toSet;

import com.github.jaguililla.appointments.domain.model.Appointment;
import com.github.jaguililla.appointments.domain.model.User;
import com.github.jaguililla.appointments.http.controllers.messages.AppointmentRequest;
import com.github.jaguililla.appointments.http.controllers.messages.AppointmentResponse;
import com.github.jaguililla.appointments.http.controllers.messages.UserResponse;
import java.util.List;
import java.util.Set;

interface AppointmentsMapper {

    static Appointment appointment(AppointmentRequest appointmentRequest) {
        final var id = appointmentRequest.getId();
        final var startTimestamp = appointmentRequest.getStartTimestamp();
        final var endTimestamp = appointmentRequest.getEndTimestamp();
        return new Appointment(id, startTimestamp, endTimestamp);
    }

    static Set<UserResponse> userResponses(Appointment appointment) {
        return appointment.users().stream().map(AppointmentsMapper::userResponse).collect(toSet());
    }

    static List<AppointmentResponse> appointmentResponses(List<Appointment> appointments) {
        return appointments.stream().map(AppointmentsMapper::appointmentResponse).toList();
    }

    static AppointmentResponse appointmentResponse(Appointment appointment) {
        return new AppointmentResponse()
            .id(appointment.id())
            .startTimestamp(appointment.start())
            .endTimestamp(appointment.end())
            .users(userResponses(appointment));
    }

    static UserResponse userResponse(User u) {
        return new UserResponse().id(u.id()).name(u.name());
    }
}
