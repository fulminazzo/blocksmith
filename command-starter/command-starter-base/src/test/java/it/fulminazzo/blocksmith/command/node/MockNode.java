//TODO: update
//package it.fulminazzo.blocksmith.command.node;
//
//import it.fulminazzo.blocksmith.command.execution.CommandExecutionContext;
//import lombok.EqualsAndHashCode;
//import lombok.ToString;
//import lombok.Value;
//import org.jetbrains.annotations.NotNull;
//
//import java.util.List;
//import java.util.Set;
//
//@Value
//@EqualsAndHashCode(callSuper = true)
//@ToString(callSuper = true)
//public class MockNode extends CommandNode implements Cloneable {
//    @NotNull String name;
//
//    @Override
//    public @NotNull List<String> getCompletions(@NotNull CommandExecutionContext context) {
//        throw new UnsupportedOperationException();
//    }
//
//    @Override
//    protected void processInput(@NotNull CommandExecutionContext context) {
//        throw new UnsupportedOperationException();
//    }
//
//    @Override
//    protected void validateTabCompleteInput(@NotNull CommandExecutionContext context) {
//        throw new UnsupportedOperationException();
//    }
//
//    @Override
//    public boolean matches(final @NotNull String token) {
//        return token.equals(name);
//    }
//
//    @Override
//    protected MockNode clone() throws CloneNotSupportedException {
//        MockNode clone = new MockNode(name);
//        @NotNull Set<CommandNode> children = clone.getChildren();
//        for (CommandNode child : getChildren())
//            if (child instanceof MockNode)
//                children.add(((MockNode) child).clone());
//            else
//                throw new CloneNotSupportedException("Clone not supported on node: " + child.getClass().getCanonicalName());
//        getExecutionInfo().ifPresent(clone::setExecutionInfo);
//        return clone;
//    }
//
//}
