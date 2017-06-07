package com.github.npetzall.testcontainers.junit.jdbc;

import org.junit.AssumptionViolatedException;
import org.junit.Test;
import org.testcontainers.containers.JdbcDatabaseContainer;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JdbcAssumptionsTest {

    @Test
    public void assumptionExceptionIsThrownWhengetJdbcDriverThrowsThrowable() {
        JdbcDatabaseContainer jdbcDatabaseContainerMock = mock(JdbcDatabaseContainer.class);
        when(jdbcDatabaseContainerMock.getJdbcDriverInstance()).thenThrow(new RuntimeException("Could not get Driver"));
        assertThatThrownBy(() -> JdbcAssumptions.assumeDriverIsPresent().accept(jdbcDatabaseContainerMock)).isInstanceOf(AssumptionViolatedException.class);
    }

}
