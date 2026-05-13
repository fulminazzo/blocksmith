package it.fulminazzo.blocksmith.reflect;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

/**
 * Mock class for testing purposes.
 *
 * @see ReflectFunctionalTest
 */
@Data
@AllArgsConstructor
public abstract class NamedEntity implements Entity {
    @Getter
    @Setter
    static String defaultName = "John";

    private @Nullable String name;

}
