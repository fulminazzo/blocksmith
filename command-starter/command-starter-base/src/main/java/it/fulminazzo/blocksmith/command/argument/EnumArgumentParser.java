package it.fulminazzo.blocksmith.command.argument;

import it.fulminazzo.blocksmith.command.CommandMessages;
import it.fulminazzo.blocksmith.command.visitor.InputVisitor;
import it.fulminazzo.blocksmith.message.argument.Placeholder;
import it.fulminazzo.blocksmith.reflect.Reflect;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * A dynamic {@link ArgumentParser} for enum types.
 * Requires the enum type to be passed for each initialization.
 *
 * @param <E> the type of the enum
 */
final class EnumArgumentParser<E extends Enum<E>> implements ArgumentParser<E> {
    private final @NotNull String enumTypeName;
    private final @NotNull Map<String, E> values;

    /**
     * Instantiates a new Enum argument parser.
     *
     * @param type the Java class of the enum
     */
    @SuppressWarnings("unchecked")
    public EnumArgumentParser(final @NotNull Class<E> type) {
        this.enumTypeName = type.getSimpleName();
        this.values = new TreeMap<>();
        Reflect.on(type).values().forEach(e -> values.put(e.name().toLowerCase(), (E) e));
    }

    @Override
    public @NotNull E parse(final @NotNull InputVisitor<?, ?> visitor) throws ArgumentParseException {
        final String rawArgument = visitor.getInput().getCurrent();
        E value = values.get(rawArgument.toLowerCase());
        if (value == null)
            throw new ArgumentParseException(CommandMessages.INVALID_ENUM)
                    .arguments(
                            Placeholder.of(CommandMessages.ARGUMENT_PLACEHOLDER, rawArgument),
                            Placeholder.of("name", enumTypeName)
                    );
        else return value;
    }

    @Override
    public @NotNull List<String> getCompletions(final @NotNull InputVisitor<?, ?> visitor) {
        return new ArrayList<>(values.keySet());
    }

}
