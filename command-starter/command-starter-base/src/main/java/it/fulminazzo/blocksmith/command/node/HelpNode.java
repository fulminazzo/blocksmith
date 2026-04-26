package it.fulminazzo.blocksmith.command.node;

import it.fulminazzo.blocksmith.command.annotation.Help;
import it.fulminazzo.blocksmith.command.help.HelpPage;
import it.fulminazzo.blocksmith.command.help.HelpPageRenderer;
import it.fulminazzo.blocksmith.reflect.Reflect;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * A special {@link CommandNode} that represents the help command associated with {@link Help}.
 */
@EqualsAndHashCode(callSuper = true, doNotUseGetters = true)
@ToString(callSuper = true, doNotUseGetters = true)
public final class HelpNode extends InjectedNode {
    private final @Nullable Help helpAnnotation;

    /**
     * Instantiates a new Help node.
     *
     * @param helpAnnotation the {@link Help} annotation to get information from
     * @param parent         the parent
     */
    public HelpNode(final @Nullable Help helpAnnotation, final @NotNull LiteralNode parent) {
        super(
                helpAnnotation == null ? new String[]{Help.DEFAULT_NAME} : helpAnnotation.aliases(),
                helpAnnotation == null ? "" : helpAnnotation.description(),
                helpAnnotation == null ? "" : helpAnnotation.permission(),
                parent
        );
        this.helpAnnotation = helpAnnotation;
        //TODO: dynamic resolution of min and max
        //TODO: better handling
        Method method = Reflect.on(this).getMethod("unused", int.class);
        Parameter parameter = method.getParameters()[0];
        NumberArgumentNode<?> page = (NumberArgumentNode<?>) (Object) ArgumentNode.of("page", parameter, true);
        page.setDefaultValue("1");
        page.setExecutor((n, v) -> {
            HelpPage helpPage = HelpPage.create(n);
            int pageArgument = (int) v.getArguments().getLast();
            new HelpPageRenderer(helpPage, v, pageArgument).render().forEach(l ->
                    v.getCommandSender().sendMessage(l)
            );
        });
    }

    @Override
    public @NotNull HelpNode merge(final @NotNull CommandNode node) {
        if (node instanceof HelpNode && helpAnnotation == null) {
            getAliases().clear();
            setCommandInfo(null);
        }
        return (HelpNode) super.merge(node);
    }

    @SuppressWarnings("unused")
    private static void unused(final int page) {
        // mock function, used to pass a parameter to the page node
    }

}
