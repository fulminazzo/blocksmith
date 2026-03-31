package it.fulminazzo.blocksmith.reflect;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

@Data
@AllArgsConstructor
public abstract class NamedEntity implements Entity {
    @Getter
    @Setter
    static String DEFAULT_NAME = "John";

    private @Nullable String name;

}
