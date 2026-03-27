package it.fulminazzo.blocksmith.command.node;

import it.fulminazzo.blocksmith.command.CommandSenderWrapper;
import it.fulminazzo.blocksmith.command.execution.CommandExecutionContext;
import it.fulminazzo.blocksmith.command.execution.CommandExecutionException;
import it.fulminazzo.blocksmith.message.argument.Placeholder;
import it.fulminazzo.blocksmith.util.ReflectionUtils;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.executable.ExecutableValidator;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Set;

/**
 * Contains information about the actual execution of a command.
 */
@Value
@Getter(AccessLevel.NONE)
public class ExecutionInfo {
    private static final ExecutableValidator validator;

    static {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator().forExecutables();
        }
    }

    @NotNull Object executor;
    @Getter
    @NotNull Method method;

    /**
     * Invokes the internal method.
     *
     * @param context the context of invocation
     * @throws CommandExecutionException in case of any errors
     */
    public void invoke(final @NotNull CommandExecutionContext context) throws CommandExecutionException {
        final CommandSenderWrapper sender = context.getCommandSender();
        try {
            LinkedList<Object> arguments = context.getArguments();
            if (arguments.size() != method.getParameterCount()) {
                Class<?> parameterType = method.getParameterTypes()[0];
                if (parameterType.equals(CommandSenderWrapper.class))
                    arguments.addFirst(sender);
                else if (sender.extendsType(parameterType)) arguments.addFirst(sender.getActualSender());
                else throw new CommandExecutionException(sender.isPlayer()
                            ? "error.player-cannot-execute"
                            : "error.console-cannot-execute"
                    );
            }
            Object[] parameterValues = arguments.toArray();
            validateParameters(executor, method, parameterValues);
            method.invoke(executor, parameterValues);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof CommandExecutionException) throw (CommandExecutionException) cause;
            throw new CommandExecutionException("error.internal-error", cause)
                    .arguments(Placeholder.of("message", cause.getMessage()));
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException(String.format("Method %s#%s should be declared public",
                    method.getDeclaringClass().getCanonicalName(),
                    ReflectionUtils.methodToString(method)
            ));
        }
    }

    private static void validateParameters(final @NotNull Object executor,
                                           final @NotNull Method method,
                                           final @NotNull Object[] parameterValues) throws CommandExecutionException {
        Set<ConstraintViolation<Object>> violations = validator.validateParameters(executor, method, parameterValues);
        Optional<ConstraintViolation<Object>> first = violations.stream().findFirst();
        if (first.isPresent()) {
            ConstraintViolation<Object> violation = first.get();
            throw new CommandExecutionException(violation.getMessage())
                    .arguments(Placeholder.of("argument", violation.getInvalidValue()));
        }
    }

}
