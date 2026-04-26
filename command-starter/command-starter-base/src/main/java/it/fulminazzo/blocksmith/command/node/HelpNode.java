package it.fulminazzo.blocksmith.command.node;

import it.fulminazzo.blocksmith.command.annotation.Help;
import it.fulminazzo.blocksmith.command.annotation.Permission;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * A special {@link CommandNode} that represents the help command associated with {@link Help}.
 */
@EqualsAndHashCode(callSuper = true, doNotUseGetters = true)
@ToString(callSuper = true, doNotUseGetters = true)
public final class HelpNode extends InjectedNode {
    private @NotNull Help helpAnnotation;

    /**
     * Instantiates a new Help node.
     *
     * @param helpAnnotation the {@link Help} annotation to get information from
     * @param parent         the parent
     */
    public HelpNode(final @NotNull Help helpAnnotation, final @NotNull LiteralNode parent) {
        super(
                helpAnnotation.aliases(),
                helpAnnotation.description(),
                helpAnnotation.permission(),
                parent
        );
        this.helpAnnotation = helpAnnotation;
        addChild(new HelpPageNode(parent));
    }

    @Override
    public @NotNull HelpNode merge(final @NotNull CommandNode node) {
        if (node instanceof HelpNode && isDefaultHelpAnnotation(helpAnnotation)) {
            HelpNode helpNode = (HelpNode) node;
            getAliases().clear();
            setCommandInfo(null);
            name = node.getName();
            helpAnnotation = helpNode.helpAnnotation;
        }
        return (HelpNode) super.merge(node);
    }

    private static boolean isDefaultHelpAnnotation(final @NotNull Help helpAnnotation) {
        if (!Arrays.equals(helpAnnotation.aliases(), new String[]{Help.DEFAULT_NAME})) return false;
        if (!helpAnnotation.description().isEmpty()) return false;
        Permission permission = helpAnnotation.permission();
        return permission.value().isEmpty() &&
                permission.group().isEmpty() &&
                permission.grant() == Permission.Grant.OP;
    }

}
