package it.fulminazzo.blocksmith.command.parser;

import it.fulminazzo.blocksmith.command.node.CommandInfo;
import it.fulminazzo.blocksmith.command.node.ExecutionInfo;
import org.jetbrains.annotations.NotNull;

/**
 * A parser to read raw commands declarations and transform them into {@link CommandNode}.
 */
public final class CommandParser {
    private final @NotNull String rawCommand;
    private final @NotNull CommandTokenizer tokenizer;
    private final @NotNull CommandInfo commandInfo;
    private final @NotNull ExecutionInfo executionInfo;

    /**
     * Instantiates a new Command parser.
     *
     * @param command       the command
     * @param commandInfo   the command info
     * @param executionInfo the execution info
     */
    public CommandParser(final @NotNull String command,
                         final @NotNull CommandInfo commandInfo,
                         final @NotNull ExecutionInfo executionInfo) {
        this.rawCommand = command;
        this.tokenizer = new CommandTokenizer(command);
        this.commandInfo = commandInfo;
        this.executionInfo = executionInfo;
    }

    /**
     * Checks if the given token matches with the last read one.
     * <br>
     * If it does, the next one is read.
     *
     * @param expected the expected token
     * @return the next read token
     */
    @NotNull CommandToken consume(final @NotNull CommandToken expected) {
        match(expected);
        return tokenizer.next();
    }

    /**
     * Checks if the given token matches with the last read one.
     *
     * @param expected the expected token
     */
    void match(final @NotNull CommandToken expected) {
        if (tokenizer.getLastToken() != expected)
            throw CommandParseException.of("Invalid input in command '%s': expected token %s but got '%s'",
                    rawCommand, expected, tokenizer.getLastRead()
            );
    }

}
