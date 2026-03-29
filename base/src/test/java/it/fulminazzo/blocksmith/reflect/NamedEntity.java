package it.fulminazzo.blocksmith.reflect;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

@Data
public abstract class NamedEntity {
    @Getter
    @Setter
    static String DEFAULT_NAME = "John";

    private @NotNull String name;

}
