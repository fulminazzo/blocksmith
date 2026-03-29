package it.fulminazzo.blocksmith.reflect;

import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
public abstract class NamedEntity {
    static final String DEFAULT_NAME = "John";

    @NotNull String name;

}
