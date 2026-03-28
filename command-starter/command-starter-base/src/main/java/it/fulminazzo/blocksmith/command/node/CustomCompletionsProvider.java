package it.fulminazzo.blocksmith.command.node;

import it.fulminazzo.blocksmith.command.execution.CommandExecutionException;
import it.fulminazzo.blocksmith.util.ReflectionUtils;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
    public @NotNull List<String> getCompletions() throws CommandExecutionException {
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

}
