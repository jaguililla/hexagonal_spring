package com.github.jaguililla.appointments.domain;

import com.github.jaguililla.appointments.domain.model.Appointment;

public interface AppointmentsNotifier {
    void notify(final Event event, final Appointment appointment);
}
