package com.github.jaguililla.appointments.output.stores;

import com.github.jaguililla.appointments.domain.UsersRepository;
import com.github.jaguililla.appointments.domain.model.User;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

public final class JdbcTemplateUsersRepository implements UsersRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(JdbcTemplateUsersRepository.class);

    private static User userDataMapper(
        final ResultSet row, final int index
    ) throws SQLException {
        final var id = row.getObject("id", UUID.class);
        final var name = row.getString("name");
        return new User(id, name);
    }

    private final NamedParameterJdbcTemplate template;

    public JdbcTemplateUsersRepository(final DataSource dataSource) {
        this.template = new NamedParameterJdbcTemplate(dataSource);
    }

    @Override
    public Set<User> get(final Set<UUID> id) {
        LOGGER.debug("--> Reading uid: {}", id);

        final var users = id.isEmpty()
            ? Collections.<User>emptyList()
            : template.query(
                "select * from Users where id in (:id)",
                Map.of("id", id),
                JdbcTemplateUsersRepository::userDataMapper
            );

        LOGGER.debug("=== Result: {}", users);

        return new HashSet<>(users);
    }
}
