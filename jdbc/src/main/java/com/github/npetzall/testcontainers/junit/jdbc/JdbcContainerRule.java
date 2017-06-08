package com.github.npetzall.testcontainers.junit.jdbc;

import com.github.npetzall.testcontainers.junit.generic.GenericContainerRule;
import com.github.npetzall.testcontainers.junit.jdbc.exceptions.InitScriptException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.jdbc.ext.ScriptUtils;
import org.testcontainers.shaded.com.google.common.base.Charsets;
import org.testcontainers.shaded.com.google.common.io.Resources;

import javax.script.ScriptException;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class JdbcContainerRule<T extends JdbcDatabaseContainer> extends GenericContainerRule<JdbcContainerRule<T>, T> {

    private static Logger log = LoggerFactory.getLogger(JdbcContainerRule.class);

    private String queryString ="";
    private String initScriptPath;
    private Consumer<Connection>[] initFunctions;

    public JdbcContainerRule(Supplier<T> containerSupplier) {
        super(containerSupplier);
    }

    public JdbcContainerRule<T> withQueryString(String queryString) {
        this.queryString = queryString;
        return self();
    }

    public JdbcContainerRule<T> withInitScript(String initScriptPath) {
        this.initScriptPath = initScriptPath;
        return self();
    }

    public JdbcContainerRule<T> withInitFunctions(Consumer<Connection>...initFunctions) {
        this.initFunctions = initFunctions;
        return self();
    }

    @Override
    protected void afterStart(T container) {
        super.afterStart(container);
        if (initScriptPath != null) {
            try {
                URL resource = Resources.getResource(initScriptPath);
                String sql = Resources.toString(resource, Charsets.UTF_8);
                ScriptUtils.executeSqlScript(container.createConnection(queryString), initScriptPath, sql);
            } catch (IOException | IllegalArgumentException e) {
                log.error("Could not load classpath init script: {}", initScriptPath);
                throw new InitScriptException("Could not load classpath init script: " + initScriptPath, e);
            } catch (ScriptException | SQLException e) {
                log.error("Error while execution init script: {}", initScriptPath, e);
                throw new InitScriptException("SQLException: ", e);
            }
        }

        for(Consumer initFunction : initFunctions) {
            Connection connection = null;
            try {
                connection = container.createConnection(queryString);
                initFunction.accept(connection);
                if (!connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
                log.warn("Failed to execute function: {}", initFunction.getClass().getSimpleName() , e);
            }
        }
    }
}
