package it.fulminazzo.blocksmith.command.help;

import it.fulminazzo.blocksmith.command.CommandMessages;
import it.fulminazzo.blocksmith.message.Messenger;
import it.fulminazzo.blocksmith.message.argument.Argument;
import it.fulminazzo.blocksmith.message.util.ComponentUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class HelpPageStyle {
    public static final @NotNull String DEFAULT_FILLER = "<gold><strikethrough>-</strikethrough></gold>";

    public static final @NotNull String DEFAULT_PERMISSION = "<gray>Permission</gray><dark_gray>:</dark_gray> ";
    public static final @NotNull String DEFAULT_USAGE = "<gray>Usage</gray><dark_gray>:</dark_gray> ";
    public static final @NotNull String DEFAULT_SUBCOMMANDS = "Subcommands";
    public static final @NotNull String DEFAULT_SUBCOMMAND_FORMAT =
            "<hover:show_text:'<white>%usage%</white>\n<gray>%permission%</gray>\n\n<aqua>Click for more information</aqua>'>" +
                    "<white>%name%</white> <dark_gray>-</dark_gray> <gray>%description%</gray>" +
                    "</hover>";
    public static final @NotNull String DEFAULT_NO_SUBCOMMANDS = "\n  <red>(none)</red>\n ";

    @NotNull Messenger messenger;
    @NotNull Locale locale;

    public @NotNull Component getFillerComponent() {
        return getComponentOrElse(CommandMessages.HELP_COMMAND_FILLER, DEFAULT_FILLER);
    }

    public @NotNull Component getPermissionComponent() {
        return getComponentOrElse(CommandMessages.HELP_COMMAND_PERMISSION, DEFAULT_PERMISSION);
    }

    public @NotNull Component getUsageComponent() {
        return getComponentOrElse(CommandMessages.HELP_COMMAND_USAGE, DEFAULT_USAGE);
    }

    public @NotNull Component getSubcommandsComponent() {
        return getComponentOrElse(CommandMessages.HELP_COMMAND_SUBCOMMANDS, DEFAULT_SUBCOMMANDS);
    }

    public @NotNull Component getNoSubcommandsComponent() {
        return getComponentOrElse(CommandMessages.HELP_COMMAND_NO_SUBCOMMANDS, DEFAULT_NO_SUBCOMMANDS);
    }

    public @NotNull Component getSubcommandFormat(final @NotNull Argument @NotNull ... arguments) {
        return getComponentOrElse(CommandMessages.HELP_COMMAND_SUBCOMMAND_FORMAT, DEFAULT_SUBCOMMAND_FORMAT, arguments);
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
