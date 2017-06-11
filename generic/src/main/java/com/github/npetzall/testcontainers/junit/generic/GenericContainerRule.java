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

    /**
     * Add an assumption prior to container creation that throws {@link AssumptionViolatedException}
     * so that tests are ignored. This is opt-in
     *
     * @return this
     */

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

    /**
     * Add assumptions, they will be evaluated before start.
     * If assumption fails it should throw {@link AssumptionViolatedException}
     *
     * @param assumptions {@literal Consumer<Container>}
     * @return this
     */

    public SELF withAssumptions(Consumer<T>...assumptions) {
        for (Consumer<T> consumer : assumptions) {
            this.assumptions.add(consumer);
        }
        return self();
    }

    /**
     * Get the wrapped container
     *
     * @return Subclass of GenericContainer.
     */
    public T getContainer() {
        return container;
    }

    /** {@inheritDoc} */
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
