package it.fulminazzo.blocksmith.command.parser;

import it.fulminazzo.blocksmith.command.annotation.Command;
import it.fulminazzo.blocksmith.command.annotation.Default;
import it.fulminazzo.blocksmith.command.annotation.Greedy;
import it.fulminazzo.blocksmith.command.annotation.Permission;
import it.fulminazzo.blocksmith.command.node.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * A parser to convert a command declaration into {@link CommandNode}s.
 * Requires the method that will execute the command, with the command information
 * (declaration, description and permission).
 */
public final class CommandParser {
    private final @NotNull String rawCommand;
    private final @NotNull CommandTokenizer tokenizer;
    private final @NotNull CommandInfo commandInfo;
    private final @NotNull ExecutionInfo executionInfo;
    private final @NotNull Parameter[] parameters;

    private final int startIndex;
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
    CommandParser(final @NotNull String command,
                  final @NotNull CommandInfo commandInfo,
                  final @NotNull ExecutionInfo executionInfo,
                  final int parameterIndex) {
        this.rawCommand = command;
        this.tokenizer = new CommandTokenizer(command);
        this.commandInfo = commandInfo;
        this.executionInfo = executionInfo;
        this.parameters = executionInfo.getMethod().getParameters();
        this.startIndex = parameterIndex;
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

        if (parameterIndex != parameters.length)
            throw parseException("method %s declares %s argument parameters, but only %s arguments were given",
                    executionInfo.getMethod(), parameters.length - startIndex, parameterIndex - startIndex
            );

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
     */
    void consume(final @NotNull CommandToken expected) {
        match(expected);
        tokenizer.next();
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

    /**
     * Obtains all the Command nodes from the given module.
     *
     * @param commandModule the command module
     * @param senderType        the type of the sender (to identify methods with sender declared)
     * @return the nodes
     */
    public static @NotNull List<CommandNode> parseCommands(final @NotNull Object commandModule,
                                                           final @NotNull Class<?> senderType) {
        if (commandModule instanceof Class<?>) return parseAnonymousCommands((Class<?>) commandModule, senderType);

        Class<?> moduleType = commandModule.getClass();
        if (!moduleType.isAnnotationPresent(Command.class))
            throw CommandParseException.of("Invalid command module '%s': %s annotation is required", commandModule, Command.class);
        String baseCommand = moduleType.getAnnotation(Command.class).value().trim();
        if (baseCommand.isEmpty())
            throw CommandParseException.of("Invalid command module '%s': command cannot be empty", commandModule);

        CommandInfo baseCommandInfo = createCommandInfo(moduleType);

        List<CommandNode> commands = new ArrayList<>();

        for (Method method : moduleType.getMethods())
            if (!Modifier.isStatic(method.getModifiers()) && method.isAnnotationPresent(Command.class)) {
                Command annotation = method.getAnnotation(Command.class);

                String rawCommand = annotation.value().trim();

                CommandInfo commandInfo = createCommandInfo(method);
                if (rawCommand.isEmpty()) commandInfo.merge(baseCommandInfo);
                else rawCommand = baseCommand + " " + rawCommand;

                ExecutionInfo executionInfo = new ExecutionInfo(commandModule, method);
                int parameterIndex = 0;
                if (method.getParameterTypes()[0].equals(senderType)) parameterIndex++;

                CommandParser parser = new CommandParser(rawCommand, commandInfo, executionInfo, parameterIndex);
                CommandNode node = parser.parse();

                commands.add(node);

                commandInfo.merge(getComputedCommandInfo(node));
            }
        return commands;
    }

    /**
     * Obtains all the Command nodes from the given class.
     * Command nodes will be created from static functions annotated with {@link Command}.
     *
     * @param commandsContainer the commands container
     * @param senderType        the type of the sender (to identify methods with sender declared)
     * @return the nodes
     */
    static @NotNull List<CommandNode> parseAnonymousCommands(final @NotNull Class<?> commandsContainer,
                                                             final @NotNull Class<?> senderType) {
        List<CommandNode> commands = new ArrayList<>();
        for (Method method : commandsContainer.getMethods())
            if (Modifier.isStatic(method.getModifiers()) && method.isAnnotationPresent(Command.class)) {
                Command annotation = method.getAnnotation(Command.class);

                String rawCommand = annotation.value().trim();
                if (rawCommand.isEmpty())
                    throw CommandParseException.of("Invalid command method %s: command cannot be empty", method);

                CommandInfo commandInfo = createCommandInfo(method);
                ExecutionInfo executionInfo = new ExecutionInfo(commandsContainer, method);
                int parameterIndex = 0;
                if (method.getParameterTypes()[0].equals(senderType)) parameterIndex++;

                CommandParser parser = new CommandParser(rawCommand, commandInfo, executionInfo, parameterIndex);
                CommandNode node = parser.parse();

                commands.add(node);

                commandInfo.merge(getComputedCommandInfo(node));
            }
        return commands;
    }

    /**
     * Computes a default Command info object from the given node and the corresponding command declaration.
     *
     * @param node the node
     * @return the computed command information
     */
    static @NotNull CommandInfo getComputedCommandInfo(final @NotNull CommandNode node) {
        StringBuilder computedPermission = new StringBuilder();
        CommandNode n = node;
        while (n != null) {
            if (n instanceof LiteralNode) {
                if (computedPermission.length() > 0) computedPermission.append(".");
                computedPermission.append(n.getName());
            }
            n = n.getFirstChild();
        }
        return new CommandInfo(
                "command.description." + computedPermission,
                new PermissionInfo(
                        computedPermission.toString(),
                        Permission.Default.OP
                )
        );
    }

    /**
     * Creates a Command info object from the given element.
     * Will check for the {@link Command} annotation to determine the description and
     * for the {@link Permission} annotation to determine the permission information.
     *
     * @param element the element
     * @return the command information
     */
    static @NotNull CommandInfo createCommandInfo(final @NotNull AnnotatedElement element) {
        final String description;
        if (element.isAnnotationPresent(Command.class))
            description = element.getAnnotation(Command.class).description().trim();
        else description = "";

        final String permission;
        final Permission.Default scope;
        if (element.isAnnotationPresent(Permission.class)) {
            Permission permissionAnnotation = element.getAnnotation(Permission.class);
            permission = permissionAnnotation.value().trim();
            scope = permissionAnnotation.permissionDefault();
        } else {
            permission = "";
            scope = Permission.Default.OP;
        }

        PermissionInfo permissionInfo = new PermissionInfo(permission, scope);
        return new CommandInfo(description, permissionInfo);
    }

}
