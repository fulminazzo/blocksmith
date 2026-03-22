package it.fulminazzo.blocksmith.command.parser;

import it.fulminazzo.blocksmith.command.node.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * A parser to read raw commands declarations and transform them into {@link CommandNode}.
 */
public final class CommandParser {
    private final @NotNull String rawCommand;
    private final @NotNull CommandTokenizer tokenizer;
    private final @NotNull CommandInfo commandInfo;
    private final @NotNull ExecutionInfo executionInfo;
    private final @NotNull Class<?>[] parameterTypes;

    private int parameterIndex;
    // Signals that an optional argument has been reached;
    // therefore, all the following nodes must be optional arguments.
    private boolean optionalArguments;

    /**
     * Instantiates a new Command parser.
     *
     * @param command        the command
     * @param commandInfo    the command info
     * @param executionInfo  the execution info
     * @param parameterIndex the starting index of the parameters (excluding the command sender)
     */
    public CommandParser(final @NotNull String command,
                         final @NotNull CommandInfo commandInfo,
                         final @NotNull ExecutionInfo executionInfo,
                         final int parameterIndex) {
        this.rawCommand = command;
        this.tokenizer = new CommandTokenizer(command);
        this.commandInfo = commandInfo;
        this.executionInfo = executionInfo;
        this.parameterTypes = executionInfo.getMethod().getParameterTypes();
        this.parameterIndex = parameterIndex;
    }

    /**
     * EXPRESSION := OPTIONAL_ARGUMENT | MANDATORY_ARGUMENT | ALIASES_LITERAL | SIMPLE_LITERAL
     *
     * @return the node
     */
    @NotNull CommandNode parseExpression() {
        CommandToken lastToken = tokenizer.getLastToken();
        if (lastToken == CommandToken.OPEN_BRACKET)
            return parseOptionalArgument();
        else if (optionalArguments)
            throw CommandParseException.of("Invalid input in command '%s': an optional argument has been defined, therefore all upcoming nodes must be optional arguments as well", rawCommand);
        else switch (lastToken) {
            case LOWER_THAN:
                return parseMandatoryArgument();
            case OPEN_PHARENTHESIS:
                return parseAliasesLiteral();
            default:
                return parseSimpleLiteral();
        }
    }

    /**
     * OPTIONAL_ARGUMENT := [ GENERAL_ARGUMENT ]
     *
     * @return the node
     */
    @NotNull CommandNode parseOptionalArgument() {
        consume(CommandToken.OPEN_BRACKET);
        CommandNode node = parseGeneralArgument(true);
        consume(CommandToken.CLOSE_BRACKET);
        return node;
    }

    /**
     * MANDATORY_ARGUMENT := &lt; GENERAL_ARGUMENT &gt;
     *
     * @return the node
     */
    @NotNull CommandNode parseMandatoryArgument() {
        consume(CommandToken.LOWER_THAN);
        CommandNode node = parseGeneralArgument(false);
        consume(CommandToken.GREATER_THAN);
        return node;
    }

    /**
     * GENERAL_ARGUMENT := {@link CommandToken#LITERAL}
     *
     * @param optional if <code>true</code>, the argument will be marked as optional
     * @return the node
     */
    @NotNull CommandNode parseGeneralArgument(final boolean optional) {
        if (optional) optionalArguments = true;
        match(CommandToken.LITERAL);
        if (parameterIndex >= parameterTypes.length)
            throw CommandParseException.of("Invalid input in command '%s': received argument '%s' but no matching parameter was found",
                    rawCommand, tokenizer.getLastRead());
        ArgumentNode<?> node = new ArgumentNode<>(tokenizer.getLastRead(), parameterTypes[parameterIndex++], optional);
        tokenizer.next();
        return node;
    }

    /**
     * ALIASES_LITERAL := \( {@link CommandToken#LITERAL} ( \| {@link CommandToken#LITERAL})* \)
     *
     * @return the node
     */
    @NotNull CommandNode parseAliasesLiteral() {
        List<String> aliases = new ArrayList<>();
        do {
            tokenizer.next();
            match(CommandToken.LITERAL);
            aliases.add(tokenizer.getLastRead());
        } while (tokenizer.next() == CommandToken.PIPE);
        consume(CommandToken.CLOSE_PARENTHESIS);
        return new LiteralNode(aliases.toArray(new String[0]));
    }

    /**
     * SIMPLE_LITERAL := {@link CommandToken#LITERAL}
     *
     * @return the node
     */
    @NotNull CommandNode parseSimpleLiteral() {
        match(CommandToken.LITERAL);
        String literal = tokenizer.getLastRead();
        tokenizer.next();
        return new LiteralNode(literal);
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
            throw CommandParseException.of("Invalid input in command '%s': expected '%s' but got '%s'",
                    rawCommand, expected, tokenizer.getLastRead()
            );
    }

}
