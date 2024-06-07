package com.github.jaguililla.appointments.domain.model;

import static com.github.jaguililla.appointments.domain.Checks.requireNonBlank;
import static java.util.Objects.requireNonNull;

import java.util.UUID;

public record User(
    UUID id,
    String name
) {
    public User {
        requireNonNull(id, "id can not be null");
        requireNonBlank(name, "name");
    }
}
