package com.github.npetzall.testcontainers.junit.generic;

import org.junit.AssumptionViolatedException;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.dockerclient.FailingDockerClientProviderStrategy;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class GenericContainerRuleTest {

    @ClassRule
    public static GenericContainerRule genericContainerRule = new GenericContainerRule(() -> new GenericContainer<>().withCommand("ping -c 5 www.google.com")).assumeDockerIsPresent();

    @Test
    public void whenDockerIsPresentNoExceptionIsThrownAndContainerCanBeRetrieved() {
        assertThat(genericContainerRule.getContainer().isRunning()).isTrue().as("Container is running");
    }
}
