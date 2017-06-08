package com.github.npetzall.testcontainers.junit.jdbc;

import com.github.npetzall.testcontainers.junit.jdbc.exceptions.InitScriptException;
import org.junit.Test;
import org.testcontainers.containers.JdbcDatabaseContainer;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class JdbcContainerRuleTest {

    @Test
    public void testInitScriptPathNotFound() {
        JdbcDatabaseContainer container = mock(JdbcDatabaseContainer.class);
        doNothing().when(container).start();
        JdbcContainerRule<JdbcDatabaseContainer> containerRule = new JdbcContainerRule<>(() -> container)
                .withInitScript("this_should_not_exist");
        assertThatThrownBy(() -> containerRule.afterStart(container)).isInstanceOf(InitScriptException.class).hasMessageStartingWith("Could not load classpath init script");
    }

    @Test
    public void testInitScriptSqlException() throws SQLException {
        JdbcDatabaseContainer container = mock(JdbcDatabaseContainer.class);
        doNothing().when(container).start();
        when(container.createConnection(anyString())).thenThrow(new SQLException());
        JdbcContainerRule<JdbcDatabaseContainer> containerRule = new JdbcContainerRule<>(() -> container)
                .withInitScript("org/testcontainers/junit/jdbc/initscripts/mysqlInitScript.sql");
        assertThatThrownBy(() -> containerRule.afterStart(container)).isInstanceOf(InitScriptException.class).hasMessageStartingWith("SQLException");
    }
}
