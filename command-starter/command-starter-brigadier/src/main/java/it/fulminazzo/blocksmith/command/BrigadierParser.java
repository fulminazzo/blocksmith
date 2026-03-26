package it.fulminazzo.blocksmith.command;

import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import it.fulminazzo.blocksmith.command.node.ArgumentNode;
import it.fulminazzo.blocksmith.command.node.CommandNode;
import it.fulminazzo.blocksmith.command.node.LiteralNode;
import it.fulminazzo.blocksmith.command.node.NumberArgumentNode;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A parser for converting Blocksmith commands into Brigadiers.
 *
 * @param <S> the type of the command sender (for Brigadier)
 */
@RequiredArgsConstructor
@SuppressWarnings("unchecked")
final class BrigadierParser<S> {

    /**
     * Converts the given Blocksmith command node to a Brigadier command node.
     *
     * @param node the blocksmith command node
     * @return the brigadier command node
     */
    public @NotNull com.mojang.brigadier.tree.CommandNode<S> parse(final @NotNull CommandNode node) {
        return parseChildren(LiteralArgumentBuilder.literal(node.getName()), node).build();
    }

    /**
     * Appends all the children of the node to the given builder.
     *
     * @param builder the brigadier builder
     * @param node    the blocksmith node
     * @return the brigadier builder itself
     */
    @NotNull ArgumentBuilder<S, ?> parseChildren(final @NotNull ArgumentBuilder<S, ?> builder,
                                                 final @NotNull CommandNode node) {
        node.getChildren().forEach(n -> parseChild(builder, n));
        return builder;
    }

    /**
     * Appends to the given builder the node (converted to a Brigadier command node).
     *
     * @param builder the brigadier builder
     * @param node    the blocksmith node
     */
    void parseChild(final @NotNull ArgumentBuilder<S, ?> builder,
                    final @NotNull CommandNode node) {
        if (node instanceof LiteralNode) parseChild(builder, (LiteralNode) node);
        else parseChild(builder, (ArgumentNode<?>) node);
    }

    /**
     * Appends to the given builder the literal node (converted to a Brigadier command node).
     *
     * @param builder the brigadier builder
     * @param node    the blocksmith node
     */
    void parseChild(final @NotNull ArgumentBuilder<S, ?> builder,
                    final @NotNull LiteralNode node) {
        node.getAliases().forEach(a ->
                //TODO: execute
                builder.then(parseChildren(LiteralArgumentBuilder.literal(a), node))
        );
    }

    /**
     * Appends to the given builder the argument node (converted to a Brigadier command node).
     *
     * @param <T>     the type of the derived {@link ArgumentType}
     * @param builder the brigadier builder
     * @param node    the blocksmith node
     */
    <T> void parseChild(final @NotNull ArgumentBuilder<S, ?> builder,
                        final @NotNull ArgumentNode<?> node) {
        ArgumentType<T> type = getArgumentType(node);
        final RequiredArgumentBuilder<S, T> argumentBuilder;
        if (type != null) argumentBuilder = RequiredArgumentBuilder.argument(node.getName(), type);
        else
            argumentBuilder = RequiredArgumentBuilder.<S, T>argument(node.getName(), (ArgumentType<T>) StringArgumentType.string())
                    .suggests(((c, b) -> {
                        //TODO: parse completions
                        return b.buildFuture();
                    }));
        //TODO: execute
        builder.then(parseChildren(argumentBuilder, node));
    }

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
