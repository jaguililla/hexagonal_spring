package com.github.jaguililla.appointments.domain;

import com.github.jaguililla.appointments.domain.model.User;
import java.util.Set;
import java.util.UUID;

public interface UsersRepository {
    Set<User> get(final Set<UUID> id);
}
