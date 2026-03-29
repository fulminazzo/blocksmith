package it.fulminazzo.blocksmith.reflect;

import lombok.Value;
import org.jetbrains.annotations.NotNull;

@Value
public class Person {
    static final String DEFAULT_NAME = "John";
    static final int DEFAULT_AGE = 18;

    @NotNull String name;
    int age;

}
