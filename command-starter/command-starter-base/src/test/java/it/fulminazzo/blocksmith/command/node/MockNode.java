package it.fulminazzo.blocksmith.command.node;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

@Value
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MockNode extends CommandNode {
    @NotNull String name;

    @Override
    public boolean matches(final @NotNull String token) {
        return token.equals(name);
    }

}
