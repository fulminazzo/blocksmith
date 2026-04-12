package it.fulminazzo.blocksmith.command.node.handler;

import it.fulminazzo.blocksmith.reflect.Reflect;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;

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

    public void execute() {
        //TODO: implement
    }

}
