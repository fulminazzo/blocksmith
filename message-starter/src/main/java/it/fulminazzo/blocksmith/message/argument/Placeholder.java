package it.fulminazzo.blocksmith.message.argument;

import it.fulminazzo.blocksmith.message.MessageParseContext;
import it.fulminazzo.blocksmith.message.util.ComponentUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a placeholder replacement.
 * A placeholder is defined as a string between two percentage signs:
 * <code>Hello, %name%!</code> -> <code>Hello, Alex!</code>
 */
@Value
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Placeholder implements Argument {
    private static final String PLACEHOLDER_START = "%";
    private static final String PLACEHOLDER_END = "%";

    @NotNull String placeholder;
    @NotNull Component value;

    @Override
    public @NotNull Component apply(final @NotNull MessageParseContext context) {
        Component replacement = value;
        String code = ComponentUtils.toString(replacement);
        Component tmp = context.getMessenger().getComponentOrNull(code, context.getLocale());
        return context.getMessage().replaceText(TextReplacementConfig.builder()
                .matchLiteral(placeholder)
                .replacement(tmp != null ? tmp : replacement)
                .build());
    }

    /**
     * Creates a new Placeholder.
     *
     * @param placeholder the placeholder (without percentage signs)
     * @param value       the value to replace
     * @return the placeholder
     */
    public static @NotNull Placeholder of(final @NotNull String placeholder,
                                          final @NotNull Object value) {
        return of(placeholder, ComponentUtils.toComponent(value.toString()));
    }

    /**
     * Creates a new Placeholder.
     *
     * @param placeholder the placeholder (without percentages)
     * @param value       the value to replace
     * @return the placeholder
     */
    public static @NotNull Placeholder of(final @NotNull Component placeholder,
                                          final @NotNull Object value) {
        return of(placeholder, ComponentUtils.toComponent(value.toString()));
    }

    /**
     * Creates a new Placeholder.
     *
     * @param placeholder the placeholder (without percentages)
     * @param value       the value to replace
     * @return the placeholder
     */
    public static @NotNull Placeholder of(final @NotNull Component placeholder,
                                          final @NotNull Component value) {
        return of(ComponentUtils.toString(placeholder), value);
    }

    /**
     * Creates a new Placeholder.
     *
     * @param placeholder the placeholder (without percentages)
     * @param value       the value to replace
     * @return the placeholder
     */
    public static @NotNull Placeholder of(final @NotNull String placeholder,
                                          final @NotNull Component value) {
        return new Placeholder(formatPlaceholder(placeholder), value);
    }

    /**
     * Formats the given string with the expected placeholders format.
     *
     * @param placeholder the placeholder
     * @return the format
     */
    public static @NotNull String formatPlaceholder(final @NotNull String placeholder) {
        return PLACEHOLDER_START + placeholder + PLACEHOLDER_END;
    }

}
