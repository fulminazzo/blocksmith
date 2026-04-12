package it.fulminazzo.blocksmith.command.argument;

import it.fulminazzo.blocksmith.command.execution.CommandExecutionContext;
import it.fulminazzo.blocksmith.command.execution.CommandExecutionException;
import it.fulminazzo.blocksmith.message.argument.Placeholder;
import it.fulminazzo.blocksmith.reflect.Reflect;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A dynamic {@link ArgumentParser} for enum types.
 * Requires the enum type to be passed for each initialization.
 *
 * @param <E> the type of the enum
 */
final class EnumArgumentParser<E extends Enum<E>> implements ArgumentParser<E> {
    private final @NotNull String enumTypeName;
    private final @NotNull Reflect reflect;

    /**
     * Instantiates a new Enum argument parser.
     *
     * @param type the Java class of the enum
     */
    public EnumArgumentParser(final @NotNull Class<E> type) {
        this.enumTypeName = type.getSimpleName();
        this.reflect = Reflect.on(type);
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NotNull E parse(final @NotNull CommandExecutionContext context) throws CommandExecutionException {
        final String rawArgument = context.getCurrent();
        return (E) reflect.valueOf(rawArgument).orElseThrow(() -> new CommandExecutionException("error.enum-not-found")
                .arguments(
                        Placeholder.of("argument", rawArgument),
                        Placeholder.of("name", enumTypeName)
                ));
    }

    @Override
    public @NotNull List<String> getCompletions(final @NotNull CommandExecutionContext context) {
        return reflect.values()
                .stream()
                .map(Enum::name)
                .map(String::toLowerCase)
                .collect(Collectors.toList());
    }

}
