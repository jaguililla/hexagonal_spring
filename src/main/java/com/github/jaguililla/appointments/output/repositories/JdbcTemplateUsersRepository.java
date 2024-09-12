package com.github.jaguililla.appointments.output.repositories;

import static java.util.Collections.emptySet;
import static java.util.Objects.requireNonNull;

import com.github.jaguililla.appointments.domain.UsersRepository;
import com.github.jaguililla.appointments.domain.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcTemplateUsersRepository implements UsersRepository {

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
    public Set<User> get(final Set<UUID> ids) {
        requireNonNull(ids, "ids must not be null");
        LOGGER.debug("--> Reading uid: {}", ids);

        if (ids.isEmpty())
            return emptySet();

        final var users = template.query(
            "select * from Users where id in (:id)",
            Map.of("id", ids),
            JdbcTemplateUsersRepository::userDataMapper
        );

        LOGGER.debug("=== Result: {}", users);

        return new HashSet<>(users);
    }

    @Override
    public boolean insert(final User user) {
        requireNonNull(user, "user must not be null");
        LOGGER.debug("--> Creating user: {}", user);

        final var parameters = Map.of("id", user.id(), "name", user.name());
        final var count = template.update("insert into Users values (:id, :name)", parameters);

        return count == 1;
    }
}
