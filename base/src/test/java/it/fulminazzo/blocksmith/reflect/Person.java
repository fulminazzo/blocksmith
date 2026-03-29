package it.fulminazzo.blocksmith.reflect;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Value
public class Person extends NamedEntity {
    static final Integer DEFAULT_AGE = 18;

    Integer age;

    public Person(final @NotNull String name, final @NotNull Integer age) {
        super(name);
        this.age = age;
    }

}
