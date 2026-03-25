package it.fulminazzo.blocksmith.command.argument;

import it.fulminazzo.blocksmith.command.execution.CommandExecutionContext;
import it.fulminazzo.blocksmith.command.execution.CommandExecutionException;
import it.fulminazzo.blocksmith.message.argument.Placeholder;
import org.jetbrains.annotations.NotNull;
import org.joor.Reflect;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A dynamic {@link ArgumentParser} for enum types.
 * Requires the enum type to be passed for each initialization.
 *
 * @param <E> the type of the enum
 */
final class EnumArgumentParser<E extends Enum<E>> implements ArgumentParser<E> {
    private final @NotNull String enumTypeName;
    private final @NotNull Set<E> values;

    /**
     * Instantiates a new Enum argument parser.
     *
     * @param type the Java class of the enum
     */
    public EnumArgumentParser(final @NotNull Class<E> type) {
        this.enumTypeName = type.getSimpleName();
        E[] values = Reflect.onClass(type).call("values").get();
        this.values = new HashSet<>(Arrays.asList(values));
    }

    @Override
    public @NotNull E parse(final @NotNull CommandExecutionContext context) throws CommandExecutionException {
        final String rawArgument = context.getCurrent();
        return values.stream()
                .filter(v -> v.name().equalsIgnoreCase(rawArgument))
                .findFirst().orElseThrow(() -> new CommandExecutionException("error.invalid-enum")
                        .arguments(
                                Placeholder.of("argument", rawArgument),
                                Placeholder.of("name", enumTypeName)
                        ));
    }

    @Override
    public @NotNull List<String> getCompletions(final @NotNull CommandExecutionContext context) {
        return values.stream()
                .map(Enum::name)
                .map(String::toLowerCase)
                .collect(Collectors.toList());
    }

}
