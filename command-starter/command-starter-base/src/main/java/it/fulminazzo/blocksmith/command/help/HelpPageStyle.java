package it.fulminazzo.blocksmith.command.help;

import it.fulminazzo.blocksmith.command.CommandMessages;
import it.fulminazzo.blocksmith.message.Messenger;
import it.fulminazzo.blocksmith.message.argument.Argument;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

/**
 * Holds all the default styling for a help page rendering.
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class HelpPageStyle {
    /**
     * The default filler text.
     */
    public static final @NotNull String DEFAULT_FILLER = "<gold><strikethrough>-</strikethrough></gold>";

    /**
     * The default header.
     */
    public static final @NotNull String DEFAULT_HEADER = "%filler% <white>%name%</white> %filler%";
    /**
     * The default separator.
     */
    public static final @NotNull String DEFAULT_SEPARATOR = "%filler% <white>Subcommands</white> %filler%";
    /**
     * The default footer.
     */
    public static final @NotNull String DEFAULT_FOOTER = "%filler%" +
            "%filler%" +
            "%previous%" +
            "%filler%" +
            "<gold>[</gold><red>%page%</red><dark_gray>/</dark_gray><red>%pages%</red><gold>]</gold>" +
            "%filler%" +
            "%next%" +
            "%filler%" +
            "%filler%";

    /**
     * The default permission text (the actual permission will be suffixed).
     */
    public static final @NotNull String DEFAULT_PERMISSION = "<gray>Permission</gray><dark_gray>:</dark_gray> ";
    /**
     * The default usage text (the actual usage will be suffixed).
     */
    public static final @NotNull String DEFAULT_USAGE = "<gray>Usage</gray><dark_gray>:</dark_gray> ";

    /**
     * The default subcommands format.
     */
    public static final @NotNull String DEFAULT_SUBCOMMAND_FORMAT =
            "<hover:show_text:'<white>%usage%</white>\n<gray>%permission%</gray>\n\n<aqua>Click for more information</aqua>'>" +
                    "<white>%name%</white> <dark_gray>-</dark_gray> <gray>%description%</gray>" +
                    "</hover>";
    /**
     * The default "no subcommands" text.
     */
    public static final @NotNull String DEFAULT_NO_SUBCOMMANDS = "\n  <red>(none)</red>\n ";

    /**
     * The default previous command button text.
     */
    public static final @NotNull String DEFAULT_PREVIOUS_COMMAND = "<gold>[</gold><red>↑</red><gold>]</gold>";
    /**
     * The default previous page button text.
     */
    public static final @NotNull String DEFAULT_PREVIOUS_PAGE = "<gold>[</gold><red><<</red><gold>]</gold>";
    /**
     * The default next page button text.
     */
    public static final @NotNull String DEFAULT_NEXT_PAGE = "<gold>[</gold><red>>></red><gold>]</gold>";

    @NotNull Messenger messenger;
    @NotNull Locale locale;

    /**
     * Gets the filler text from {@link CommandMessages#HELP_COMMAND_FILLER}.
     * <br>
     * If it could not be found, falls back to {@link #DEFAULT_FILLER}.
     *
     * @return the filler component
     */
    public @NotNull Component getFillerComponent() {
        return getComponentOrElse(CommandMessages.HELP_COMMAND_FILLER, DEFAULT_FILLER);
    }

    /**
     * Gets the header from {@link CommandMessages#HELP_COMMAND_HEADER}.
     * <br>
     * If it could not be found, falls back to {@link #DEFAULT_HEADER}.
     *
     * @return the header component
     */
    public @NotNull Component getHeaderComponent() {
        return getComponentOrElse(CommandMessages.HELP_COMMAND_HEADER, DEFAULT_HEADER);
    }

    /**
     * Gets the footer from {@link CommandMessages#HELP_COMMAND_FOOTER}.
     * <br>
     * If it could not be found, falls back to {@link #DEFAULT_FOOTER}.
     *
     * @return the footer component
     */
    public @NotNull Component getFooterComponent() {
        return getComponentOrElse(CommandMessages.HELP_COMMAND_FOOTER, DEFAULT_FOOTER);
    }

    /**
     * Gets the separator from {@link CommandMessages#HELP_COMMAND_SEPARATOR}.
     * <br>
     * If it could not be found, falls back to {@link #DEFAULT_SEPARATOR}.
     *
     * @return the separator component
     */
    public @NotNull Component getSeparatorComponent() {
        return getComponentOrElse(CommandMessages.HELP_COMMAND_SEPARATOR, DEFAULT_SEPARATOR);
    }

    /**
     * Gets the permission text from {@link CommandMessages#HELP_COMMAND_PERMISSION}.
     * <br>
     * If it could not be found, falls back to {@link #DEFAULT_PERMISSION}.
     *
     * @return the permission component
     */
    public @NotNull Component getPermissionComponent() {
        return getComponentOrElse(CommandMessages.HELP_COMMAND_PERMISSION, DEFAULT_PERMISSION);
    }

    /**
     * Gets the usage text from {@link CommandMessages#HELP_COMMAND_USAGE}.
     * <br>
     * If it could not be found, falls back to {@link #DEFAULT_USAGE}.
     *
     * @return the usage component
     */
    public @NotNull Component getUsageComponent() {
        return getComponentOrElse(CommandMessages.HELP_COMMAND_USAGE, DEFAULT_USAGE);
    }

    /**
     * Gets the "no subcommands" text from {@link CommandMessages#HELP_COMMAND_NO_SUBCOMMANDS}.
     * <br>
     * If it could not be found, falls back to {@link #DEFAULT_NO_SUBCOMMANDS}.
     *
     * @return the component
     */
    public @NotNull Component getNoSubcommandsComponent() {
        return getComponentOrElse(CommandMessages.HELP_COMMAND_NO_SUBCOMMANDS, DEFAULT_NO_SUBCOMMANDS);
    }

    /**
     * Gets the subcommand format from {@link CommandMessages#HELP_COMMAND_SUBCOMMAND_FORMAT}.
     * <br>
     * If it could not be found, falls back to {@link #DEFAULT_SUBCOMMAND_FORMAT}.
     *
     * @return the subcommand format
     */
    public @NotNull Component getSubcommandFormat() {
        return getComponentOrElse(CommandMessages.HELP_COMMAND_SUBCOMMAND_FORMAT, DEFAULT_SUBCOMMAND_FORMAT);
    }

    /**
     * Gets the previous command button text from {@link CommandMessages#HELP_COMMAND_PREVIOUS_COMMAND}.
     * <br>
     * If it could not be found, falls back to {@link #DEFAULT_PREVIOUS_COMMAND}.
     *
     * @return the previous command component
     */
    public @NotNull Component getPreviousCommandComponent() {
        return getComponentOrElse(CommandMessages.HELP_COMMAND_PREVIOUS_COMMAND, DEFAULT_PREVIOUS_COMMAND);
    }

    /**
     * Gets the previous page button text from {@link CommandMessages#HELP_COMMAND_PREVIOUS_PAGE}.
     * <br>
     * If it could not be found, falls back to {@link #DEFAULT_PREVIOUS_PAGE}.
     *
     * @return the previous page component
     */
    public @NotNull Component getPreviousPageComponent() {
        return getComponentOrElse(CommandMessages.HELP_COMMAND_PREVIOUS_PAGE, DEFAULT_PREVIOUS_PAGE);
    }

    /**
     * Gets the next page button text from {@link CommandMessages#HELP_COMMAND_NEXT_PAGE}.
     * <br>
     * If it could not be found, falls back to {@link #DEFAULT_NEXT_PAGE}.
     *
     * @return the next page component
     */
    public @NotNull Component getNextPageComponent() {
        return getComponentOrElse(CommandMessages.HELP_COMMAND_NEXT_PAGE, DEFAULT_NEXT_PAGE);
    }

    private @NotNull Component getComponentOrElse(final @NotNull String messageCode,
                                                  final @NotNull String alternative,
                                                  final @NotNull Argument @NotNull ... arguments) {
        return messenger.getComponentOrElse(
                messageCode,
                locale,
                alternative,
                arguments
        );
    }

}
