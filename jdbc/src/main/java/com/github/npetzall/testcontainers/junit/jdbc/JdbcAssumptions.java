package com.github.npetzall.testcontainers.junit.jdbc;

import org.testcontainers.containers.JdbcDatabaseContainer;

import java.util.function.Consumer;

import static org.junit.Assume.assumeNoException;

public class JdbcAssumptions {

    private JdbcAssumptions() {
    }

    /**
     * Create an assumption on the JdbcDriver.
     * If it cant create the driver the test will be ignored.
     *
     * @param <T> the wrapped container
     * @return assumption that can be used in {@link com.github.npetzall.testcontainers.junit.generic.GenericContainerRule#withAssumptions(Consumer[])}
     */
    public static <T extends JdbcDatabaseContainer> Consumer<T> assumeDriverIsPresent() {
        return c -> {
            try {
                c.getJdbcDriverInstance();
            } catch (Exception e) {
                assumeNoException(e);
            }
        };
    }
}
