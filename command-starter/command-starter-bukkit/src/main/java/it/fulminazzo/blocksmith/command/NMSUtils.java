package it.fulminazzo.blocksmith.command;

import it.fulminazzo.blocksmith.reflect.Reflect;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A collection of utilities to work with NMS.
 */
final class NMSUtils {
    private static final @NotNull String COMMAND_DISPATCHER_CLASS = "com.mojang.brigadier.CommandDispatcher";

    private static final @NotNull Pattern serverVersionRegex = Pattern.compile("[0-9]+\\.([0-9]+)(?:\\.([0-9]+))?-R[0-9]+\\.[0-9]+-SNAPSHOT");

    /**
     * Gets the Brigadier command dispatcher from the given server.
     *
     * @param server the server
     * @return the command dispatcher (if found)
     */
    public static @NotNull Optional<?> getCommandDispatcher(final @NotNull Server server) {
        Object handle = Reflect.on(server).invoke("getHandle").get();
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
                .map(f -> Reflect.on(commandDispatcher).get(f.getName()).get())
                .orElse(commandDispatcher);
    }

    private static @NotNull Object getGeneralFieldOrReturnSelf(final @NotNull Object object,
                                                               final @NotNull String returnTypeName) {
        return findMethod(object.getClass(), r -> r.getSimpleName().equals(returnTypeName))
                .map(m -> Reflect.on(object).invoke(m.getName()).get())
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

    /**
     * Returns the current server version in a double format.
     * If the server version is "1.X.Y", the number returned is "X.Y".
     *
     * @return the version
     */
    public static double getServerVersion() {
        String version = Bukkit.getBukkitVersion();
        Matcher matcher = serverVersionRegex.matcher(version);
        if (matcher.matches()) {
            String first = matcher.group(1);
            String second = matcher.group(2);
            if (second == null) second = "0";
            return Double.parseDouble(first + "." + second);
        } else throw new IllegalStateException("Could not find numeric version from server version: " + version);
    }

    /**
     * Gets the NMS version of the current version.
     * <br>
     * <b>WARNING</b>: not supported in versions higher than 1.20.
     *
     * @return the version
     */
    public static @NotNull String getNMSVersion() {
        Class<? extends Server> serverClass = Bukkit.getServer().getClass();
        String version = serverClass.getPackage().getName();
        version = version.substring(version.lastIndexOf('.') + 1);
        if (version.equals("craftbukkit"))
            throw new IllegalStateException("Could not find the NMS version from the current server class: " + serverClass.getSimpleName() + ". " +
                    "Are you on a version higher than 1.20?");
        return version;
    }

}
