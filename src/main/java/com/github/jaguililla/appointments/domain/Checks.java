package com.github.jaguililla.appointments.domain;

import java.time.LocalDateTime;

import static java.lang.String.format;
import static java.util.Locale.US;
import static java.util.Objects.requireNonNull;

public interface Checks {

    static String requireNonBlank(String value, String field) {
        requireNonNull(value);

        if (value.isBlank())
            throw new IllegalArgumentException(format(US, "%s can not be blank: %s", field, value));

        return value;
    }

    static LocalDateTime requireAfter(LocalDateTime value, LocalDateTime limit, String field) {
        requireNonNull(value, format(US, "%s can not be null", field));
        if (limit != null && value.isBefore(limit))
            throw new IllegalArgumentException(
                format(US, "%s: %s can not be before to: %s", field, value, limit)
            );

        return value;
    }
}
