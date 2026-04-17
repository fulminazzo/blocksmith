package it.fulminazzo.blocksmith.command.node_old.node;//TODO: update
//package it.fulminazzo.blocksmith.command.node;
//
//import it.fulminazzo.blocksmith.command.CommandSenderWrapper;
//import it.fulminazzo.blocksmith.command.TabCompletable;
//import it.fulminazzo.blocksmith.command.annotation.Permission;
//import it.fulminazzo.blocksmith.command.execution.CommandExecutionContext;
//import it.fulminazzo.blocksmith.command.execution.CommandExecutionException;
//import it.fulminazzo.blocksmith.cooldown.StaticCooldownManager;
//import it.fulminazzo.blocksmith.message.argument.Placeholder;
//import it.fulminazzo.blocksmith.message.argument.Time;
//import lombok.*;
//import org.jetbrains.annotations.NotNull;
//import org.jetbrains.annotations.Nullable;
//
//import java.time.Duration;
//import java.util.*;
//import java.util.stream.Collectors;
//
///**
// * Represents the node of a command, whether it is static or dynamic.
// */
//@EqualsAndHashCode
//@ToString
//public abstract class CommandNode implements TabCompletable {
//
//    /**
//     * Gets all the {@link ArgumentNode} children that are greedy.
//     *
//     * @return the children
//     */
//    public @NotNull List<ArgumentNode<?>> getGreedyChildren() {
//        return children.stream()
//                .filter(c -> c instanceof ArgumentNode)
//                .map(c -> (ArgumentNode<?>) c)
//                .filter(ArgumentNode::isGreedy)
//                .collect(Collectors.toList());
//    }
//
//    /**
//     * Gets the permission to execute this node.
//     *
//     * @return the permission, if available
//     */
//    public @NotNull Optional<PermissionInfo> getPermission() {
//        if (this instanceof LiteralNode) return ((LiteralNode) this).getCommandInfo().map(CommandInfo::getPermission);
//        else if (parent == null) return Optional.empty();
//        else return parent.getPermission();
//    }
//
//    /**
//     * Returns all the tab completions based on the current context of execution.
//     *
//     * @param context the context
//     * @return the completions
//     */
//    public @NotNull List<String> tabComplete(final @NotNull CommandExecutionContext context) {
//        if (context.isDone()) return Collections.emptyList();
//        else {
//            try {
//                validateTabCompleteInput(context);
//                if (context.isLast() || context.advanceCursor().isLast()) {
//                    List<String> completions = getChildren().stream()
//                            .map(c -> c.getCompletions(context))
//                            .flatMap(Collection::stream)
//                            .collect(Collectors.toList());
//                    return filterCompletions(context, completions);
//                } else {
//                    CommandNode child = getChild(context.getCurrent());
//                    if (child == null) return Collections.emptyList();
//                    else {
//                        List<ArgumentNode<?>> greedyChildren = getGreedyChildren();
//                        if (greedyChildren.contains(child))
//                            return filterCompletions(context, child.getCompletions(context));
//                        else return child.tabComplete(context);
//                    }
//                }
//            } catch (CommandExecutionException e) {
//                return Collections.emptyList();
//            }
//        }
//    }
//
//    /**
//     * Removes any completion not starting with &lt; and not matching the last input.
//     *
//     * @param context     the context
//     * @param completions the completions
//     * @return the completions
//     */
//    @NotNull List<String> filterCompletions(final @NotNull CommandExecutionContext context,
//                                                    final @NotNull List<String> completions) {
//        List<String> finalCompletions = completions.stream()
//                .filter(c -> c.toLowerCase().startsWith(context.getCurrent().toLowerCase()))
//                .collect(Collectors.toList());
//        if (finalCompletions.isEmpty())
//            return completions.stream()
//                    .filter(c -> c.startsWith("<"))
//                    .collect(Collectors.toList());
//        else return finalCompletions;
//    }
//
//    /**
//     * Validates the current input of the context during tab completion.
//     *
//     * @param context the context
//     * @throws CommandExecutionException in case of any error (the message should contain the message code for translations)
//     */
//    protected abstract void validateTabCompleteInput(final @NotNull CommandExecutionContext context) throws CommandExecutionException;
//
//}
