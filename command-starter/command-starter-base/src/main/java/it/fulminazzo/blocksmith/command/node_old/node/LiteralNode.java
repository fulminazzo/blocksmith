package it.fulminazzo.blocksmith.command.node_old.node;//TODO: update
//package it.fulminazzo.blocksmith.command.node;
//
//import it.fulminazzo.blocksmith.action.PendingActionManager;
//import it.fulminazzo.blocksmith.command.annotation.Confirm;
//import it.fulminazzo.blocksmith.command.execution.CommandExecutionContext;
//import it.fulminazzo.blocksmith.command.execution.CommandExecutionException;
//import it.fulminazzo.blocksmith.message.argument.Placeholder;
//import lombok.*;
//import org.jetbrains.annotations.NotNull;
//import org.jetbrains.annotations.Nullable;
//
//import java.time.Duration;
//import java.util.*;
//
///**
// * A {@link CommandNode} to represent general literals.
// */
//@Data
//@EqualsAndHashCode(callSuper = true)
//@ToString(callSuper = true)
//public final class LiteralNode extends CommandNode {
//
//    /**
//     * Creates a clone of the current node, with the given literals.
//     *
//     * @param literals the new literals
//     * @return the clone
//     */
//    public @NotNull LiteralNode clone(final String @NotNull ... literals) {
//        LiteralNode clone = new LiteralNode(literals);
//        getChildren().forEach(clone::addChild);
//        getExecutionInfo().ifPresent(clone::setExecutionInfo);
//        getCommandInfo().ifPresent(clone::setCommandInfo);
//        clone.setCooldown(getCooldown());
//        clone.setConfirmationInfo(confirmAnnotation);
//        clone.setAsync(getAsyncTimeout());
//        return clone;
//    }
//
//}
