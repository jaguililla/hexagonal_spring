package com.github.jaguililla.appointments.output.repositories;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.groupingBy;

import com.github.jaguililla.appointments.domain.AppointmentsRepository;
import com.github.jaguililla.appointments.domain.model.Appointment;
import com.github.jaguililla.appointments.domain.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Stream;

public class JdbcTemplateAppointmentsRepository implements AppointmentsRepository {

    private static final Logger LOGGER =
        LoggerFactory.getLogger(JdbcTemplateAppointmentsRepository.class);

    private static Appointment appointmentDataMapper(
        final ResultSet row, final int index
    ) throws SQLException {
        final var id = row.getObject("id", UUID.class);
        final var startTimestamp = row.getTimestamp("startTimestamp").toLocalDateTime();
        final var endTimestamp = row.getTimestamp("endTimestamp").toLocalDateTime();
        final var userId = row.getObject(4, UUID.class);
        final var name = row.getString("name");
        final var user = userId == null ? null : new User(userId, name);
        final var users = user == null ? Collections.<User>emptyList() : List.of(user);
        return new Appointment(id, startTimestamp, endTimestamp, users);
    }

    private final NamedParameterJdbcTemplate template;

    public JdbcTemplateAppointmentsRepository(final DataSource dataSource) {
        this.template = new NamedParameterJdbcTemplate(dataSource);
    }

    @Override
    public boolean insert(final Appointment appointment) {
        requireNonNull(appointment, "appointment cannot be null");
        LOGGER.debug("--> Creating appointment: {}", appointment);

        // TODO Transaction
        final var parameters = Map.of(
            "id", appointment.id(),
            "startTimestamp", appointment.start(),
            "endTimestamp", appointment.end()
        );
        final var userCount = appointment.users().stream().mapToInt(u ->
            template.update("insert into AppointmentsUsers values (:id, :userId)", Map.of(
                "id", appointment.id(),
                "userId", u.id()
            ))
        ).sum();

        final var count = template.update(
            "insert into Appointments values (:id, :startTimestamp, :endTimestamp)",
            parameters
        );

        return count == 1;
    }

    @Override
    public boolean delete(final UUID id) {
        requireNonNull(id, "id cannot be null");
        LOGGER.debug("--> Deleting aid: {}", id);

        // TODO Transaction
        final var parameters = Map.of("id", id);
        final var usersCount =
            template.update("delete from AppointmentsUsers where appointmentId = :id", parameters);
        final var count = template.update("delete from Appointments where id = :id", parameters);

        return count == 1;
    }

    @Override
    public Appointment get(final UUID id) {
        requireNonNull(id, "id cannot be null");
        LOGGER.debug("--> Reading aid: {}", id);

        final var appointments = template.query(
            """
            select a.*, u.*
            from
              Appointments a
              left join AppointmentsUsers au on a.id = au.appointmentId
              left join Users u on au.userId = u.id
            where a.id = :id
            """,
            Map.of("id", id),
            JdbcTemplateAppointmentsRepository::appointmentDataMapper
        );

        final var x = appointments
            .stream()
            .reduce((a, b) ->
                b.users(Stream.concat(a.users().stream(), b.users().stream()).toList())
            )
            .orElse(null);

        LOGGER.debug("=== Result: {}", x);

        return x;
    }

    @Override
    public List<Appointment> getAll() {
        LOGGER.info("--> Reading appointments");

        final var appointments = template.query(
            """
            select a.*, u.*
            from
              Appointments a
              left join AppointmentsUsers au on a.id = au.appointmentId
              left join Users u on au.userId = u.id
            """,
            JdbcTemplateAppointmentsRepository::appointmentDataMapper
        );

        final var groupedAppointments = groupAppointments(appointments);
        LOGGER.debug("=== Result: {}", groupedAppointments);
        return groupedAppointments;
    }

    private List<Appointment> groupAppointments(final List<Appointment> appointments) {
        requireNonNull(appointments, "appointments cannot be null");
        return appointments
            .stream()
            .collect(groupingBy(Appointment::id))
            .values()
            .stream()
            .map(it ->
                it.stream().reduce((a, b) ->
                    b.users(Stream.concat(a.users().stream(), b.users().stream()).toList())
                )
            )
            .filter(Optional::isPresent)
            .map(Optional::orElseThrow)
            .toList();
    }
}
