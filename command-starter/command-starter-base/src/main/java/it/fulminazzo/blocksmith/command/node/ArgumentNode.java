package it.fulminazzo.blocksmith.command.node;

import it.fulminazzo.blocksmith.command.argument.ArgumentParseException;
import it.fulminazzo.blocksmith.command.argument.ArgumentParser;
import it.fulminazzo.blocksmith.command.argument.ArgumentParsers;
import it.fulminazzo.blocksmith.command.node.handler.CompletionsSupplier;
import it.fulminazzo.blocksmith.command.visitor.CommandInput;
import it.fulminazzo.blocksmith.command.visitor.Visitor;
import it.fulminazzo.blocksmith.message.argument.Placeholder;
import it.fulminazzo.blocksmith.reflect.Reflect;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

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
     * Converts the current input of the {@link Visitor} to an instance of {@link #type}.
     * <br>
     * The conversion process uses the following rules:
     * <ul>
     *     <li>if the argument is {@link it.fulminazzo.blocksmith.command.annotation.Greedy},
     *     the entire input is merged in a single one;</li>
     *     <li>if no input was given and {@link it.fulminazzo.blocksmith.command.annotation.Default}
     *     was used, then the value of the annotation will be considered as input.
     *     Otherwise <code>null</code> is returned;</li>
     *     <li>if {@link it.fulminazzo.blocksmith.command.annotation.Tab} was used to specify custom completions
     *     and the input is not present in the values,
     *     it is considered invalid and a {@link ArgumentParseException} is thrown;</li>
     *     <li>finally, the input is checked against the associated {@link ArgumentParser} which
     *     will throw {@link ArgumentParseException} if is not valid.</li>
     * </ul>
     *
     * @param visitor the visitor to get the input from
     * @return the parsed input
     * @throws ArgumentParseException in case of parsing exceptions
     */
    public @Nullable T parseCurrent(final @NotNull Visitor<?, ?> visitor) throws ArgumentParseException {
        final CommandInput input = visitor.getInput();
        if (isGreedy()) input.mergeRemaining();
        if (input.isDone()) {
            if (defaultValue == null) return null;
            else input.addInput(defaultValue);
        }
        final @NotNull ArgumentParser<T> parser = ArgumentParsers.of(type);
        if (completionsSupplier != null) {
            String current = input.getCurrent();
            List<String> completions = completionsSupplier.get();
            if (completions.stream().noneMatch(c -> c.equalsIgnoreCase(current)))
                throw new ArgumentParseException("error.invalid-argument")
                        .arguments(
                                Placeholder.of("argument", current),
                                Placeholder.of("expected", String.join(", ", completions))
                        );
        }
        return parser.parse(visitor);
    }

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
    public <O, X extends Exception> O accept(final @NotNull Visitor<O, X> visitor) throws X {
        return visitor.visitArgumentNode(this);
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
