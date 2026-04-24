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
import org.jetbrains.annotations.Range;

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
     * Gets the subcommands corresponding to the requested page.
     *
     * @param sender             the sender to get the subcommands for
     * @param page               the page
     * @param subcommandsPerPage the number of subcommands shown per page
     * @return the subcommands of the page
     */
    public @NotNull List<CommandData> getSubcommandsPage(final @NotNull CommandSenderWrapper<?> sender,
                                                         final @Range(from = 1, to = Integer.MAX_VALUE) int page,
                                                         final @Range(from = 1, to = Integer.MAX_VALUE) int subcommandsPerPage) {
        int pages = getSubcommandsPages(sender, subcommandsPerPage);
        if (page < 1 || page > pages)
            throw new IllegalArgumentException(String.format("invalid page %s for pages [%s, %s]", page, 1, pages));
        int from = (page - 1) * subcommandsPerPage;
        int to = Math.min(page * subcommandsPerPage, subcommands.size());
        return subcommands.subList(from, to);
    }

    /**
     * Gets the number of pages of the subcommands section for the given sender.
     *
     * @param sender             the sender to get the subcommands for
     * @param subcommandsPerPage the number of subcommands shown per page
     * @return the number of pages
     */
    public int getSubcommandsPages(final @NotNull CommandSenderWrapper<?> sender,
                                   final int subcommandsPerPage) {
        int subcommands = getExecutableSubcommands(sender).size();
        return subcommands / subcommandsPerPage + Math.min(1, subcommands % subcommandsPerPage);
    }

    /**
     * Gets all the subcommands executable from the given sender.
     *
     * @param sender the sender to get the subcommands for
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

    /**
     * Holds information about a command.
     */
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
