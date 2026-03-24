package it.fulminazzo.blocksmith.command.node;

import it.fulminazzo.blocksmith.command.execution.CommandExecutionContext;
import it.fulminazzo.blocksmith.command.execution.CommandExecutionException;
import it.fulminazzo.blocksmith.message.argument.Placeholder;
import org.jetbrains.annotations.NotNull;

/**
 * Special implementation of {@link ArgumentNode} for {@link Number} arguments.
 *
 * @param <N> the type of the argument
 */
public final class NumberArgumentNode<N extends Number> extends ArgumentNode<N> {
    private double min = -Double.MAX_VALUE;
    private double max = Double.MAX_VALUE;

    /**
     * Instantiates a new Number argument node.
     *
     * @param name     the name
     * @param type     the type
     * @param optional the optional
     */
    NumberArgumentNode(final @NotNull String name,
                       final @NotNull Class<N> type,
                       final boolean optional) {
        super(name, type, optional);
    }

    @Override
    protected void validateExecuteInput(final @NotNull CommandExecutionContext context) throws CommandExecutionException {
        Number parsed = getArgumentParser().parse(context);
        double value = parsed.doubleValue();
        if (value < min || value > max)
            throw new CommandExecutionException("error.invalid-number")
                    .arguments(
                            Placeholder.of("argument", context.getCurrent()),
                            Placeholder.of("min", cast(min)),
                            Placeholder.of("max", cast(max))
                    );
        context.addParsedArgument(parsed);
    }

    /**
     * Casts the given number to the current type.
     *
     * @param value the value to cast
     * @return the number
     */
    public @NotNull N cast(final double value) {
        final Class<N> type = getType();
        if (type == Byte.class) return type.cast((byte) value);
        else if (type == Short.class) return type.cast((short) value);
        else if (type == Integer.class) return type.cast((int) value);
        else if (type == Long.class) return type.cast((long) value);
        else if (type == Float.class) return type.cast((float) value);
        else return type.cast(value);
    }

    @Override
    protected void validateTabCompleteInput(final @NotNull CommandExecutionContext context) throws CommandExecutionException {
        validateExecuteInput(context);
    }

    /**
     * Sets the minimum allowed value.
     *
     * @param min the value
     * @return this object (for method chaining)
     */
    public @NotNull NumberArgumentNode<N> min(final double min) {
        this.min = min;
        return this;
    }

    /**
     * Sets the minimum allowed value.
     *
     * @param max the value
     * @return this object (for method chaining)
     */
    public @NotNull NumberArgumentNode<N> max(final double max) {
        this.max = max;
        return this;
    }

}
