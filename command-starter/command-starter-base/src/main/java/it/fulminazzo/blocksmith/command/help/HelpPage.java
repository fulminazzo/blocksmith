package it.fulminazzo.blocksmith.command.help;

import it.fulminazzo.blocksmith.command.node.LiteralNode;
import it.fulminazzo.blocksmith.command.node.info.CommandInfo;
import lombok.Builder;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

/**
 * A holder for information about a help page.
 */
@Value
@Builder
public class HelpPage {
    @NotNull String name;
    @NotNull String description;
    @NotNull String permission;
    @NotNull String usage;

    /**
     * Creates a new Help page.
     *
     * @param commandNode the command node
     * @return the help page
     */
    public @NotNull HelpPage create(final @NotNull LiteralNode commandNode) {
        CommandInfo commandInfo = commandNode.getCommandInfo();
        return builder()
                .name(commandNode.getName())
                .description(commandInfo.getDescription())
                .permission(commandInfo.getPermission().getPermission())
                .usage(commandNode.getUsage())
                .build();
    }

}
