package it.fulminazzo.blocksmith.command.node;

import it.fulminazzo.blocksmith.util.ReflectionUtils;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

/**
 * A provider of custom tab completions for a command.
 */
@Value
public class CustomCompletionsProvider {
    @NotNull Object executor;
    @NotNull Method method;

    /**
     * Gets the completions.
     *
     * @return the completions
     */
    public @NotNull List<String> getCompletions() {
        try {
            Collection<?> completions = (Collection<?>) method.invoke(executor);
            return completions.stream()
                    .map(o -> o == null ? "null" : o.toString())
                    .collect(Collectors.toList());
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) throw (RuntimeException) cause;
            else throw new CompletionException(cause);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException(String.format("Method %s#%s must be declared public",
                    method.getDeclaringClass().getCanonicalName(),
                    ReflectionUtils.methodToString(method)
            ));
        }
    }

    /**
     * Creates a new custom completions provider from the given method declaration.
     * If the method declaration is relative (no dots), the method will be searched in the
     * requester class. If the method declaration is absolute (contains dots), the method will
     * be searched in the specified class (and must be static).
     *
     * @param requester         the requester (can be a class or an instance)
     * @param methodDeclaration the method declaration (e.g. "getCompletions")
     * @return the custom completions provider
     */
    public static @NotNull CustomCompletionsProvider of(@NotNull Object requester,
                                                        @NotNull String methodDeclaration) {
        if (methodDeclaration.contains(".")) {
            int index = methodDeclaration.lastIndexOf('.');
            String className = methodDeclaration.substring(0, index);
            methodDeclaration = methodDeclaration.substring(index + 1);
            try {
                requester = Class.forName(className);
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException(String.format("Could not find class '%s'", className));
            }
        }
        final Method method;
        Class<?> type = null;
        try {
            if (requester instanceof Class) {
                type = (Class<?>) requester;
                method = type.getMethod(methodDeclaration);
                if (!Modifier.isStatic(method.getModifiers()))
                    throw new IllegalArgumentException(String.format("Invalid method '%s' in type '%s': " +
                            "completions functions with class executor must be static", methodDeclaration, type.getCanonicalName())
                    );
            } else {
                type = requester.getClass();
                method = type.getMethod(methodDeclaration);
                if (Modifier.isStatic(method.getModifiers()))
                    requester = type;
            }
            if (Collection.class.isAssignableFrom(method.getReturnType())) {
                return new CustomCompletionsProvider(requester, method);
            } else
                throw new IllegalArgumentException(String.format("Invalid method '%s' in type '%s': must return an instance of %s",
                        method.getName(), type.getCanonicalName(), Collection.class.getCanonicalName())
                );
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(String.format("Could not find method '%s' in class '%s'",
                    methodDeclaration, type.getCanonicalName()));
        }
    }

}
