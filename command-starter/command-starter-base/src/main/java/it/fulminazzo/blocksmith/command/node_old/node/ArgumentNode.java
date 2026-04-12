package it.fulminazzo.blocksmith.command.node_old.node;//TODO: update
//package it.fulminazzo.blocksmith.command.node;
//
//import it.fulminazzo.blocksmith.command.argument.ArgumentParser;
//import it.fulminazzo.blocksmith.command.argument.ArgumentParsers;
//import it.fulminazzo.blocksmith.command.execution.CommandExecutionContext;
//import it.fulminazzo.blocksmith.command.execution.CommandExecutionException;
//import it.fulminazzo.blocksmith.message.argument.Placeholder;
//import it.fulminazzo.blocksmith.util.ReflectionUtils;
//import lombok.*;
//import org.jetbrains.annotations.NotNull;
//import org.jetbrains.annotations.Nullable;
//
//import java.util.List;
//import java.util.stream.Collectors;
//
///**
// * Represents a dynamic argument node.
// *
// * @param <T> the type of the argument
// */
//@Data
//@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
//@EqualsAndHashCode(callSuper = true, doNotUseGetters = true)
//@ToString(callSuper = true, doNotUseGetters = true)
//public class ArgumentNode<T> extends CommandNode {
//    private final @NotNull String name;
//    private final @NotNull Class<T> type;
//    private final boolean optional;
//    @Getter(AccessLevel.NONE)
//    private @Nullable String defaultValue;
//    private boolean greedy;
//
//    private @Nullable CustomCompletionsProvider customCompletionsProvider;
//
//    /**
//     * Sets the current node to greedy.
//     *
//     * @param greedy if <code>true</code>, will take all the remaining input
//     * @return this object (for method chaining)
//     */
//    public @NotNull ArgumentNode<T> setGreedy(final boolean greedy) {
//        this.greedy = greedy;
//        return this;
//    }
//
//    /**
//     * Gets the default value (if given).
//     *
//     * @param context the current context of action
//     * @return the default value
//     * @throws CommandExecutionException in case of parsing errors
//     */
//    public @Nullable T getDefaultValue(final @NotNull CommandExecutionContext context) throws CommandExecutionException {
//        return defaultValue == null ? null : parseArgument(context.addInput(defaultValue));
//    }
//
//    private @Nullable T parseArgument(final @NotNull CommandExecutionContext context) throws CommandExecutionException {
//        if (customCompletionsProvider != null) {
//            String argument = context.getCurrent();
//            List<String> completions = customCompletionsProvider.getCompletions();
//            if (completions.stream()
//                    .noneMatch(c -> c.equalsIgnoreCase(argument)))
//                throw new CommandExecutionException("error.invalid-argument")
//                        .arguments(Placeholder.of("argument", argument),
//                                Placeholder.of("expected", String.join(", ", completions)));
//        }
//        return getArgumentParser().parse(context);
//    }
//
//    @Override
//    public @NotNull List<String> getCompletions(final @NotNull CommandExecutionContext context) {
//        if (customCompletionsProvider != null) return customCompletionsProvider.getCompletions();
//        else return getArgumentParser().getCompletions(context).stream()
//                .map(c -> c.replace("%name%", getName()))
//                .collect(Collectors.toList());
//    }
//
//    @Override
//    protected void processInput(final @NotNull CommandExecutionContext context) throws CommandExecutionException {
//        if (isGreedy()) context.mergeRemainingInput();
//        context.addParsedArgument(parseArgument(context));
//    }
//
//    @Override
//    protected void validateTabCompleteInput(final @NotNull CommandExecutionContext context) throws CommandExecutionException {
//        if (isGreedy()) context.mergeRemainingInput();
//        if (!getArgumentParser().validateCompletions(context))
//            throw new CommandExecutionException();
//    }
//
//    @Override
//    public boolean matches(final @NotNull String token) {
//        return true;
//    }
//
//    /**
//     * Gets the best argument parser for this node.
//     *
//     * @return the argument parser
//     */
//    protected @NotNull ArgumentParser<T> getArgumentParser() {
//        return ArgumentParsers.of(type);
//    }
//
//    /**
//     * Instantiates a new Argument node.
//     *
//     * @param <T>      the type of the parameter
//     * @param name     the name
//     * @param type     the Java class of the parameter
//     * @param optional if <code>true</code> the parameter will be non-mandatory
//     * @return the argument node
//     */
//    @SuppressWarnings("unchecked")
//    public static <T> @NotNull ArgumentNode<T> newNode(final @NotNull String name,
//                                                       @NotNull Class<?> type,
//                                                       final boolean optional) {
//        Class<T> actualType = (Class<T>) ReflectionUtils.toWrapper(type);
//        if (Number.class.isAssignableFrom(actualType))
//            return (ArgumentNode<T>) new NumberArgumentNode<>(name, (Class<? extends Number>) actualType, optional);
//        else return new ArgumentNode<>(name, actualType, optional);
//    }
//
//}
