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
//
//    private @Nullable CustomCompletionsProvider customCompletionsProvider;
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
//    protected void validateTabCompleteInput(final @NotNull CommandExecutionContext context) throws CommandExecutionException {
//        if (isGreedy()) context.mergeRemainingInput();
//        if (!getArgumentParser().validateCompletions(context))
//            throw new CommandExecutionException();
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
//}
