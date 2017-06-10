package com.github.npetzall.testcontainers.junit.jdbc;

import com.github.npetzall.testcontainers.junit.jdbc.exceptions.InitScriptException;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.testcontainers.containers.JdbcDatabaseContainer;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class JdbcContainerRuleTest {

    @Test
    public void testInitScriptPathNotFound() {
        JdbcDatabaseContainer container = mock(JdbcDatabaseContainer.class);
        JdbcContainerRule<JdbcDatabaseContainer> containerRule = new JdbcContainerRule<>(() -> container)
                .withInitScript("this_should_not_exist");
        assertThatThrownBy(() -> containerRule.afterStart(container)).isInstanceOf(InitScriptException.class).hasMessageStartingWith("Could not load classpath init script");
    }

    @Test
    public void testInitScriptSqlException() throws SQLException {
        JdbcDatabaseContainer container = mock(JdbcDatabaseContainer.class);
        when(container.createConnection(anyString())).thenThrow(new SQLException());
        JdbcContainerRule<JdbcDatabaseContainer> containerRule = new JdbcContainerRule<>(() -> container)
                .withInitScript("org/testcontainers/junit/jdbc/initscripts/mysqlInitScript.sql");
        assertThatThrownBy(() -> containerRule.afterStart(container)).isInstanceOf(InitScriptException.class).hasMessageStartingWith("SQLException");
    }

    @Test
    public void  testThatSuppliedQueryStringIsSentToContainerCreateConnectionDuringInitScript() throws SQLException {
        String expected = "hello";
        JdbcDatabaseContainer container = mock(JdbcDatabaseContainer.class);
        ArgumentCaptor<String> queryString = ArgumentCaptor.forClass(String.class);
        JdbcContainerRule<JdbcDatabaseContainer> containerRule = new JdbcContainerRule<>(() -> container)
                .withInitScript("org/testcontainers/junit/jdbc/initscripts/mysqlInitScript.sql")
                .withQueryString(expected);
        try {
            containerRule.afterStart(container);
        } catch (Exception e) {
            //swallow
        }
        verify(container).createConnection(queryString.capture());
        assertThat(queryString.getValue()).isEqualToIgnoringCase(expected);
    }

    @Test
    public void  testThatSuppliedQueryStringIsSentToContainerCreateConnectionDuringInitFunction() throws SQLException {
        String expected = "hello";
        JdbcDatabaseContainer container = mock(JdbcDatabaseContainer.class);
        ArgumentCaptor<String> queryString = ArgumentCaptor.forClass(String.class);
        JdbcContainerRule<JdbcDatabaseContainer> containerRule = new JdbcContainerRule<>(() -> container)
                .withQueryString(expected)
                .withInitFunctions(connection -> {
                 //nothing
                });
        try {
            containerRule.afterStart(container);
        } catch (Exception e) {
            //swallow
        }
        verify(container).createConnection(queryString.capture());
        assertThat(queryString.getValue()).isEqualToIgnoringCase(expected);
    }

    @Test
    public void  testShouldNotThrowNullPointerIfNoInitFunctionsAreAdded() throws SQLException {
        JdbcDatabaseContainer container = mock(JdbcDatabaseContainer.class);
        JdbcContainerRule<JdbcDatabaseContainer> containerRule = new JdbcContainerRule<>(() -> container);
        assertThatCode(() -> containerRule.afterStart(container)).doesNotThrowAnyException();
    }
}
