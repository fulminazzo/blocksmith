package it.fulminazzo.blocksmith.command.help;

import it.fulminazzo.blocksmith.command.CommandSenderWrapper;
import it.fulminazzo.blocksmith.command.node.LiteralNode;
import it.fulminazzo.blocksmith.command.node.info.CommandInfo;
import it.fulminazzo.blocksmith.command.node.info.PermissionInfo;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A holder for information about a help page.
 */
@Value
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class HelpPage {
    @NotNull CommandData command;
    @NotNull List<CommandData> subcommands;

    /**
     * Gets all the subcommands executable from the given sender.
     *
     * @param sender the sender
     * @return the subcommands
     */
    public @NotNull List<CommandData> getExecutableSubcommands(final @NotNull CommandSenderWrapper<?> sender) {
        return subcommands.stream()
                .filter(c -> sender.hasPermission(c.getPermission()))
                .collect(Collectors.toList());
    }

    /**
     * Creates a new Help page.
     *
     * @param commandNode the command node
     * @return the help page
     */
    public static @NotNull HelpPage create(final @NotNull LiteralNode commandNode) {
        return new HelpPage(
                CommandData.create(commandNode),
                commandNode.getSubcommands().stream()
                        .map(CommandData::create)
                        .collect(Collectors.toList())
        );
    }

    @Value
    @Builder
    public static class CommandData {
        @NotNull String name;
        @NotNull String description;
        @NotNull PermissionInfo permission;
        @NotNull String usage;

        /**
         * Creates a new Command data.
         *
         * @param commandNode the command node
         * @return the command data
         */
        public static @NotNull CommandData create(final @NotNull LiteralNode commandNode) {
            CommandInfo commandInfo = commandNode.getCommandInfo();
            return builder()
                    .name(commandNode.getName())
                    .description(commandInfo.getDescription())
                    .permission(commandInfo.getPermission())
                    .usage(commandNode.getUsage())
                    .build();
        }

    }

}
