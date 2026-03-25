package it.fulminazzo.blocksmith.message.argument;

import it.fulminazzo.blocksmith.message.MessageParseContext;
import it.fulminazzo.blocksmith.message.argument.time.node.TimeNode;
import it.fulminazzo.blocksmith.message.argument.time.parser.TimeParser;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * Represents a timed placeholder replacement.
 * Replaces the given placeholder with the time based on the supplier.
 */
public final class Time implements Argument {
    private final @NotNull TimeNode timeNode;

    private final @NotNull String placeholder;
    private final @NotNull Supplier<Long> timeSupplier;

    private Time(final @NotNull String placeholder,
                 final @NotNull String format,
                 final @NotNull Supplier<Long> timeSupplier) {
        TimeParser parser = new TimeParser(format);
        this.timeNode = parser.parse();

        this.placeholder = placeholder;
        this.timeSupplier = timeSupplier;
    }

    @Override
    public @NotNull Component apply(final @NotNull MessageParseContext context) {
        long time = timeSupplier.get();
        String replacement = timeNode.parse(time);
        return Placeholder.of(placeholder, replacement).apply(context);
    }

}
