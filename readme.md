[![Build Status](https://travis-ci.org/npetzall/testcontainers-junit.svg?branch=master)](https://travis-ci.org/npetzall/testcontainers-junit)
[![GitHub license](https://img.shields.io/badge/license-MIT-blue.svg)](https://raw.githubusercontent.com/npetzall/testcontainers-junit/master/LICENSE.md)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=com.github.npetzall.testcontainers.junit%3Ajunit-parent&metric=alert_status)](https://sonarcloud.io/dashboard?id=com.github.npetzall.testcontainers.junit%3Ajunit-parent)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=com.github.npetzall.testcontainers.junit%3Ajunit-parent&metric=ncloc)](https://sonarcloud.io/component_measures?id=com.github.npetzall.testcontainers.junit%3Ajunit-parent&metric=ncloc)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=com.github.npetzall.testcontainers.junit%3Ajunit-parent&metric=coverage)](https://sonarcloud.io/component_measures?id=com.github.npetzall.testcontainers.junit%3Ajunit-parent&metric=coverage)
[![GitHub tag](https://img.shields.io/github/tag/npetzall/testcontainers-junit.svg)](https://github.com/npetzall/testcontainers-junit/tags)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.npetzall.testcontainers.junit/junit-parent.svg)](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.github.npetzall.testcontainers.junit%22)  
# JUnit Additions for testcontainers.

This is used instead of the Container as [`@Rule`](http://junit.org/junit4/javadoc/latest/org/junit/Rule.html) or [`@ClassRule`](http://junit.org/junit4/javadoc/latest/org/junit/ClassRule.html)

This was developed since it currently lacks the ability to [assume](http://junit.org/junit4/javadoc/latest/org/junit/Assume.html) that docker exists.

Depending on the type of test one is performing. The lack of docker should be an ignored test instead of a failing test.  
This is to make a Failure speak about the SUT instead of your ability to run docker.

You want your tests to be small and keep the reasons for failure to a minimum.

[Testcontainers](https://github.com/testcontainers/testcontainers-java)

Examples GenericContainer or subclass:
```java
import org.junit.ClassRule;
import org.junit.Test;
import com.github.npetzall.testcontainers.junit.generic.GenericContainerRule;
import java.sql.Connection;
import java.sql.Statement;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.testcontainers.containers.MySQLContainer;
  
public class SomeTestClassWithGenericContainerRule {  
  
    @ClassRule
    public static GenericContainerRule<MySQLContainer> mysqlContainerRule = new GenericContainerRule<>(() -> new MySQLContainer())
                                  .assumeDockerIsPresent()
                                  .withAssumptions(container -> {
                                      try {
                                          container.getJdbcDriverInstance();
                                      } catch (Exception e) {
                                          throw new AssumptionViolatedException("No driver");
                                      }
                                  });
    
    @Test
    public void doSomeTest(){
        Connection connection = mysqlContainerRule.getContainer().createConnetion("");
        Statement statment = connection.createStatment();
        //do stuff with the connection
    }
      
    @Test
    public void doSomeOtherTest(){
        MysqlContainer mysqlContainer = mysqlContainerRule.getContainer();
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(mysqlContainer.getJdbcUrl());
        hikariConfig.setUsername(mysqlContainer.getUsername());
        hikariConfig.setPassword(mysqlContainer.getPassword());
    
        HikariDataSource ds = new HikariDataSource(hikariConfig);
        // do stuff with the datasource
    }
}
```

Example JdcbDatabaseContainer or subclass
```java
import org.junit.ClassRule;
import org.junit.Test;
import java.sql.Connection;
import java.sql.Statement;
import org.testcontainers.containers.MySQLContainer;
import com.github.npetzall.testcontainers.junit.jdbc.JdbcContainerRule;
  
import static com.github.npetzall.testcontainers.junit.jdbc.JdbcAssumptions.assumeDriverIsPresent;
  
public class SomeTestClassWithJdbcContainerRule {
    
    @ClassRule
    public static JdbcContainerRule<MySQLContainer> mysqlContainerJdbcRule = new JdbcContainerRule<>(() -> new MySQLContainer())
            .withInitScript("org/testcontainers/junit/jdbc/initscripts/mysqlInitScript.sql")
            .assumeDockerIsPresent()
            .withAssumptions(assumeDriverIsPresent())
            .withInitFunctions(connection -> {
                try {
                    Statement statement = connection.createStatement();
                    statement.execute("INSERT INTO junit_jdbc values (2, 'added by function');");
                    if (!connection.getAutoCommit()) {
                        connection.commit();
                    }
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });
    
    @Test
    public void someTest() {
        MysqlContainer mysqlContainer = mysqlContainerJdbcRule.getContainer();
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(mysqlContainer.getJdbcUrl());
        hikariConfig.setUsername(mysqlContainer.getUsername());
        hikariConfig.setPassword(mysqlContainer.getPassword());
    
        HikariDataSource ds = new HikariDataSource(hikariConfig);
        // do stuff with the datasource
    }
}
```
