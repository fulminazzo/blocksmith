package it.fulminazzo.blocksmith.reflect;

import lombok.*;
import org.jetbrains.annotations.Nullable;

/**
 * Mock class for testing purposes.
 *
 * @see ReflectFunctionalTest
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public final class Person extends NamedEntity {
    @Getter
    @Setter
    static Integer defaultAge = 18;

    private @Nullable Integer age;

    /**
     * Instantiates a new Person.
     *
     * @param name the name
     * @param age  the age
     */
    public Person(final @Nullable String name, final @Nullable Integer age) {
        super(name);
        this.age = age;
    }

}
