package com.github.jaguililla.appointments.domain;

import static com.github.jaguililla.appointments.domain.Event.CREATED;
import static com.github.jaguililla.appointments.domain.Event.DELETED;
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNullElse;

import com.github.jaguililla.appointments.domain.model.Appointment;
import com.github.jaguililla.appointments.domain.model.User;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class AppointmentsService {
    private final AppointmentsRepository appointmentsRepository;
    private final UsersRepository usersRepository;
    private final AppointmentsNotifier appointmentsNotifier;

    public AppointmentsService(
        final AppointmentsRepository appointmentsRepository,
        final UsersRepository usersRepository,
        final AppointmentsNotifier appointmentsNotifier
    ) {
        this.appointmentsRepository = appointmentsRepository;
        this.usersRepository = usersRepository;
        this.appointmentsNotifier = appointmentsNotifier;
    }

    public Appointment create(final Appointment appointment, final Set<UUID> userIds) {
        final var users = usersRepository.get(new HashSet<>(requireNonNullElse(userIds, emptyList())));
        final var fullAppointment = appointment.users(users.stream().toList());
        final var created = appointmentsRepository.insert(fullAppointment);

        if (created)
            appointmentsNotifier.notify(CREATED, fullAppointment);
        else
            throw new IllegalStateException("Error creating appointment");

        return fullAppointment;
    }

    public User create(final User appointment) {
        usersRepository.insert(appointment);
        return appointment;
    }

    public boolean delete(final UUID id) {
        final var appointment = appointmentsRepository.get(id);
        if (appointment == null)
            return false;

        final var deleted = appointmentsRepository.delete(id);

        if (deleted)
            appointmentsNotifier.notify(DELETED, appointment);

        return deleted;
    }

    public Appointment get(final UUID id) {
        return appointmentsRepository.get(id);
    }

    public List<Appointment> getAll() {
        return appointmentsRepository.getAll();
    }
}
