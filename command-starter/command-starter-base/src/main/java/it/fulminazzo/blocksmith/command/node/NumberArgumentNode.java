package it.fulminazzo.blocksmith.command.node;

import it.fulminazzo.blocksmith.command.visitor.Visitor;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;

/**
 * Special implementation of {@link ArgumentNode} for {@link Number} arguments.
 *
 * @param <N> the type of the argument
 */
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public final class NumberArgumentNode<N extends Number> extends ArgumentNode<N> {
    double min = -Double.MAX_VALUE;
    double max = Double.MAX_VALUE;

    /**
     * Instantiates a new Number argument node.
     *
     * @param name     the name of the node
     * @param type     the Java class of the node
     * @param optional <code>true</code> if the node may be omitted
     */
    NumberArgumentNode(final @NotNull String name, final @NotNull Class<N> type, final boolean optional) {
        super(name, type, optional);
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

    @Override
    public <T, X extends Exception> T accept(final @NotNull Visitor<T, X> visitor) throws X {
        return visitor.visitNumberArgumentNode(this);
    }

}
