package it.fulminazzo.blocksmith.message.argument.time;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Identifies all the allowed tokens of a time format.
 */
@RequiredArgsConstructor
enum TimeToken {
    OPEN_PHARENTHESIS("\\("),
    CLOSE_PARENTHESIS("\\)"),

    OPEN_BRACKET("\\["),
    CLOSE_BRACKET("\\]"),

    OPEN_BRACE("\\{"),
    CLOSE_BRACE("\\}"),

    PIPE("\\|"),
    PERCENTAGE("%"),

    LITERAL("[A-Za-z_][A-Za-z0-9_]*", "text"),
    ANYTHING_ELSE("[^A-Za-z0-9_*()\\[\\]{}|%]+", ""),
    EOF("");

    private final @NotNull String regex;
    @Getter
    private final @NotNull String token;

    TimeToken(final @NotNull String regex) {
        this(regex, regex.replace("\\", ""));
    }

    /**
     * Checks if the current token matches with the given one.
     *
     * @param token the token
     * @return <code>true</code> if it does
     */
    public boolean matches(final @NotNull String token) {
        return token.matches(regex);
    }

    /**
     * Attempts to get the best token from the given raw string.
     *
     * @param raw the string
     * @return the token (or <code>null</code> if not found)
     */
    public static @Nullable TimeToken getToken(final @NotNull String raw) {
        for (TimeToken commandToken : values()) {
            if (commandToken != EOF && commandToken.matches(raw)) return commandToken;
        }
        return null;
    }

}