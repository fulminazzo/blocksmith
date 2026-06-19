package it.fulminazzo.blocksmith.application;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Mock annotation for testing purposes.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface InvalidDependencies {

    /**
     * Invalid dependencies.
     *
     * @return the dependencies
     */
    int[] dependsOn();

}
