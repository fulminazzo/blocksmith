package it.fulminazzo.blocksmith.command.node.handler;

import it.fulminazzo.blocksmith.command.visitor.InputVisitor;
import it.fulminazzo.blocksmith.reflect.Reflect;
import it.fulminazzo.blocksmith.reflect.ReflectException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * A {@link Supplier} for customized completions in {@link it.fulminazzo.blocksmith.command.node.ArgumentNode}s.
 */
@Value
@AllArgsConstructor(access = AccessLevel.NONE)
public class CompletionsSupplier {
    @NotNull Reflect executor;
    @NotNull Method method;

    /**
     * Instantiates a new Completions supplier
     *
     * @param executor the actual executor of the function
     * @param method   the function that will return the completions
     */
    public CompletionsSupplier(final @NotNull Object executor, final @NotNull Method method) {
        this.executor = Reflect.on(executor);
        this.method = method;
    }

    /**
     * Gets the completions.
     *
     * @param visitor the visitor requesting the completions
     * @return the completions
     */
    public @NotNull List<String> get(final @NotNull InputVisitor<?, ?> visitor) {
        return getUnquoted(visitor).stream()
                .map(s -> s.contains(" ") ? "\"" + s + "\"" : s)
                .collect(Collectors.toList());
    }

    /**
     * Gets the completions.
     * If a completion contains a white space, it will not be quoted.
     *
     * @param visitor the visitor requesting the completions
     * @return the completions
     */
    public @NotNull List<String> getUnquoted(final @NotNull InputVisitor<?, ?> visitor) {
        final Collection<?> completions;
        if (method.getParameterCount() == 0) completions = executor.invoke(method).get();
        else {
            Optional<?> sender = CommandExecutor.parseCommandSenderParameter(method, visitor.getCommandSender());
            if (sender.isPresent()) completions = executor.invoke(method, sender.get()).get();
            else completions = Collections.emptyList();
        }
        return completions.stream()
                .map(o -> o == null ? "null" : o.toString())
                .collect(Collectors.toList());
    }

    /**
     * Creates a new completions supplier from the given method declaration.
     * If the method declaration is relative (no dots), the method will be searched in the
     * requester class. If the method declaration is absolute (contains dots), the method will
     * be searched in the specified class (and must be static).
     *
     * @param requester         the requester (can be a class or an instance)
     * @param methodDeclaration the method declaration (e.g. "getCompletions")
     * @return the supplier
     */
    public static @NotNull CompletionsSupplier of(@NotNull Object requester,
                                                  @NotNull String methodDeclaration) {
        Reflect reflect;
        if (methodDeclaration.contains(".")) {
            int index = methodDeclaration.lastIndexOf('.');
            String className = methodDeclaration.substring(0, index);
            methodDeclaration = methodDeclaration.substring(index + 1);
            reflect = Reflect.on(className);
            requester = reflect.get();
        } else reflect = Reflect.on(requester);
        final Method method;
        try {
            @NotNull String methodName = methodDeclaration;
            method = reflect.getMethod(m -> m.getName().equals(methodName) && m.getParameterCount() < 2);
        } catch (ReflectException e) {
            throw new ReflectException("Could not find method ? %s() in type '%s'", methodDeclaration, reflect.getType());
        }
        if (reflect.extendsType(Type.class)) {
            if (!Modifier.isStatic(method.getModifiers()))
                throw new ReflectException("Invalid method %s in type %s: " +
                        "completions functions with class executor must be static",
                        method, reflect.getType()
                );
        } else {
            if (Modifier.isStatic(method.getModifiers())) {
                reflect = Reflect.on(reflect.getType());
                requester = reflect.get();
            }
        }
        if (Collection.class.isAssignableFrom(method.getReturnType())) {
            return new CompletionsSupplier(requester, method);
        } else
            throw new ReflectException("Invalid method %s in type %s: " +
                    "must return an instance of %s",
                    method, reflect.getType(), Collection.class
            );
    }

}
