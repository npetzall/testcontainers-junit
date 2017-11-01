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
import java.sql.Driver;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class JdbcContainerRule<T extends JdbcDatabaseContainer> extends GenericContainerRule<JdbcContainerRule<T>, T> {

    private static Logger log = LoggerFactory.getLogger(JdbcContainerRule.class);

    private String queryString ="";
    private String initScriptPath;
    private String initUser = null;
    private String initPassword = null;
    private List<Consumer<Connection>> initFunctions = new ArrayList();

    public JdbcContainerRule(Supplier<T> containerSupplier) {
        super(containerSupplier);
    }

    /**
     * String to add at end of jdbc url to customize the connection
     * Is used {@link #withInitFunctions(Consumer[])} and {@link #withInitScript(String)}
     *
     * @param queryString parameters for the connectionString
     * @return this
     */
    public JdbcContainerRule<T> withQueryString(String queryString) {
        this.queryString = queryString;
        return self();
    }

    /**
     * Run a script during startup of the container, before tests are executed.
     *
     * @param initScriptPath path to the initscript
     * @return this
     */
    public JdbcContainerRule<T> withInitScript(String initScriptPath) {
        this.initScriptPath = initScriptPath;
        return self();
    }

    /**
     * Execute a code-block before the tests are executed.
     * {@literal Consumer<Connection>}
     *
     * @param initFunctions {@literal Consumer<Connection>}
     * @return this
     */
    public JdbcContainerRule<T> withInitFunctions(Consumer<Connection>...initFunctions) {
        for(Consumer<Connection> initFunction : initFunctions) {
            this.initFunctions.add(initFunction);
        }
        return self();
    }

    /**
     * Specify a user with password that should be used when execution initScript or initFunction
     * @param user database user
     * @param password database password
     * @return this
     */
    public JdbcContainerRule<T> withInitUser(String user, String password) {
        initUser = user;
        initPassword = password;
        return self();
    }

    @Override
    protected void afterStart(T container) {
        super.afterStart(container);
        if (initScriptPath != null) {
            try {
                URL resource = Resources.getResource(initScriptPath);
                String sql = Resources.toString(resource, Charsets.UTF_8);
                ScriptUtils.executeSqlScript(getConnection(container), initScriptPath, sql);
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
                connection = getConnection(container);
                initFunction.accept(connection);
                if (!connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
                log.warn("Failed to execute function: {}", initFunction.getClass().getSimpleName() , e);
            }
        }
    }

    private Connection getConnection(T container) throws SQLException {
        if (initUser != null && initPassword != null) {
            Properties info = new Properties();
            info.put("user", initUser);
            info.put("password", initPassword);
            String url = container.getJdbcUrl() + queryString;
            Driver jdbcDriverInstance = container.getJdbcDriverInstance();
            return jdbcDriverInstance.connect(url, info);
        } else {
            return container.createConnection(queryString);
        }
    }
}
