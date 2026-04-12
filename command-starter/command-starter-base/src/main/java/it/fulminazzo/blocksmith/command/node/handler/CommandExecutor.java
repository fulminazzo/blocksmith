package it.fulminazzo.blocksmith.command.node.handler;

import it.fulminazzo.blocksmith.command.CommandSenderWrapper;
import it.fulminazzo.blocksmith.command.visitor.execution.CommandExecutionException;
import it.fulminazzo.blocksmith.command.visitor.execution.ExecutionContext;
import it.fulminazzo.blocksmith.message.argument.Placeholder;
import it.fulminazzo.blocksmith.reflect.Reflect;
import it.fulminazzo.blocksmith.reflect.ReflectException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.LinkedList;

/**
 * The actual executor of a command.
 */
@Value
@AllArgsConstructor(access = AccessLevel.NONE)
public class CommandExecutor {
    @NotNull Reflect executor;
    @NotNull Method method;

    /**
     * Instantiates a new Command executor.
     *
     * @param executor the actual executor of the function
     * @param method   the function containing the command logic
     */
    public CommandExecutor(final @NotNull Object executor, final @NotNull Method method) {
        this.executor = Reflect.on(executor);
        this.method = method;
    }

    /**
     * Executes the actual command logic.
     *
     * @param context the context of execution
     * @throws CommandExecutionException in case of any errors
     */
    public void execute(final @NotNull ExecutionContext context) throws CommandExecutionException {
        final CommandSenderWrapper<?> sender = context.getCommandSender();
        try {
            LinkedList<Object> arguments = context.getArguments();
            if (arguments.size() != method.getParameterCount()) {
                Class<?> parameterType = method.getParameterTypes()[0];
                if (CommandSenderWrapper.class.isAssignableFrom(parameterType)) {
                    ParameterizedType paramType = (ParameterizedType) method.getGenericParameterTypes()[0];
                    Type actualSenderType = paramType.getActualTypeArguments()[0];
                    if (!sender.extendsType(actualSenderType))
                        throw new CommandExecutionException(sender.isPlayer()
                                ? "error.player-cannot-execute"
                                : "error.console-cannot-execute"
                        );
                    arguments.addFirst(sender);
                } else if (sender.extendsType(parameterType)) arguments.addFirst(sender.getActualSender());
                else throw new CommandExecutionException(sender.isPlayer()
                            ? "error.player-cannot-execute"
                            : "error.console-cannot-execute"
                    );
            }
            Object[] parameterValues = arguments.toArray();
            //TODO: parameters validation
            executor.invoke(method, parameterValues);
        } catch (ReflectException e) {
            Throwable cause = e.getCause();
            if (cause instanceof CommandExecutionException) throw (CommandExecutionException) cause;
            throw new CommandExecutionException("error.internal-error", cause)
                    .arguments(Placeholder.of("message", cause.getMessage()));
        }
    }

}
