package com.github.npetzall.testcontainers.junit.generic;

import org.junit.rules.TestRule;

public interface ContainerRule<SELF> extends TestRule {

    default SELF self() {
        return (SELF) this;
    }

}
