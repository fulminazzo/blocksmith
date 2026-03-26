package it.fulminazzo.blocksmith.command;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import it.fulminazzo.blocksmith.command.node.ArgumentNode;
import it.fulminazzo.blocksmith.util.ReflectionUtils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Holds all the default Brigadier ArgumentTypes.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ArgumentTypes {
    private static final @NotNull Map<Class<?>, ArgumentType<?>> types = new HashMap<>();

    /**
     * Attempts to get an ArgumentType from the given node.
     * Throws {@link IllegalArgumentException} if no type is found.
     *
     * @param <T>  the type of the argument (must be known to the client)
     * @param <A>  the Java type
     * @param node the argument node
     * @return the argument type
     */
    @SuppressWarnings({"unchecked"})
    public static <T, A> @NotNull ArgumentType<T> of(final @NotNull ArgumentNode<A> node) {
        if (node.isGreedy()) return (ArgumentType<T>) StringArgumentType.greedyString();
        Class<A> type = node.getType();
        if (Enum.class.isAssignableFrom(type))
            throw new UnsupportedOperationException();
        ArgumentType<?> argumentType = types.get(ReflectionUtils.toWrapper(type));
        if (argumentType == null)
            throw new IllegalArgumentException(String.format("No default Brigadier argument type supports the type %s. " +
                    "Please provide a custom type through %s#register", type.getCanonicalName(), ArgumentTypes.class.getSimpleName()));
        return (ArgumentType<T>) argumentType;
    }

    /**
     * Associates a new ArgumentType to the given Java class.
     * <br>
     * <b>WARNING</b>: because of Minecraft limitations, the argument type
     * must contain only classes that the client knows.
     * This means that any platform-related class or custom class
     * must be avoided (this is also the reason why the type of the argument
     * is different from the actual Java type).
     *
     * @param <T>          the type of the argument (must be known to the client)
     * @param <A>          the Java type
     * @param type         the Java class of the argument
     * @param argumentType the argument type
     */
    public static <T, A> void register(final @NotNull Class<A> type,
                                       final @NotNull ArgumentType<T> argumentType) {
        types.put(type, argumentType);
    }

}
