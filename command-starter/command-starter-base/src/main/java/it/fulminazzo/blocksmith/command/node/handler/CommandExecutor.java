package it.fulminazzo.blocksmith.command.node.handler;

import it.fulminazzo.blocksmith.command.CommandMessages;
import it.fulminazzo.blocksmith.command.CommandSenderWrapper;
import it.fulminazzo.blocksmith.command.visitor.execution.CommandExecutionException;
import it.fulminazzo.blocksmith.command.visitor.execution.CommandExecutionVisitor;
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
import java.util.List;
import java.util.Optional;

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
     * @param executionVisitor the execution visitor
     * @throws CommandExecutionException in case of any errors
     */
    public void execute(final @NotNull CommandExecutionVisitor executionVisitor) throws CommandExecutionException {
        final CommandSenderWrapper<?> sender = executionVisitor.getCommandSender();
        try {
            List<?> arguments = getArguments(executionVisitor, sender);
            executor.invoke(method, arguments.toArray());
        } catch (ReflectException e) {
            Throwable cause = e.getCause();
            if (cause instanceof CommandExecutionException) throw (CommandExecutionException) cause;
            String message = cause.getMessage();
            if (message == null) message = "Unknown";
            throw new CommandExecutionException(CommandMessages.INTERNAL_ERROR, cause)
                    .arguments(Placeholder.of("message", message));
        } catch (Exception e) {
            if (e instanceof CommandExecutionException) throw e;
            else throw new CommandExecutionException(CommandMessages.INTERNAL_ERROR, e)
                    .arguments(Placeholder.of("message", e.getMessage()));
        }
    }

    private List<?> getArguments(final @NotNull CommandExecutionVisitor executionVisitor,
                                 final @NotNull CommandSenderWrapper<?> sender) throws CommandExecutionException {
        LinkedList<Object> arguments = executionVisitor.getArguments();
        if (arguments.size() != method.getParameterCount()) {
            Optional<?> parsedSender = parseCommandSenderParameter(method, sender);
            if (parsedSender.isPresent()) arguments.addFirst(parsedSender.get());
            else throw new CommandExecutionException(sender.isPlayer()
                    ? CommandMessages.PLAYER_CANNOT_EXECUTE
                    : CommandMessages.CONSOLE_CANNOT_EXECUTE
            );
        }
        return arguments;
    }

    /**
     * Parses the command sender parameter for the given method.
     * <br>
     * Assuming the method has a first parameter representing the executor of the command,
     * the parameter type will be checked to return the most appropriate representation
     * of the sender (namely the {@link CommandSenderWrapper} or the sender itself).
     *
     * @param method the method to check
     * @param sender the command sender
     * @return the parsed sender (or {@code null} if the sender is not recognized from the parameter type)
     */
    static @NotNull Optional<Object> parseCommandSenderParameter(final @NotNull Method method,
                                                                 final @NotNull CommandSenderWrapper<?> sender) {
        Class<?> parameterType = method.getParameterTypes()[0];
        if (CommandSenderWrapper.class.isAssignableFrom(parameterType)) {
            Type senderType = method.getGenericParameterTypes()[0];
            if (senderType instanceof ParameterizedType) {
                ParameterizedType paramType = (ParameterizedType) senderType;
                Type actualSenderType = paramType.getActualTypeArguments()[0];
                if (!sender.extendsType(actualSenderType)) return Optional.empty();
            }
            return Optional.of(sender);
        } else if (sender.extendsType(parameterType)) return Optional.of(sender.handle());
        else return Optional.empty();
    }

}
