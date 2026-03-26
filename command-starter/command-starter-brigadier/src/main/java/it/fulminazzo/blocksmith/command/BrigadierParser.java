package it.fulminazzo.blocksmith.command;

import com.mojang.brigadier.arguments.*;
import it.fulminazzo.blocksmith.command.node.ArgumentNode;
import it.fulminazzo.blocksmith.command.node.NumberArgumentNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unchecked")
final class BrigadierParser {

    /**
     * Converts the given number node to a Brigadier argument type.
     *
     * @param <T>  the type of the argument
     * @param <A>  the Java type
     * @param node the argument node
     * @return the argument type (or <code>null</code> if not found)
     */
    static <T, A> @Nullable ArgumentType<T> getArgumentType(final @NotNull ArgumentNode<A> node) {
        if (node.isGreedy()) return (ArgumentType<T>) StringArgumentType.greedyString();
        Class<A> type = node.getType();
        if (Number.class.isAssignableFrom(type))
            return getArgumentType((NumberArgumentNode<Number>) node);
        else if (type.equals(Boolean.class)) return (ArgumentType<T>) BoolArgumentType.bool();
        else if (type.equals(String.class)) return (ArgumentType<T>) StringArgumentType.string();
        else return null;
    }

    /**
     * Converts the given number node to a Brigadier argument type.
     *
     * @param <T>  the type of the argument
     * @param <N>  the Java type
     * @param node the node
     * @return the argument type
     */
    static <T, N extends Number> @NotNull ArgumentType<T> getArgumentType(final @NotNull NumberArgumentNode<N> node) {
        Class<N> type = node.getType();
        if (type.equals(Double.class))
            return (ArgumentType<T>) DoubleArgumentType.doubleArg(node.getMin(), node.getMax());
        else if (type.equals(Float.class))
            return (ArgumentType<T>) FloatArgumentType.floatArg((float) node.getMin(), (float) node.getMax());
        else if (type.equals(Long.class))
            return (ArgumentType<T>) LongArgumentType.longArg((long) node.getMin(), (long) node.getMax());
        else return (ArgumentType<T>) IntegerArgumentType.integer((int) node.getMin(), (int) node.getMax());
    }

}
