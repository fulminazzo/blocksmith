package it.fulminazzo.blocksmith.message.argument;

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
    public @NotNull Component apply(final @NotNull Component component) {
        return component.replaceText(TextReplacementConfig.builder()
                .matchLiteral(placeholder)
                .replacement(value)
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
     * @param placeholder the placeholder (without ampersands)
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
     * @param placeholder the placeholder (without ampersands)
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
     * @param placeholder the placeholder (without ampersands)
     * @param value       the value to replace
     * @return the placeholder
     */
    public static @NotNull Placeholder of(final @NotNull String placeholder,
                                          final @NotNull Component value) {
        return new Placeholder(PLACEHOLDER_START + placeholder + PLACEHOLDER_END, value);
    }

}
