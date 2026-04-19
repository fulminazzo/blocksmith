package it.fulminazzo.blocksmith.reflect;

import lombok.*;
import org.jetbrains.annotations.Nullable;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
public final class Person extends NamedEntity {
    @Getter
    @Setter
    static Integer defaultAge = 18;

    private @Nullable Integer age;

    public Person(final @Nullable String name, final @Nullable Integer age) {
        super(name);
        this.age = age;
    }

}
