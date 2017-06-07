package com.github.npetzall.testcontainers.junit.jdbc;

import org.testcontainers.containers.JdbcDatabaseContainer;

import java.util.function.Consumer;

import static org.junit.Assume.assumeNoException;

public class JdbcAssumptions {

    private JdbcAssumptions() {
    }

    public static <T extends JdbcDatabaseContainer> Consumer<T> assumeDriverIsPresent() {
        return c -> {
            try {
                c.getJdbcDriverInstance();
            } catch (Throwable t) {
                assumeNoException(t);
            }
        };
    }
}
