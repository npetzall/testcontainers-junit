package org.testcontainers.dockerclient;

import com.github.dockerjava.api.DockerClient;

public class FailingDockerClientProviderStrategy extends DockerClientProviderStrategy {
    @Override
    public void test() throws DockerClientProviderStrategy.InvalidConfigurationException {

    }

    @Override
    public String getDescription() {
        return "ExceptionInInitializerError is thrown on getClient";
    }

    @Override
    public DockerClient getClient() {
        throw new ExceptionInInitializerError("There is no docker");
    }
}
