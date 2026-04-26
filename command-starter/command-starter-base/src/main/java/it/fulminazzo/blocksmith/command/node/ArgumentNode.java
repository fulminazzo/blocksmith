package it.fulminazzo.blocksmith.command.node;

import it.fulminazzo.blocksmith.command.CommandMessages;
import it.fulminazzo.blocksmith.command.argument.ArgumentParseException;
import it.fulminazzo.blocksmith.command.argument.ArgumentParser;
import it.fulminazzo.blocksmith.command.argument.ArgumentParsers;
import it.fulminazzo.blocksmith.command.node.handler.CompletionsSupplier;
import it.fulminazzo.blocksmith.command.visitor.CommandInput;
import it.fulminazzo.blocksmith.command.visitor.InputVisitor;
import it.fulminazzo.blocksmith.command.visitor.Visitor;
import it.fulminazzo.blocksmith.message.argument.Placeholder;
import it.fulminazzo.blocksmith.reflect.Reflect;
import it.fulminazzo.blocksmith.validation.ValidationException;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Parameter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a dynamic node in a command tree.
 * The value of the node is stored for later conversion.
 *
 * @param <T> the type that the value is converted to
 */
@SuppressWarnings("unchecked")
@Getter
@EqualsAndHashCode(callSuper = true, doNotUseGetters = true)
@ToString(callSuper = true, doNotUseGetters = true)
@FieldDefaults(level = AccessLevel.PROTECTED)
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public abstract class ArgumentNode<T> extends CommandNode {
    final @NotNull String name;
    final boolean optional;
    @Getter(AccessLevel.NONE)
    @Setter
    @Nullable String defaultValue;
    boolean greedy;

    @Nullable CompletionsSupplier completionsSupplier;

    /**
     * Attempts to advance the visitor cursor past this node token(s).
     * Intended exclusively for tab-completion purposes.
     *
     * @param visitor the visitor to get the input from
     * @return {@code true} if the cursor was correctly advanced, {@code false} otherwise
     */
    public boolean tryAdvanceCursor(final @NotNull InputVisitor<?, ?> visitor) {
        handleGreedy(visitor);
        final CommandInput input = visitor.getInput();
        if (input.isDone()) {
            if (defaultValue == null) return false;
            else input.addInput(defaultValue);
        }
        CommandInput snapshot = input.snapshot();
        if (getParser().tryAdvanceCursor(visitor) && !input.isDone())
            // there is something else to read,
            // we should advance cursor
            return true;
        else {
            input.restore(snapshot);
            return false;
        }
    }

    /**
     * Converts the current input of the {@link Visitor} to an instance of {@link #getType()}.
     * <br>
     * The conversion process uses the following rules:
     * <ul>
     *     <li>if the argument is {@link it.fulminazzo.blocksmith.command.annotation.Greedy},
     *     the entire input is merged in a single one;</li>
     *     <li>if no input was given and {@link it.fulminazzo.blocksmith.command.annotation.Default}
     *     was used, then the value of the annotation will be considered as input.
     *     Otherwise {@code null} is returned;</li>
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
     * @throws ValidationException    in case of invalid argument
     */
    public @Nullable T parseCurrent(final @NotNull InputVisitor<?, ?> visitor) throws ArgumentParseException, ValidationException {
        handleGreedy(visitor);
        final CommandInput input = visitor.getInput();
        if (input.isDone()) {
            if (defaultValue == null) return null;
            else input.addInput(defaultValue);
        }
        if (completionsSupplier != null) {
            String current = input.getCurrent();
            List<String> completions = completionsSupplier.getUnquoted();
            if (completions.stream().noneMatch(c -> c.equalsIgnoreCase(current)))
                throw new ArgumentParseException(CommandMessages.UNRECOGNIZED_ARGOMENT)
                        .arguments(
                                Placeholder.of(CommandMessages.ARGUMENT_PLACEHOLDER, current),
                                Placeholder.of("expected", String.join(", ", completions))
                        );
        }
        return parseCurrentImpl(visitor);
    }

    /**
     * Sets the current node to greedy.
     *
     * @param greedy if {@code true}, will take all the remaining input
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

    /**
     * Gets the {@link ArgumentParser} associated with the node type.
     *
     * @return the argument parser
     */
    public @NotNull ArgumentParser<T> getParser() {
        return ArgumentParsers.of(getType());
    }

    /**
     * Converts the current input of the {@link Visitor} to an instance of {@link #getType()}.
     * <br>
     * The conversion process uses the following rules:
     * <ul>
     *     <li>if the argument is {@link it.fulminazzo.blocksmith.command.annotation.Greedy},
     *     the entire input is merged in a single one;</li>
     *     <li>if no input was given and {@link it.fulminazzo.blocksmith.command.annotation.Default}
     *     was used, then the value of the annotation will be considered as input.
     *     Otherwise {@code null} is returned;</li>
     *     <li>if {@link it.fulminazzo.blocksmith.command.annotation.Tab} was used to specify custom completions
     *     and the input is not present in the values,
     *     it is considered invalid and a {@link ArgumentParseException} is thrown;</li>
     *     <li>finally, the input is checked against the associated {@link ArgumentParser} which
     *     will throw {@link ArgumentParseException} if is not valid.</li>
     * </ul>
     * For internal use only.
     *
     * @param visitor the visitor to get the input from
     * @return the parsed input
     * @throws ArgumentParseException in case of parsing exceptions
     * @throws ValidationException    in case of invalid argument
     */
    protected abstract @Nullable T parseCurrentImpl(final @NotNull InputVisitor<?, ?> visitor) throws ArgumentParseException, ValidationException;

    /**
     * Gets the type of the argument.
     *
     * @return the Java class
     */
    public abstract @NotNull Class<T> getType();

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

    @Override
    public @NotNull List<String> getCompletions(final @NotNull InputVisitor<?, ?> visitor) {
        handleGreedy(visitor);
        if (completionsSupplier != null) return completionsSupplier.get();
        else return getParser().getCompletions(visitor).stream()
                .map(c -> c.replace("%name%", getName()))
                .collect(Collectors.toList());
    }

    private void handleGreedy(final @NotNull InputVisitor<?, ?> visitor) {
        if (isGreedy()) visitor.getInput().mergeRemaining();
    }

    /**
     * Instantiates a new Argument node.
     *
     * @param <T>       the type of the parameter
     * @param name      the name
     * @param parameter the parameter that corresponds to the node
     * @param optional  if {@code true} the parameter will be non-mandatory
     * @return the argument node
     */
    @SuppressWarnings("RedundantCast")
    public static <T> @NotNull ArgumentNode<T> of(final @NotNull String name,
                                                  final @NotNull Parameter parameter,
                                                  final boolean optional) {
        Class<T> actualType = (Class<T>) Reflect.toWrapper(parameter.getType());
        if (Number.class.isAssignableFrom(actualType))
            return (ArgumentNode<T>) new NumberArgumentNode<>(name, parameter, optional);
        else return new ArgumentNodeImpl<>(name, parameter, optional);
    }

}
