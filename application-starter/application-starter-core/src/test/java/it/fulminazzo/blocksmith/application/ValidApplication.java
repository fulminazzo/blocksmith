package it.fulminazzo.blocksmith.application;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Mock {@link Application} implementation for testing purposes.
 */
@SuppressWarnings("unused")
final class ValidApplication implements Application {

    @NoDependencies
    private Object first;

    @SingleDependency(dependsOn = "first")
    private Object second;

    @SingleDependency(dependsOn = "first")
    private Object third;

    @MultipleDependencies(dependsOn = {"second", "third"})
    private Object fourth;

    @NoDependencies
    private Object fifth;

    @Override
    public @NotNull File directory() {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull Logger logger() {
        return LoggerFactory.getLogger(getClass());
    }

}
