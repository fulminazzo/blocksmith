package it.fulminazzo.blocksmith.command.parser;

import it.fulminazzo.blocksmith.command.annotation.Default;
import it.fulminazzo.blocksmith.command.annotation.Greedy;
import it.fulminazzo.blocksmith.command.node.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * A parser to read raw commands declarations and transform them into {@link CommandNode}.
 */
public final class CommandParser {
    private final @NotNull String rawCommand;
    private final @NotNull CommandTokenizer tokenizer;
    private final @NotNull CommandInfo commandInfo;
    private final @NotNull ExecutionInfo executionInfo;
    private final @NotNull Parameter[] parameters;

    private int parameterIndex;
    // Signals that an optional argument has been reached;
    // therefore, all the following nodes must be optional arguments.
    private @Nullable String optionalArgument;
    // Signals that a greedy argument has been reached;
    // therefore, nothing else can be specified.
    private @Nullable String greedyArgument;

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
        this.parameters = executionInfo.getMethod().getParameters();
        this.parameterIndex = parameterIndex;
    }

    /**
     * Converts the given raw command input to a node of commands.
     *
     * @return the node
     */
    public @NotNull CommandNode parse() {
        CommandNode first = null;
        LiteralNode lastLiteral = null;
        CommandNode last = null;
        tokenizer.next();
        while (tokenizer.getLastToken() != CommandToken.EOF) {
            CommandNode node = parseExpression();
            if (first == null) first = node;
            else last.addChild(node);
            if (node instanceof LiteralNode) lastLiteral = (LiteralNode) node;
            last = node;
            if (tokenizer.getLastToken() != CommandToken.EOF)
                consume(CommandToken.SPACE);
        }
        if (first == null) throw parseException("could not parse command");
        last.setExecutionInfo(executionInfo);

        if (lastLiteral == null) throw parseException("at least one literal must be given to identify the command");
        else lastLiteral.setCommandInfo(commandInfo);

        return first;
    }

    /**
     * EXPRESSION := OPTIONAL_ARGUMENT | MANDATORY_ARGUMENT | ALIASES_LITERAL | SIMPLE_LITERAL
     *
     * @return the node
     */
    @NotNull CommandNode parseExpression() {
        if (greedyArgument != null)
            throw parseException("after declaring greedy argument '%s', no subsequent node can be specified " +
                    "(the greedy argument will inglobate all the remaining input anyway)",
                    greedyArgument);
        CommandToken lastToken = tokenizer.getLastToken();
        if (lastToken == CommandToken.OPEN_BRACKET)
            return parseOptionalArgument();
        else if (optionalArgument != null)
            throw parseException("after declaring optional argument '%s', all subsequent nodes MUST be of the same kind (optional arguments)", optionalArgument);
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
        match(CommandToken.LITERAL);
        String argument = tokenizer.getLastRead();
        if (optional) optionalArgument = argument;
        if (parameterIndex >= parameters.length)
            throw parseException("received argument '%s' but no matching parameter was found", argument);
        Parameter parameter = parameters[parameterIndex++];
        ArgumentNode<?> node = new ArgumentNode<>(argument, parameter.getType(), optional);
        if (parameter.isAnnotationPresent(Default.class))
            node.setDefaultValue(parameter.getAnnotation(Default.class).value());
        if (parameter.isAnnotationPresent(Greedy.class)) {
            node.setGreedy(true);
            greedyArgument = argument;
        }
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
            throw parseException("expected '%s' but got '%s'", expected, tokenizer.getLastRead());
    }

    private @NotNull CommandParseException parseException(final @NotNull String message,
                                                          final Object @NotNull ... args) {
        return CommandParseException.of("Invalid input in command '%s': " + message,
                Stream.concat(Stream.of(rawCommand), Stream.of(args)).toArray()
        );
    }

}
