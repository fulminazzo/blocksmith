package it.fulminazzo.blocksmith.application;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@interface InvalidDependencies {

    int[] dependsOn();

}
