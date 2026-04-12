//TODO: update
//package it.fulminazzo.blocksmith.command.parser;
//
//import lombok.Getter;
//import lombok.RequiredArgsConstructor;
//import org.jetbrains.annotations.NotNull;
//import org.jetbrains.annotations.Nullable;
//
///**
// * Identifies all the allowed tokens of a command.
// */
//@RequiredArgsConstructor
//enum CommandToken {
//    OPEN_BRACKET("\\["),
//    CLOSE_BRACKET("\\]"),
//
//    OPEN_PHARENTHESIS("\\("),
//    CLOSE_PARENTHESIS("\\)"),
//
//    LOWER_THAN("<"),
//    GREATER_THAN(">"),
//
//    PIPE("\\|"),
//
//    SPACE("( |\r|\n|\t)+", "space"),
//
//    LITERAL("[A-Za-z_?!/][A-Za-z0-9_?!/]*", "text"),
//    EOF("");
//
//    private final @NotNull String regex;
//    @Getter
//    private final @NotNull String token;
//
//    CommandToken(final @NotNull String regex) {
//        this(regex, regex.replace("\\", ""));
//    }
//
//    /**
//     * Checks if the current token matches with the given one.
//     *
//     * @param token the token
//     * @return <code>true</code> if it does
//     */
//    public boolean matches(final @NotNull String token) {
//        return token.matches(regex);
//    }
//
//    /**
//     * Attempts to get the best token from the given raw string.
//     *
//     * @param raw the string
//     * @return the token (or <code>null</code> if not found)
//     */
//    public static @Nullable CommandToken getToken(final @NotNull String raw) {
//        for (CommandToken commandToken : values()) {
//            if (commandToken != EOF && commandToken.matches(raw)) return commandToken;
//        }
//        return null;
//    }
//
//}
