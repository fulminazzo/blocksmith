package it.fulminazzo.blocksmith.reflect;

import lombok.*;
import org.jetbrains.annotations.NotNull;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
public final class Person extends NamedEntity {
    @Getter
    @Setter
    static Integer DEFAULT_AGE = 18;

    private Integer age;

    public Person(final @NotNull String name, final @NotNull Integer age) {
        super(name);
        this.age = age;
    }

}
