package it.fulminazzo.blocksmith.command;

import org.bukkit.Server;
import org.jetbrains.annotations.NotNull;
import org.joor.Reflect;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * A collection of utilities to work with NMS.
 */
final class NMSUtils {
    private static final String COMMAND_DISPATCHER_CLASS = "com.mojang.brigadier.CommandDispatcher";

    /**
     * Gets the Brigadier command dispatcher from the given server.
     *
     * @param server the server
     * @return the command dispatcher (if found)
     */
    public static @NotNull Optional<?> getCommandDispatcher(final @NotNull Server server) {
        Object handle = Reflect.on(server).call("getHandle").get();
        Object dedicatedServer = getGeneralFieldOrReturnSelf(handle, "DedicatedServer");
        Object commands = getGeneralFieldOrReturnSelf(dedicatedServer, "Commands");
        Object commandDispatcher = getCommandDispatcher(commands);
        if (commandDispatcher.getClass().getCanonicalName().equals(COMMAND_DISPATCHER_CLASS))
            return Optional.of(commandDispatcher);
        else return Optional.empty();
    }

    private static @NotNull Object getCommandDispatcher(final @NotNull Object commands) {
        Object commandDispatcher = getGeneralFieldOrReturnSelf(commands, "CommandDispatcher");
        return Arrays.stream(commandDispatcher.getClass().getDeclaredFields())
                .filter(f -> f.getType().getSimpleName().equals("CommandDispatcher"))
                .findFirst()
                .map(f -> Reflect.on(commandDispatcher).field(f.getName()).get())
                .orElse(commandDispatcher);
    }

    private static @NotNull Object getGeneralFieldOrReturnSelf(final @NotNull Object object,
                                                               final @NotNull String returnTypeName) {
        return findMethod(object.getClass(), r -> r.getSimpleName().equals(returnTypeName))
                .map(m -> Reflect.on(object).call(m.getName()).get())
                .orElse(object);
    }

    /**
     * Attempts to find a method from the given type that matches the given predicate.
     *
     * @param type            the Java class where the method is declared
     * @param returnTypeCheck the predicate to check the return type
     * @return the method (if found)
     */
    static @NotNull Optional<Method> findMethod(@NotNull Class<?> type,
                                                final @NotNull Predicate<Class<?>> returnTypeCheck) {
        while (!type.equals(Object.class)) {
            for (Method method : type.getDeclaredMethods()) {
                if (returnTypeCheck.test(method.getReturnType()))
                    return Optional.of(method);
            }
            type = type.getSuperclass();
        }
        return Optional.empty();
    }

}
