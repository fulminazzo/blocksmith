package it.fulminazzo.blocksmith.application;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.File;

/**
 * Mock {@link Application} implementation for testing purposes.
 */
@SuppressWarnings("unused")
final class ValidApplication implements Application {

    private Object first;

    @SingleDependency(dependsOn = "first")
    private Object second;

    @SingleDependency(dependsOn = "first")
    private Object third;

    @MultipleDependencies(dependsOn = {"second", "third"})
    private Object fourth;

    private Object fifth;

    @Override
    public @NotNull File directory() {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull Logger logger() {
        throw new UnsupportedOperationException();
    }

}
