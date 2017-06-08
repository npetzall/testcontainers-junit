package com.github.npetzall.testcontainers.junit.generic;

import org.junit.AssumptionViolatedException;
import org.junit.runner.Description;
import org.junit.runners.model.MultipleFailureException;
import org.junit.runners.model.Statement;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.GenericContainer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class GenericContainerRule<SELF extends GenericContainerRule<SELF, T>, T extends GenericContainer> implements ContainerRule<SELF> {

    private Supplier<T> containerSupplier;
    private T container;
    private List<Consumer<T>> assumptions = new ArrayList<>();

    public GenericContainerRule(Supplier<T> containerSupplier) {
        this.containerSupplier = containerSupplier;
    }

    public SELF assumeDockerIsPresent() {
        final Supplier<T> originalContainerSupplier = containerSupplier;
        containerSupplier = () -> {
          try {
              DockerClientFactory.instance().client();
          } catch (Throwable t) {
              throw new AssumptionViolatedException("Unable to create container[might be docker related]", t);
          }
            return originalContainerSupplier.get();
        };
        return self();
    }

    public SELF withAssumptions(Consumer<T>...assumptions) {
        for (Consumer<T> consumer : assumptions) {
            this.assumptions.add(consumer);
        }
        return self();
    }

    public T getContainer() {
        return container;
    }

    @Override
    public Statement apply(Statement base, Description description) {
        container = containerSupplier.get();
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                List<Throwable> errors = new ArrayList<>();
                try {
                    beforeStart(container);
                    container.start();
                    afterStart(container);
                    beforeTest(container);
                    base.evaluate();
                    afterTest(container);
                } catch (Exception e) {
                    errors.add(e);
                    error(e);
                } finally {
                    beforeStop(container);
                    container.stop();
                    afterStop(container);
                }

                MultipleFailureException.assertEmpty(errors);
            }
        };
    }

    protected void beforeStart(T container) {
        assumptions.forEach(a -> a.accept(container));
    }

    protected void afterStart(T container) {
       //Hook
    }

    protected void beforeTest(T container) {
        //Hook
    }

    protected void afterTest(T container) {
        //Hook
    }

    protected void error(Throwable t) {
        //Hook
    }

    protected void beforeStop(T container) {
        //Hook
    }

    protected void afterStop(T container) {
        //Hook
    }

}
