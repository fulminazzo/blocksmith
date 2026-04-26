package it.fulminazzo.blocksmith.command.node;

import it.fulminazzo.blocksmith.command.annotation.Help;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A special {@link CommandNode} that represents the help command associated with {@link Help}.
 */
@EqualsAndHashCode(callSuper = true, doNotUseGetters = true)
@ToString(callSuper = true, doNotUseGetters = true)
public final class HelpNode extends InjectedNode {
    private @Nullable Help helpAnnotation;

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
        if (node instanceof HelpNode && helpAnnotation == null) {
            HelpNode helpNode = (HelpNode) node;
            getAliases().clear();
            setCommandInfo(null);
            name = node.getName();
            helpAnnotation = helpNode.helpAnnotation;
        }
        return (HelpNode) super.merge(node);
    }

}
