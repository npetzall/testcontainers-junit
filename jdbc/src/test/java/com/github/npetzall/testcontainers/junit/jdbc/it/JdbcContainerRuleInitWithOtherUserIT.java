package com.github.npetzall.testcontainers.junit.jdbc.it;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.MySQLContainer;

import com.github.npetzall.testcontainers.junit.jdbc.JdbcContainerRule;

import static com.github.npetzall.testcontainers.junit.jdbc.JdbcAssumptions.assumeDriverIsPresent;
import static org.assertj.core.api.Assertions.assertThat;

public class JdbcContainerRuleInitWithOtherUserIT {
  @ClassRule
  public static JdbcContainerRule<MySQLContainer> jdbcContainerRule = new JdbcContainerRule<>(() -> new MySQLContainer())
    .withInitScript("org/testcontainers/junit/jdbc/initscripts/mysqlInitScriptRoot.sql")
    .assumeDockerIsPresent()
    .withAssumptions(assumeDriverIsPresent())
    .withInitFunctions(connection -> {
      try {
        Statement statement = connection.createStatement();
        statement.execute("INSERT INTO by_root.in_by_root values (2, 'added by function');");
        if (!connection.getAutoCommit()) {
          connection.commit();
        }
        connection.close();
      } catch (SQLException e) {
        e.printStackTrace();
      }
    })
    .withInitUser("root","test")
    .withQueryString("?useSSL=false");

  @Test
  public void scriptExecuted() throws SQLException {
    String text = getText(1);
    assertThat(text).isEqualToIgnoringCase("added by script").as("Found text added by script");
  }

  @Test
  public void functionExecuted() throws SQLException {
    String text = getText(2);
    assertThat(text).isEqualToIgnoringCase("added by function").as("Found text added by function");
  }

  private String getText(int id) throws SQLException {
    Connection connection = jdbcContainerRule.getContainer().createConnection("?useSSL=false");
    ResultSet rs = connection.createStatement().executeQuery("SELECT text FROM by_root.in_by_root where ID=" + id);
    rs.next();
    return rs.getString(1);
  }
}
