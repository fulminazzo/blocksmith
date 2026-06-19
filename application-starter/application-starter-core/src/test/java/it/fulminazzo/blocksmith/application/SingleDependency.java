package it.fulminazzo.blocksmith.application;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Mock annotation for testing purposes.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface SingleDependency {

    /**
     * Dependency.
     *
     * @return the dependency
     */
    String dependsOn();

}
