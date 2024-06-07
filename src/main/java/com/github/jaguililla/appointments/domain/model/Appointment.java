package com.github.jaguililla.appointments.domain.model;

import static com.github.jaguililla.appointments.domain.Checks.requireAfter;
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record Appointment(
    UUID id,
    LocalDateTime start,
    LocalDateTime end,
    List<User> users
) {
    public Appointment {
        requireNonNull(id, "id can not be null");
        requireNonNull(start, "start can not be null");
        requireNonNull(end, "end can not be null");
        requireNonNull(users, "users can not be null");
        requireAfter(end, start, "end");
    }

    public Appointment(UUID id, LocalDateTime start, LocalDateTime end) {
        this(id, start, end, emptyList());
    }

    public Appointment users(List<User> users) {
        return new Appointment(id, start, end, users);
    }
}
