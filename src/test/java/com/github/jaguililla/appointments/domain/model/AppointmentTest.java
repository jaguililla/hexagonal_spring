package com.github.jaguililla.appointments.domain.model;

import static com.github.jaguililla.appointments.Asserts.assertIllegalArgument;
import static com.github.jaguililla.appointments.Asserts.assertNull;
import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Locale.US;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

class AppointmentTest {

    @Test
    void appointment_checks_creation_parameters() {
        var id = UUID.randomUUID();
        var now = LocalDateTime.now();
        var tomorrow = now.plusDays(1);
        var yesterday = now.minusDays(1);

        assertNull("id", () -> new Appointment(null, now, tomorrow));
        assertNull("start", () -> new Appointment(id, null, tomorrow));
        assertNull("end", () -> new Appointment(id, now, null));
        assertNull("users", () -> new Appointment(id, now, tomorrow, null));

        assertIllegalArgument(
            format(US, "%s: %s can not be before to: %s", "end", yesterday, now),
            () -> new Appointment(id, now, yesterday)
        );
    }

    @Test
    void appointment_can_be_created_correctly() {
        var id = UUID.randomUUID();
        var now = LocalDateTime.now();
        var tomorrow = now.plusDays(1);
        var yesterday = now.minusDays(1);

        assertEquals(emptyList(), new Appointment(id, now, now).users());
        assertEquals(emptyList(), new Appointment(id, now, tomorrow).users());
        assertEquals(emptyList(), new Appointment(id, yesterday, now).users());
    }

    @Test
    void appointment_can_be_copied_with_different_users() {
        var id = UUID.randomUUID();
        var now = LocalDateTime.now();
        var bill = new User(id, "Bill");
        var sue = new User(id, "Sue");
        var appointment = new Appointment(id, now, now, List.of(bill));

        assertEquals(List.of(bill), appointment.users());
        assertEquals(List.of(sue), appointment.users(List.of(sue)).users());
    }
}
