package com.github.jaguililla.appointments;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

import org.junit.jupiter.api.function.Executable;

public interface Asserts {

    static void assertIllegalArgument(String message, Executable executable) {
        assertThrows(IllegalArgumentException.class, message, executable);
    }

    static void assertNull(String field, Executable executable) {
        assertThrows(NullPointerException.class, "%s can not be null".formatted(field), executable);
    }

    static <T extends Throwable> void assertThrows(
        Class<T> expectedType, String message, Executable executable
    ) {
        var e = assertThrowsExactly(expectedType, executable);
        assertEquals(message, e.getMessage());
    }
}
