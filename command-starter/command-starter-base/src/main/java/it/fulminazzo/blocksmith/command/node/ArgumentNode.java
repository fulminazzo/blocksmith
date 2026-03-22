package it.fulminazzo.blocksmith.command.node;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a dynamic argument node.
 *
 * @param <T> the type of the argument
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public final class ArgumentNode<T> extends CommandNode {
    private final @NotNull String name;
    private final @NotNull Class<T> type;
    private @Nullable String defaultValue;
    private boolean greedy;

    @Override
    public boolean matches(final @NotNull String token) {
        return true;
    }

}
