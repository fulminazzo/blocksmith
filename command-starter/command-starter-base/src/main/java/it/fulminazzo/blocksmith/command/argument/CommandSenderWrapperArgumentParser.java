package it.fulminazzo.blocksmith.command.argument;

import it.fulminazzo.blocksmith.command.CommandSenderWrapper;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;

/**
 * An {@link ArgumentParser} that wraps a {@link CommandSenderWrapper} around the actual command sender type.
 *
 * @param <S> the type of the command sender
 */
public final class CommandSenderWrapperArgumentParser<S> extends DelegateArgumentParser<S, CommandSenderWrapper<S>> {

    /**
     * Instantiates a new Command sender wrapper parser.
     *
     * @param genericCommandSenderType the Java class of a generic command sender
     */
    @SuppressWarnings("unchecked")
    public CommandSenderWrapperArgumentParser(final @NotNull Type genericCommandSenderType) {
        super(
                (v, o) -> (CommandSenderWrapper<S>) v.getApplication().getCommandRegistry().wrapSender(o),
                genericCommandSenderType
        );
    }

}
