package it.fulminazzo.blocksmith.command.node;

import it.fulminazzo.blocksmith.command.argument.ArgumentParseException;
import it.fulminazzo.blocksmith.command.argument.ArgumentParsers;
import it.fulminazzo.blocksmith.command.help.HelpPage;
import it.fulminazzo.blocksmith.command.help.HelpPageRenderer;
import it.fulminazzo.blocksmith.command.visitor.InputVisitor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Identifies the {@link ArgumentNode} of a {@link HelpNode}.
 */
@EqualsAndHashCode(callSuper = true, doNotUseGetters = true)
@ToString(callSuper = true, doNotUseGetters = true)
final class HelpPageNode extends ArgumentNode<Integer> {
    private final @NotNull LiteralNode commandNode;

    /**
     * Instantiates a new Help page node.
     *
     * @param commandNode the command node
     */
    HelpPageNode(final @NotNull LiteralNode commandNode) {
        super("page", true);
        this.commandNode = commandNode;
        setDefaultValue("1");
        setExecutor((h, v) -> {
            HelpPage helpPage = getHelpPage();
            int pageArgument = (int) v.getArguments().getLast();
            new HelpPageRenderer(helpPage, v, pageArgument).render().forEach(l ->
                    v.getCommandSender().sendMessage(l)
            );
        });
    }

    /**
     * Gets the help page derived from the command node.
     *
     * @return the command node
     */
    @NotNull HelpPage getHelpPage() {
        return HelpPage.create(commandNode);
    }

    boolean isValid(final @NotNull InputVisitor<?, ?> visitor, final int page) {
        int pages = getHelpPage().getSubcommandsPages(visitor.getCommandSender(), HelpPageRenderer.SUBCOMMANDS_LINES);
        return page >= 1 && page <= pages;
    }

    @Override
    protected @Nullable Integer parseCurrentImpl(final @NotNull InputVisitor<?, ?> visitor) throws ArgumentParseException {
        return (Integer) ArgumentParsers.of(getType()).parse(visitor);
    }

    @Override
    public @NotNull List<String> getCompletions(final @NotNull InputVisitor<?, ?> visitor) {
        List<String> completions = super.getCompletions(visitor);
        completions.removeIf(s -> !isValid(visitor, Integer.parseInt(s)));
        return completions;
    }

    @Override
    public @NotNull Class<Integer> getType() {
        return Integer.class;
    }

}
