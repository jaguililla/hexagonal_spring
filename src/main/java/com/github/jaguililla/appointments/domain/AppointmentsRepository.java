package com.github.jaguililla.appointments.domain;

import com.github.jaguililla.appointments.domain.model.Appointment;
import java.util.List;
import java.util.UUID;

public interface AppointmentsRepository {
    boolean insert(final Appointment appointment);
    boolean delete(final UUID id);
    Appointment get(final UUID id);
    List<Appointment> getAll();
}
