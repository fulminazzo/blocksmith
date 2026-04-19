package it.fulminazzo.blocksmith.command.parser;

import it.fulminazzo.blocksmith.command.CommandSenderWrapper;
import it.fulminazzo.blocksmith.command.annotation.*;
import it.fulminazzo.blocksmith.command.node.ArgumentNode;
import it.fulminazzo.blocksmith.command.node.CommandNode;
import it.fulminazzo.blocksmith.command.node.LiteralNode;
import it.fulminazzo.blocksmith.command.node.NumberArgumentNode;
import it.fulminazzo.blocksmith.command.node.handler.CompletionsSupplier;
import it.fulminazzo.blocksmith.command.node.handler.ExecutionHandler;
import it.fulminazzo.blocksmith.command.node.info.CommandInfo;
import it.fulminazzo.blocksmith.command.node.info.PermissionInfo;
import it.fulminazzo.blocksmith.reflect.Reflect;
import it.fulminazzo.blocksmith.reflect.ReflectException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
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
    private final @NotNull ExecutionHandler executionHandler;
    private final @NotNull Parameter[] parameters;

    private final int startIndex;
    private int parameterIndex;
    /**
     * Signals that an optional argument has been reached;
     * therefore, all the following nodes must be optional arguments.
     */
    private @Nullable String optionalArgument;
    /**
     * Signals that a greedy argument has been reached;
     * therefore, nothing else can be specified.
     */
    private @Nullable String greedyArgument;

    private final @NotNull String permissionGroup;
    private @NotNull String computedPermission = "";

    /**
     * Instantiates a new Command parser.
     *
     * @param command          the command
     * @param commandInfo      the command info
     * @param executionHandler the execution handler
     * @param parameterIndex   the starting index of the parameters (excluding the command sender)
     * @param permissionGroup  the group to prepend to the commands permissions, if none is given
     */
    CommandParser(final @NotNull String command,
                  final @NotNull CommandInfo commandInfo,
                  final @NotNull ExecutionHandler executionHandler,
                  final int parameterIndex,
                  final @NotNull String permissionGroup) {
        this.rawCommand = command;
        this.tokenizer = new CommandTokenizer(command);
        this.commandInfo = commandInfo;
        this.executionHandler = executionHandler;
        this.parameters = executionHandler.getMethod().getParameters();
        this.startIndex = parameterIndex;
        this.parameterIndex = parameterIndex;
        this.permissionGroup = permissionGroup;
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

        if (lastLiteral == null) throw parseException("at least one literal must be given to identify the command");
        else {
            commandInfo.merge(lastLiteral.getCommandInfo());
            lastLiteral.setCommandInfo(commandInfo);
        }

        last.setExecutor(executionHandler);
        Method method = executionHandler.getMethod();
        if (method.isAnnotationPresent(Confirm.class)) {
            Confirm confirmAnnotation = method.getAnnotation(Confirm.class);
            lastLiteral.setConfirmationInfo(confirmAnnotation);
        }

        if (parameterIndex != parameters.length)
            throw parseException("method %s declares %s argument parameters, but only %s arguments were given",
                    executionHandler.getMethod(), parameters.length - startIndex, parameterIndex - startIndex
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
     * @param optional if {@code true}, the argument will be marked as optional
     * @return the node
     */
    @NotNull CommandNode parseGeneralArgument(final boolean optional) {
        match(CommandToken.LITERAL);
        String argument = tokenizer.getLastRead();
        if (optional) optionalArgument = argument;
        if (parameterIndex >= parameters.length)
            throw parseException("received argument '%s' but no matching parameter was found", argument);
        Parameter parameter = parameters[parameterIndex++];
        ArgumentNode<?> node = ArgumentNode.of(argument, parameter, optional);
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
        if (parameter.isAnnotationPresent(Tab.class)) {
            Tab tab = parameter.getAnnotation(Tab.class);
            node.setCompletionsSupplier(CompletionsSupplier.of(
                    executionHandler.getExecutor(),
                    tab.value()
            ));
        }
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
        literalNode.setCommandInfo(computed);
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
                        permissionGroup,
                        computedPermission,
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
     * @param commandModule   the command module
     * @param senderType      the type of the sender (to identify methods with sender declared)
     * @param permissionGroup the group to prepend to the commands permissions, if none is given
     * @param executorService the executor to handle asynchronous executions
     * @return the nodes
     */
    public static @NotNull List<CommandNode> parseCommands(final @NotNull Object commandModule,
                                                           final @NotNull Class<?> senderType,
                                                           final @NotNull String permissionGroup,
                                                           final @NotNull ExecutorService executorService) {
        if (commandModule instanceof Class<?>)
            return parseAnonymousCommands((Class<?>) commandModule, senderType, permissionGroup, executorService);

        Class<?> moduleType = commandModule.getClass();
        if (!moduleType.isAnnotationPresent(Command.class))
            throw CommandParseException.of("Invalid command module '%s': %s annotation is required", moduleType, Command.class);
        final Reflect reflect = Reflect.on(commandModule);

        Command baseAnnotation = moduleType.getAnnotation(Command.class);
        String baseCommand = baseAnnotation.value().trim();
        if (baseAnnotation.dynamic()) {
            String methodName = "getAliases";
            final Method aliasesMethod;
            try {
                aliasesMethod = reflect.getMethod(m -> m.getName().equals(methodName) &&
                        m.getParameterCount() == 0 &&
                        !Modifier.isStatic(m.getModifiers()) &&
                        Collection.class.isAssignableFrom(m.getReturnType())
                );
            } catch (ReflectException e) {
                throw CommandParseException.of("Invalid command module '%s': required public method '%s' with return type %s was not found",
                        commandModule, methodName, Collection.class
                );
            }
            Collection<?> aliases = reflect.invoke(aliasesMethod).get();
            baseCommand = String.format("(%s) ", aliases.stream()
                    .filter(Objects::nonNull)
                    .map(Object::toString)
                    .collect(Collectors.joining("|"))
            ) + baseCommand;
            baseCommand = baseCommand.trim();
        }
        if (baseCommand.isEmpty())
            throw CommandParseException.of("Invalid command module '%s': command cannot be empty", commandModule);

        CommandInfo baseCommandInfo = createCommandInfo(moduleType, permissionGroup);
        Duration baseCooldown = extractCooldown(moduleType);

        List<CommandNode> commands = new ArrayList<>();

        for (Method method : reflect.getMethods(m -> !Modifier.isStatic(m.getModifiers()) && m.isAnnotationPresent(Command.class))) {
            Command annotation = method.getAnnotation(Command.class);

            String rawCommand = annotation.value().trim();

            CommandInfo commandInfo = createCommandInfo(method, permissionGroup);
            if (rawCommand.isEmpty()) {
                rawCommand = baseCommand;
                commandInfo.merge(baseCommandInfo);
            } else rawCommand = baseCommand + " " + rawCommand;
            Duration cooldown = extractCooldown(method);
            if (cooldown == null) cooldown = baseCooldown;

            ExecutionHandler executionHandler = prepareExecutionHandler(commandModule, method, cooldown, executorService);
            int parameterIndex = getParameterIndex(method, senderType);

            CommandParser parser = new CommandParser(rawCommand, commandInfo, executionHandler, parameterIndex, permissionGroup);
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
     * @param permissionGroup   the group to prepend to the commands permissions, if none is given
     * @param executorService   the executor to handle asynchronous executions
     * @return the nodes
     */
    static @NotNull List<CommandNode> parseAnonymousCommands(final @NotNull Class<?> commandsContainer,
                                                             final @NotNull Class<?> senderType,
                                                             final @NotNull String permissionGroup,
                                                             final @NotNull ExecutorService executorService) {
        List<CommandNode> commands = new ArrayList<>();
        final Reflect reflect = Reflect.on(commandsContainer);
        for (Method method : reflect.getMethods(m -> Modifier.isStatic(m.getModifiers()) && m.isAnnotationPresent(Command.class))) {
            Command annotation = method.getAnnotation(Command.class);

            String rawCommand = annotation.value().trim();

            if (annotation.dynamic()) {
                String name = method.getName();
                name = name.substring(0, 1).toUpperCase() + name.substring(1);
                final String methodName = "get" + name + "Aliases";
                final Method aliasesMethod;
                try {
                    aliasesMethod = reflect.getMethod(m -> m.getName().equals(methodName) &&
                            m.getParameterCount() == 0 &&
                            Modifier.isStatic(m.getModifiers()) &&
                            Collection.class.isAssignableFrom(m.getReturnType()));
                } catch (ReflectException e) {
                    throw CommandParseException.of("Invalid dynamic command method %s: required public static method '%s' with return type %s was not found",
                            method, methodName, Collection.class
                    );
                }
                Collection<?> aliases = reflect.invoke(aliasesMethod).get();
                if (aliases.isEmpty())
                    throw CommandParseException.of("Invalid dynamic command method %s: aliases cannot be empty", method);
                rawCommand = String.format("(%s) ", aliases.stream()
                        .filter(Objects::nonNull)
                        .map(Object::toString)
                        .collect(Collectors.joining("|"))
                ) + rawCommand;
                rawCommand = rawCommand.trim();
            }

            if (rawCommand.isEmpty())
                throw CommandParseException.of("Invalid command method %s: command cannot be empty", method);

            CommandInfo commandInfo = createCommandInfo(method, permissionGroup);
            Duration cooldown = extractCooldown(method);
            ExecutionHandler executionHandler = prepareExecutionHandler(commandsContainer, method, cooldown, executorService);
            int parameterIndex = getParameterIndex(method, senderType);

            CommandParser parser = new CommandParser(rawCommand, commandInfo, executionHandler, parameterIndex, permissionGroup);
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
     * @param element         the element
     * @param permissionGroup the group to prepend to the commands permissions, if none is given
     * @return the command information
     */
    static @NotNull CommandInfo createCommandInfo(final @NotNull AnnotatedElement element,
                                                  final @NotNull String permissionGroup) {
        final String description = element.getAnnotation(Command.class).description().trim();

        final String permission;
        String group;
        final Permission.Grant grant;
        if (element.isAnnotationPresent(Permission.class)) {
            Permission permissionAnnotation = element.getAnnotation(Permission.class);
            permission = permissionAnnotation.value().trim();
            group = permissionAnnotation.group();
            if (group.trim().isEmpty()) group = permissionGroup;
            grant = permissionAnnotation.grant();
        } else {
            permission = "";
            group = permissionGroup;
            grant = Permission.Grant.OP;
        }

        PermissionInfo permissionInfo = new PermissionInfo(group, permission, grant);
        return new CommandInfo(description, permissionInfo);
    }

    /**
     * Creates a new Execution handler from the given method.
     *
     * @param executor        the actual executor of the method
     * @param method          the method with the command logic
     * @param cooldown        the cooldown extracted for the method
     * @param executorService the executor to handle asynchronous executions
     * @return the execution handler
     */
    static @NotNull ExecutionHandler prepareExecutionHandler(final @NotNull Object executor,
                                                             final @NotNull Method method,
                                                             final @Nullable Duration cooldown,
                                                             final @NotNull ExecutorService executorService) {
        ExecutionHandler handler = new ExecutionHandler(executor, method).setCooldown(cooldown);
        if (method.isAnnotationPresent(Async.class)) {
            Async asyncAnnotation = method.getAnnotation(Async.class);
            handler.setAsync(
                    executorService,
                    Duration.of(asyncAnnotation.value(), asyncAnnotation.unit().toChronoUnit())
            );
        }
        return handler;
    }

    private static int getParameterIndex(final @NotNull Method method, final @NotNull Class<?> senderType) {
        int parameterIndex = 0;
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length > 0) {
            Class<?> parameterType = parameterTypes[0];
            if (senderType.isAssignableFrom(parameterType) || CommandSenderWrapper.class.isAssignableFrom(parameterType))
                parameterIndex++;
        }
        return parameterIndex;
    }

    private static @Nullable Duration extractCooldown(final @NotNull AnnotatedElement element) {
        if (element.isAnnotationPresent(Cooldown.class)) {
            Cooldown cooldownAnnotation = element.getAnnotation(Cooldown.class);
            return Duration.of(cooldownAnnotation.value(), cooldownAnnotation.unit().toChronoUnit());
        } else return null;
    }

}
