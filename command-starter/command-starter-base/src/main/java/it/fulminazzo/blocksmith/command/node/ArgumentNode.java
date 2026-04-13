package it.fulminazzo.blocksmith.command.node;

import it.fulminazzo.blocksmith.command.node.handler.CompletionsSupplier;
import it.fulminazzo.blocksmith.reflect.Reflect;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a dynamic node in a command tree.
 * The value of the node is stored for later conversion.
 *
 * @param <T> the type that the value is converted to
 */
@Getter
@EqualsAndHashCode(callSuper = true, doNotUseGetters = true)
@ToString(callSuper = true, doNotUseGetters = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class ArgumentNode<T> extends CommandNode {
    final @NotNull String name;
    final @NotNull Class<T> type;
    final boolean optional;
    @Getter(AccessLevel.NONE)
    @Setter
    @Nullable String defaultValue;
    boolean greedy;

    @Nullable CompletionsSupplier completionsSupplier;

    /**
     * Sets the current node to greedy.
     *
     * @param greedy if <code>true</code>, will take all the remaining input
     * @return this object (for method chaining)
     */
    public @NotNull ArgumentNode<T> setGreedy(final boolean greedy) {
        this.greedy = greedy;
        return this;
    }

    /**
     * Toggles the custom completions supplier.
     *
     * @param completionsSupplier the completions supplier
     * @return this object (for method chaining)
     */
    public @NotNull ArgumentNode<T> setCompletionsSupplier(final @Nullable CompletionsSupplier completionsSupplier) {
        this.completionsSupplier = completionsSupplier;
        return this;
    }

    @Override
    public boolean matches(final @NotNull String token) {
        // the actual validation is delegated to the parsers,
        // so they can deliver proper error messages
        return true;
    }

    /**
     * Instantiates a new Argument node.
     *
     * @param <T>      the type of the parameter
     * @param name     the name
     * @param type     the Java class of the parameter
     * @param optional if <code>true</code> the parameter will be non-mandatory
     * @return the argument node
     */
    @SuppressWarnings("unchecked")
    public static <T> @NotNull ArgumentNode<T> of(final @NotNull String name,
                                                  @NotNull Class<?> type,
                                                  final boolean optional) {
        Class<T> actualType = (Class<T>) Reflect.toWrapper(type);
        if (Number.class.isAssignableFrom(actualType))
            return (ArgumentNode<T>) new NumberArgumentNode<>(name, (Class<? extends Number>) actualType, optional);
        else return new ArgumentNode<>(name, actualType, optional);
    }

}
