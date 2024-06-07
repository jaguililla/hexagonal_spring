package com.github.jaguililla.appointments.domain.model;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static com.github.jaguililla.appointments.Asserts.assertIllegalArgument;
import static com.github.jaguililla.appointments.Asserts.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

class UserTest {

    @Test
    void user_checks_creation_parameters() {
        var id = UUID.randomUUID();

        assertNull("id", () -> new User(null, "Jill"));
        assertIllegalArgument("name can not be blank: ", () -> new User(id, ""));
    }

    @Test
    void user_can_be_created_correctly() {
        assertEquals("Mary", new User(UUID.randomUUID(), "Mary").name());
    }
}
