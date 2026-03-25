package it.fulminazzo.blocksmith.command.parser;

import it.fulminazzo.blocksmith.command.annotation.*;
import it.fulminazzo.blocksmith.command.node.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.*;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
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
    private final @Nullable Duration cooldown;
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

    private final @Nullable String prefix;
    private @NotNull String computedPermission = "";

    /**
     * Instantiates a new Command parser.
     *
     * @param command        the command
     * @param commandInfo    the command info
     * @param cooldown       the cooldown of the command
     * @param executionInfo  the execution info
     * @param parameterIndex the starting index of the parameters (excluding the command sender)
     * @param prefix         the string to prepend to the generated commands permission, if none is given
     */
    CommandParser(final @NotNull String command,
                  final @NotNull CommandInfo commandInfo,
                  final @Nullable Duration cooldown,
                  final @NotNull ExecutionInfo executionInfo,
                  final int parameterIndex,
                  final @Nullable String prefix) {
        this.rawCommand = command;
        this.tokenizer = new CommandTokenizer(command);
        this.commandInfo = commandInfo;
        this.cooldown = cooldown;
        this.executionInfo = executionInfo;
        this.parameters = executionInfo.getMethod().getParameters();
        this.startIndex = parameterIndex;
        this.parameterIndex = parameterIndex;
        this.prefix = prefix;
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
        if (cooldown != null) last.setCooldown(cooldown);

        if (lastLiteral == null) throw parseException("at least one literal must be given to identify the command");
        else {
            lastLiteral.getCommandInfo().ifPresent(commandInfo::merge);
            lastLiteral.setCommandInfo(commandInfo);
        }

        if (parameterIndex != parameters.length)
            throw parseException("method %s declares %s argument parameters, but only %s arguments were given",
                    executionInfo.getMethod(), parameters.length - startIndex, parameterIndex - startIndex
            );

        return first;
    }

    /**
     * EXPRESSION := OPTIONAL_ARGUMENT | MANDATORY_ARGUMENT | LITERAL
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
        else if (lastToken == CommandToken.LOWER_THAN) return parseMandatoryArgument();
        else return parseLiteral();
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
        ArgumentNode<?> node = ArgumentNode.newNode(argument, parameter.getType(), optional);
        if (parameter.isAnnotationPresent(Default.class))
            node.setDefaultValue(parameter.getAnnotation(Default.class).value());
        if (parameter.isAnnotationPresent(Greedy.class)) {
            node.setGreedy(true);
            greedyArgument = argument;
        }
        if (parameter.isAnnotationPresent(Range.class))
            if (node instanceof NumberArgumentNode<?>) {
                NumberArgumentNode<?> argumentNode = (NumberArgumentNode<?>) node;
                Range range = parameter.getAnnotation(Range.class);
                argumentNode.min(range.min()).max(range.max());
            } else throw parseException("argument '%s' type must be %s or an implementation", argument, Number.class);
        tokenizer.next();
        return node;
    }

    /**
     * LITERAL := ALIASES_LITERAL | SIMPLE_LITERAL
     *
     * @return the node
     */
    @NotNull CommandNode parseLiteral() {
        final LiteralNode literalNode;
        if (tokenizer.getLastToken() == CommandToken.OPEN_PHARENTHESIS)
            literalNode = parseAliasesLiteral();
        else literalNode = parseSimpleLiteral();
        if (!computedPermission.isEmpty()) computedPermission += ".";
        computedPermission += literalNode.getName();
        final CommandInfo computed = computeCurrentCommandInfo();
        literalNode.getCommandInfo().ifPresentOrElse(
                i -> i.merge(computed),
                () -> literalNode.setCommandInfo(computed)
        );
        return literalNode;
    }

    /**
     * ALIASES_LITERAL := \( {@link CommandToken#LITERAL} ( \| {@link CommandToken#LITERAL})* \)
     *
     * @return the node
     */
    @NotNull LiteralNode parseAliasesLiteral() {
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
    @NotNull LiteralNode parseSimpleLiteral() {
        match(CommandToken.LITERAL);
        String literal = tokenizer.getLastRead();
        tokenizer.next();
        return new LiteralNode(literal);
    }

    @SuppressWarnings("deprecation")
    private @NotNull CommandInfo computeCurrentCommandInfo() {
        return new CommandInfo(
                getDefaultDescription(computedPermission),
                new PermissionInfo(
                        (prefix == null ? "" : prefix + ".") + computedPermission,
                        Permission.Grant.OP,
                        true
                ),
                true
        );
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
     * @param senderType    the type of the sender (to identify methods with sender declared)
     * @param prefix        the string to prepend to the generated commands permission, if none is given
     * @return the nodes
     */
    public static @NotNull List<CommandNode> parseCommands(final @NotNull Object commandModule,
                                                           final @NotNull Class<?> senderType,
                                                           final @Nullable String prefix) {
        if (commandModule instanceof Class<?>)
            return parseAnonymousCommands((Class<?>) commandModule, senderType, prefix);

        Class<?> moduleType = commandModule.getClass();
        if (!moduleType.isAnnotationPresent(Command.class))
            throw CommandParseException.of("Invalid command module '%s': %s annotation is required", commandModule, Command.class);

        Command baseAnnotation = moduleType.getAnnotation(Command.class);
        String baseCommand = baseAnnotation.value().trim();
        if (baseAnnotation.dynamic()) {
            String methodName = "getAliases";
            try {
                Method aliasesMethod = moduleType.getMethod(methodName);
                if (!Modifier.isStatic(aliasesMethod.getModifiers()) && Collection.class.isAssignableFrom(aliasesMethod.getReturnType())) {
                    Collection<?> aliases = (Collection<?>) aliasesMethod.invoke(commandModule);
                    baseCommand = String.format("(%s) ", aliases.stream()
                            .filter(Objects::nonNull)
                            .map(Object::toString)
                            .collect(Collectors.joining("|"))
                    ) + baseCommand;
                    baseCommand = baseCommand.trim();
                } else throw new NoSuchMethodException();
            } catch (NoSuchMethodException e) {
                throw CommandParseException.of("Invalid command module '%s': required public method '%s' with return type %s was not found",
                        commandModule, methodName, Collection.class
                );
            } catch (InvocationTargetException e) {
                Throwable cause = e.getCause();
                if (cause instanceof RuntimeException) throw (RuntimeException) cause;
                else throw new RuntimeException(cause);
            } catch (IllegalAccessException e) {
                throw CommandParseException.of("Invalid command module '%s': cannot access method '%s'",
                        commandModule, methodName
                );
            }
        }
        if (baseCommand.isEmpty())
            throw CommandParseException.of("Invalid command module '%s': command cannot be empty", commandModule);

        CommandInfo baseCommandInfo = createCommandInfo(moduleType);
        Duration baseCooldown = extractCooldown(moduleType);

        List<CommandNode> commands = new ArrayList<>();

        for (Method method : moduleType.getMethods())
            if (!Modifier.isStatic(method.getModifiers()) && method.isAnnotationPresent(Command.class)) {
                Command annotation = method.getAnnotation(Command.class);

                String rawCommand = annotation.value().trim();

                CommandInfo commandInfo = createCommandInfo(method);
                if (rawCommand.isEmpty()) {
                    rawCommand = baseCommand;
                    commandInfo.merge(baseCommandInfo);
                } else rawCommand = baseCommand + " " + rawCommand;
                Duration cooldown = extractCooldown(method);
                if (cooldown == null) cooldown = baseCooldown;

                ExecutionInfo executionInfo = new ExecutionInfo(commandModule, method);
                int parameterIndex = getParameterIndex(method, senderType);

                CommandParser parser = new CommandParser(rawCommand, commandInfo, cooldown, executionInfo, parameterIndex, prefix);
                CommandNode node = parser.parse();

                commands.add(node);
            }
        return commands;
    }

    /**
     * Obtains all the Command nodes from the given class.
     * Command nodes will be created from static functions annotated with {@link Command}.
     *
     * @param commandsContainer the commands container
     * @param senderType        the type of the sender (to identify methods with sender declared)
     * @param prefix            the string to prepend to the generated commands permission, if none is given
     * @return the nodes
     */
    static @NotNull List<CommandNode> parseAnonymousCommands(final @NotNull Class<?> commandsContainer,
                                                             final @NotNull Class<?> senderType,
                                                             final @Nullable String prefix) {
        List<CommandNode> commands = new ArrayList<>();
        for (Method method : commandsContainer.getMethods())
            if (Modifier.isStatic(method.getModifiers()) && method.isAnnotationPresent(Command.class)) {
                Command annotation = method.getAnnotation(Command.class);

                String rawCommand = annotation.value().trim();

                if (annotation.dynamic()) {
                    String methodName = method.getName();
                    methodName = methodName.substring(0, 1).toUpperCase() + methodName.substring(1);
                    methodName = "get" + methodName + "Aliases";
                    try {
                        Method aliasesMethod = commandsContainer.getMethod(methodName);
                        if (Modifier.isStatic(aliasesMethod.getModifiers()) && Collection.class.isAssignableFrom(aliasesMethod.getReturnType())) {
                            Collection<?> aliases = (Collection<?>) aliasesMethod.invoke(commandsContainer);
                            rawCommand = String.format("(%s) ", aliases.stream()
                                    .filter(Objects::nonNull)
                                    .map(Object::toString)
                                    .collect(Collectors.joining("|"))
                            ) + rawCommand;
                            rawCommand = rawCommand.trim();
                        } else throw new NoSuchMethodException();
                    } catch (NoSuchMethodException e) {
                        throw CommandParseException.of("Invalid dynamic command method %s: required public static method '%s' with return type %s was not found",
                                method, methodName, Collection.class
                        );
                    } catch (InvocationTargetException e) {
                        Throwable cause = e.getCause();
                        if (cause instanceof RuntimeException) throw (RuntimeException) cause;
                        else throw new RuntimeException(cause);
                    } catch (IllegalAccessException e) {
                        throw CommandParseException.of("Invalid dynamic command method %s: cannot access method '%s'",
                                method, methodName
                        );
                    }
                }

                if (rawCommand.isEmpty())
                    throw CommandParseException.of("Invalid command method %s: command cannot be empty", method);

                CommandInfo commandInfo = createCommandInfo(method);
                Duration cooldown = extractCooldown(method);
                ExecutionInfo executionInfo = new ExecutionInfo(commandsContainer, method);
                int parameterIndex = getParameterIndex(method, senderType);

                CommandParser parser = new CommandParser(rawCommand, commandInfo, cooldown, executionInfo, parameterIndex, prefix);
                CommandNode node = parser.parse();

                commands.add(node);
            }
        return commands;
    }

    /**
     * Gets the default description of the given command path.
     *
     * @param path the path
     * @return the default description
     */
    public static @NotNull String getDefaultDescription(final @NotNull String path) {
        return "command.description." + path;
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
        final Permission.Grant grant;
        if (element.isAnnotationPresent(Permission.class)) {
            Permission permissionAnnotation = element.getAnnotation(Permission.class);
            permission = permissionAnnotation.value().trim();
            grant = permissionAnnotation.grant();
        } else {
            permission = "";
            grant = Permission.Grant.OP;
        }

        PermissionInfo permissionInfo = new PermissionInfo(permission, grant);
        return new CommandInfo(description, permissionInfo);
    }

    private static int getParameterIndex(final @NotNull Method method, final @NotNull Class<?> senderType) {
        int parameterIndex = 0;
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length > 0 && parameterTypes[0].equals(senderType)) parameterIndex++;
        return parameterIndex;
    }

    private static @Nullable Duration extractCooldown(final @NotNull AnnotatedElement element) {
        if (element.isAnnotationPresent(Cooldown.class)) {
            Cooldown cooldownAnnotation = element.getAnnotation(Cooldown.class);
            return Duration.of(cooldownAnnotation.value(), cooldownAnnotation.unit().toChronoUnit());
        } else return null;
    }

}
