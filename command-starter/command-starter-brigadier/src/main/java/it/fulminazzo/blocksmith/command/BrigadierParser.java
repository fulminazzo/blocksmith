package it.fulminazzo.blocksmith.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import it.fulminazzo.blocksmith.command.node.*;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

/**
 * A parser for converting Blocksmith commands into Brigadiers.
 *
 * @param <S> the type of the command sender (for Brigadier)
 */
@RequiredArgsConstructor
@SuppressWarnings("unchecked")
final class BrigadierParser<S> {
    private final @NotNull CommandRegistry delegate;

    /**
     * Converts the given Blocksmith command node to a Brigadier command node.
     *
     * @param node the blocksmith command node
     * @return the brigadier command node
     */
    public @NotNull LiteralCommandNode<S> parse(final @NotNull LiteralNode node) {
        LiteralArgumentBuilder<S> builder = LiteralArgumentBuilder.literal(node.getName());
        checkRequiresConfirmation(node, builder, node);
        return parseChildren(node, builder, node).build();
    }

    /**
     * Appends all the children of the node to the given builder.
     *
     * @param <B>     the type of the brigadier builder
     * @param root    the blocksmith node that originated the conversion
     * @param builder the brigadier builder
     * @param node    the blocksmith node
     * @return the brigadier builder itself
     */
    <B extends ArgumentBuilder<S, B>> @NotNull B parseChildren(final @NotNull LiteralNode root,
                                                               final @NotNull B builder,
                                                               final @NotNull CommandNode node) {
        node.getChildren().forEach(n -> parseChild(root, builder, n));
        return builder;
    }

    /**
     * Appends to the given builder the node (converted to a Brigadier command node).
     *
     * @param root    the blocksmith node that originated the conversion
     * @param builder the brigadier builder
     * @param node    the blocksmith node
     */
    void parseChild(final @NotNull LiteralNode root,
                    final @NotNull ArgumentBuilder<S, ?> builder,
                    final @NotNull CommandNode node) {
        if (node instanceof LiteralNode) parseChild(root, builder, (LiteralNode) node);
        else parseChild(root, builder, (ArgumentNode<?>) node);
    }

    /**
     * Appends to the given builder the literal node (converted to a Brigadier command node).
     *
     * @param root    the blocksmith node that originated the conversion
     * @param builder the brigadier builder
     * @param node    the blocksmith node
     */
    void parseChild(final @NotNull LiteralNode root,
                    final @NotNull ArgumentBuilder<S, ?> builder,
                    final @NotNull LiteralNode node) {
        node.getAliases().forEach(a ->
                builder.then(parseChildren(
                        root,
                        LiteralArgumentBuilder.<S>literal(a)
                                .requires(s -> node.getCommandInfo()
                                        .map(CommandInfo::getPermission)
                                        .map(p -> delegate.wrapSender(s).hasPermission(p))
                                        .orElse(true)
                                )
                                .executes(executes(root)),
                        node
                ))
        );
        checkRequiresConfirmation(root, builder, node);
    }

    /**
     * Appends to the given builder the argument node (converted to a Brigadier command node).
     *
     * @param <T>     the type of the derived {@link ArgumentType}
     * @param root    the blocksmith node that originated the conversion
     * @param builder the brigadier builder
     * @param node    the blocksmith node
     */
    <T> void parseChild(final @NotNull LiteralNode root,
                        final @NotNull ArgumentBuilder<S, ?> builder,
                        final @NotNull ArgumentNode<?> node) {
        ArgumentType<T> type = getArgumentType(node);
        final RequiredArgumentBuilder<S, T> argumentBuilder;
        if (type != null) argumentBuilder = RequiredArgumentBuilder.argument(node.getName(), type);
        else
            argumentBuilder = RequiredArgumentBuilder.<S, T>argument(node.getName(), (ArgumentType<T>) StringArgumentType.string())
                    .suggests(((c, b) -> {
                        S source = c.getSource();
                        String input = c.getInput();
                        String[] split = input.split(" ");
                        if (input.endsWith(" ")) {
                            split = Arrays.copyOf(split, split.length + 1);
                            split[split.length - 1] = "";
                        }
                        delegate.tabComplete(
                                root,
                                source,
                                split[0],
                                Arrays.copyOfRange(split, 1, split.length)
                        ).forEach(b::suggest);
                        return b.buildFuture();
                    }));
        builder.then(parseChildren(
                root,
                argumentBuilder.executes(executes(root)),
                node
        ));
    }

    /**
     * Provides the execute function for Brigadier nodes.
     *
     * @param root the blocksmith node that originated the conversion
     * @return the function
     */
    @NotNull Command<S> executes(final @NotNull LiteralNode root) {
        return c -> {
            S source = c.getSource();
            String[] input = c.getInput().split(" ");
            delegate.execute(
                    root,
                    source,
                    input[0],
                    Arrays.copyOfRange(input, 1, input.length)
            );
            return Command.SINGLE_SUCCESS;
        };
    }

    private void checkRequiresConfirmation(final @NotNull LiteralNode root,
                                           final @NotNull ArgumentBuilder<S, ?> builder,
                                           final @NotNull LiteralNode node) {
        if (node.requiresConfirmation())
            builder.then(LiteralArgumentBuilder.<S>literal(node.getConfirmWord()).executes(executes(root)))
                    .then(LiteralArgumentBuilder.<S>literal(node.getCancelWord()).executes(executes(root)));
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
        return (ArgumentType<T>) ArgumentTypes.get(type).orElseGet(() -> {
            if (Number.class.isAssignableFrom(type))
                return getArgumentType((NumberArgumentNode<Number>) node);
            else return null;
        });
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
