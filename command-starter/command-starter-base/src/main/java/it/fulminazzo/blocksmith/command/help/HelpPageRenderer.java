package it.fulminazzo.blocksmith.command.help;

import it.fulminazzo.blocksmith.command.CommandMessages;
import it.fulminazzo.blocksmith.command.CommandSenderWrapper;
import it.fulminazzo.blocksmith.command.visitor.InputVisitor;
import it.fulminazzo.blocksmith.message.Messenger;
import it.fulminazzo.blocksmith.message.util.ComponentUtils;
import it.fulminazzo.blocksmith.util.StringUtils;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

@RequiredArgsConstructor
public final class HelpPageRenderer {
    public static final @NotNull String DEFAULT_PERMISSION = "<gray>Permission</gray><dark_gray>:</dark_gray> ";
    public static final @NotNull String DEFAULT_USAGE = "<gray>Usage</gray><dark_gray>:</dark_gray> ";
    public static final @NotNull String DEFAULT_SUBCOMMANDS = "Subcommands";
    public static final @NotNull String DEFAULT_SUBCOMMAND_FORMAT =
            "<click:run_command:'%command%'>" +
                    "<hover:show_text:'<white>%usage%</white>\\n<gray>%permission%</gray>\\n\\n<aqua>Click for more information</aqua>'>" +
                    "<white>%name%</white> <dark_gray>-</dark_gray> <gray>%description%</gray>" +
                    "</hover>" +
                    "</click>";
    public static final @NotNull String DEFAULT_NO_SUBCOMMANDS = "\n  <red>(none)</red>\n ";
    public static final @NotNull String DEFAULT_PREVIOUS_PAGE = "<gold>[</gold><red>\\<\\<\\<</red><gold>]</gold>";
    public static final @NotNull String DEFAULT_NEXT_PAGE = "<gold>[</gold><red>\\>\\>\\></red><gold>]</gold>";
    public static final @NotNull String DEFAULT_CURRENT_PAGE =
            "<gold><strikethrough>--------</strikethrough></gold>" +
                    " <red>%page%</red><dark_gray>/</dark_gray><red>%pages%</red> " +
                    "<gold><strikethrough>--------</strikethrough></gold>";

    public static final String HELP_COMMAND_NAME = "help"; //TODO: configurable

    private static final @NotNull PlainTextComponentSerializer PLAIN_SERIALIZER = PlainTextComponentSerializer.plainText();

    private static final int MAX_FONT_WIDTH = 320;

    private static final int MAX_DESCRIPTION_LINES = 2;
    private static final int SUBCOMMANDS_LINES = 3;

    private final @NotNull HelpPage helpPage;
    private final @NotNull List<Component> lines = new LinkedList<>();

    public @NotNull List<Component> render(final @NotNull InputVisitor<?, ?> visitor, int page) {
        final HelpPageStyle style = HelpPageStyle.get();
        final Messenger messenger = visitor.getApplication().getMessenger();
        final CommandSenderWrapper<?> sender = visitor.getCommandSender();
        final Locale locale = sender.receiver().getLocale();

        final int pages = helpPage.getSubcommandsPages(sender, SUBCOMMANDS_LINES);
        // Imagine the sender requested the help page and got access to a next page.
        // Then, they lost some permissions, therefore the next page disappeared.
        // By using this calculation, we make sure that the renderer does not halt in this or similar scenarios
        page = Math.min(page, pages);
        // Header
        lines.add(formatAndFill(style.getHeader(), visitor, page, pages));
        renderDescription(messenger, locale);
        renderPermission(messenger, locale);
        renderUsage(messenger, locale);
        // Separator
        lines.add(formatAndFill(style.getSeparatorText(),visitor, page, pages));
        // Subcommands
        renderSubcommands(messenger, sender, page);
        // Footer
        lines.add(formatAndFill(style.getFooter(), visitor, page, pages));
        return lines;
    }

    /**
     * Renders the description component(s) for the given {@link Locale}.
     * <br>
     * If the description is missing, empty lines will be printed.
     *
     * @param messenger the messenger to get the description component from
     * @param locale    the locale
     */
    void renderDescription(final @NotNull Messenger messenger, final @NotNull Locale locale) {
        final Component description = messenger.getComponentOrNull(helpPage.getCommand().getDescription(), locale);
        List<Component> descriptionComponents = new ArrayList<>();
        if (description != null)
            descriptionComponents.addAll(truncateLines(description, MAX_DESCRIPTION_LINES));
        while (descriptionComponents.size() < MAX_DESCRIPTION_LINES)
            descriptionComponents.add(Component.text(""));
        lines.addAll(descriptionComponents);
    }

    /**
     * Renders the permission component for the given {@link Locale}.
     * <br>
     * If it could not be found, it falls back to {@link #DEFAULT_PERMISSION}.
     *
     * @param messenger the messenger to get the general permission component from
     * @param locale    the locale
     */
    void renderPermission(final @NotNull Messenger messenger, final @NotNull Locale locale) {
        final Component permissionComponent = getComponentOrElse(messenger, CommandMessages.HELP_COMMAND_PERMISSION, locale, DEFAULT_PERMISSION);
        Component permission = ComponentUtils.toComponent(helpPage.getCommand().getPermission().getPermission());
        permission = truncate(PLAIN_SERIALIZER.serialize(permissionComponent), permission);
        lines.add(permissionComponent.append(permission));
    }

    /**
     * Renders the usage component for the given {@link Locale}.
     * <br>
     * If it could not be found, it falls back to {@link #DEFAULT_USAGE}.
     *
     * @param messenger the messenger to get the general usage component from
     * @param locale    the locale
     */
    void renderUsage(final @NotNull Messenger messenger, final @NotNull Locale locale) {
        final Component usageComponent = getComponentOrElse(messenger, CommandMessages.HELP_COMMAND_USAGE, locale, DEFAULT_USAGE);
        Component usage = ComponentUtils.toComponent(helpPage.getCommand().getUsage());
        usage = truncate(PLAIN_SERIALIZER.serialize(usageComponent), usage);
        lines.add(usageComponent.append(usage));
    }

    /**
     * Renders all the subcommands for the given page.
     *
     * @param messenger the messenger to get the subcommand format component from
     * @param sender    to get the subcommands for
     * @param page      the requested page
     */
    void renderSubcommands(final @NotNull Messenger messenger,
                           final @NotNull CommandSenderWrapper<?> sender,
                           final @Range(from = 1, to = Integer.MAX_VALUE) int page) {
        final Locale locale = sender.receiver().getLocale();
        int rendered = 0;
        if (page > 0) {
            List<HelpPage.CommandData> subcommands = helpPage.getSubcommandsPage(sender, page, SUBCOMMANDS_LINES);
            for (HelpPage.CommandData command : subcommands) {
                renderSubcommand(messenger, locale, command);
                rendered++;
            }
        }
        if (rendered == 0)
            lines.add(getComponentOrElse(messenger, CommandMessages.HELP_COMMAND_NO_SUBCOMMANDS, locale, DEFAULT_NO_SUBCOMMANDS));
        else
            while (rendered++ < SUBCOMMANDS_LINES) lines.add(Component.text(""));
    }

    /**
     * Renders the given subcommand information in the subcommands section.
     *
     * @param messenger   the messenger to get the subcommand format component from
     * @param locale      the locale
     * @param commandData the subcommand data
     */
    void renderSubcommand(final @NotNull Messenger messenger,
                          final @NotNull Locale locale,
                          final @NotNull HelpPage.CommandData commandData) {
        Component subcommandComponent = getComponentOrElse(messenger, CommandMessages.HELP_COMMAND_SUBCOMMAND_FORMAT, locale, DEFAULT_SUBCOMMAND_FORMAT)
                .replaceText(r -> r.matchLiteral("%name%").replacement(commandData.getName()))
                .replaceText(r -> r.matchLiteral("%permission%").replacement(commandData.getPermission().getPermission()))
                .replaceText(r -> r
                        .matchLiteral("%description%")
                        .replacement(getComponentOrEmpty(messenger, commandData.getDescription(), locale))
                )
                .replaceText(r -> r
                        .matchLiteral("%usage%")
                        .replacement(ComponentUtils.toComponent(commandData.getUsage()))
                );
        int length = getMaxTruncationLength(PLAIN_SERIALIZER.serialize(subcommandComponent));
        if (length != -1) subcommandComponent = ComponentUtils.truncate(subcommandComponent, length);
        lines.add(subcommandComponent);
    }

    /**
     * Formats the given string with {@link #format(Component, InputVisitor, int, int)}.
     * Then, it fills it using {@link HelpPageStyle#getFiller()}.
     *
     * @param raw       the raw string
     * @param visitor   the current visitor handling the input
     * @param page      the page
     * @param pages     the total pages
     * @return the formatted component
     */
    @NotNull Component formatAndFill(final @NotNull String raw,
                                     final @NotNull InputVisitor<?, ?> visitor,
                                     final @Range(from = 0, to = Integer.MAX_VALUE) int page,
                                     final @Range(from = 0, to = Integer.MAX_VALUE) int pages) {
        Component baseComponent = format(ComponentUtils.toComponent(raw), visitor, page, pages);
        final HelpPageStyle style = HelpPageStyle.get();

        final String styleFiller = style.getFiller();
        StringBuilder fillers = new StringBuilder(styleFiller);
        String rawComponent = formatTitle(PLAIN_SERIALIZER.serialize(baseComponent));
        while (getMaxLength(rawComponent + (fillers + styleFiller).repeat(2)) == -1)
            fillers.append(styleFiller);

        String filler = fillers.toString();
        for (String s : style.getFillerStyles())
            filler = StringUtils.tag(s, filler);

        rawComponent = formatTitle(ComponentUtils.toString(baseComponent));
        return ComponentUtils.toComponent(filler + rawComponent + filler);
    }

    /**
     * Formats the given text with the following placeholders:
     * <ul>
     *     <li>{@code %filler%}: one character of the current {@link HelpPageStyle#getFiller()};</li>
     *     <li>{@code %name%}: the name of the command;</li>
     *     <li>{@code %subcommands%}: the title specified in the {@link it.fulminazzo.blocksmith.message.Messenger}
     *     under {@link CommandMessages#HELP_COMMAND_SUBCOMMANDS}.
     *     If it could not be found, it falls back to {@link #DEFAULT_SUBCOMMANDS}.</li>
     *     <li>{@code %previous%}: the title specified in the {@link it.fulminazzo.blocksmith.message.Messenger}
     *     under {@link CommandMessages#HELP_COMMAND_PREVIOUS_PAGE} (only shown if necessary);</li>
     *     <li>{@code %next%}: the title specified in the {@link it.fulminazzo.blocksmith.message.Messenger}
     *     under {@link CommandMessages#HELP_COMMAND_NEXT_PAGE} (only shown if necessary);</li>
     *     <li>{@code %current%}: the title specified in the {@link it.fulminazzo.blocksmith.message.Messenger}
     *     under {@link CommandMessages#HELP_COMMAND_CURRENT_PAGE}.</li>
     * </ul>
     *
     * @param component the component to format
     * @param visitor   the current visitor handling the input
     * @param page      the page
     * @param pages     the total pages
     * @return the formatted component
     */
    @NotNull Component format(final @NotNull Component component,
                              final @NotNull InputVisitor<?, ?> visitor,
                              final @Range(from = 0, to = Integer.MAX_VALUE) int page,
                              final @Range(from = 0, to = Integer.MAX_VALUE) int pages) {
        final HelpPageStyle style = HelpPageStyle.get();
        final Messenger messenger = visitor.getApplication().getMessenger();
        final Locale locale = visitor.getCommandSender().receiver().getLocale();
        final String currentInput = visitor.getInput().getPartialRawInput();
        Component previousPage = page > 1 || style.isAlwaysShowPreviousPage() ?
                replacePagePlaceholders(
                        getComponentOrElse(messenger, CommandMessages.HELP_COMMAND_PREVIOUS_PAGE, locale, DEFAULT_PREVIOUS_PAGE),
                        page,
                        pages
                ) : Component.empty();
        if (page > 1) previousPage.clickEvent(ClickEvent.clickEvent(
                ClickEvent.Action.RUN_COMMAND,
                ClickEvent.Payload.string(String.format("%s %s %s", currentInput, HELP_COMMAND_NAME, page - 1))
        ));
        Component nextPage = page < pages || style.isAlwaysShowNextPage() ?
                replacePagePlaceholders(
                        getComponentOrElse(messenger, CommandMessages.HELP_COMMAND_NEXT_PAGE, locale, DEFAULT_NEXT_PAGE),
                        page,
                        pages
                ) : Component.empty();
        if (page < pages) nextPage.clickEvent(ClickEvent.clickEvent(
                ClickEvent.Action.RUN_COMMAND,
                ClickEvent.Payload.string(String.format("%s %s %s", currentInput, HELP_COMMAND_NAME, page + 1))
        ));
        Component currentPage = pages > 0 || style.isAlwaysShowCurrentPage() ?
                replacePagePlaceholders(
                        getComponentOrElse(messenger, CommandMessages.HELP_COMMAND_CURRENT_PAGE, locale, DEFAULT_CURRENT_PAGE),
                        page,
                        pages
                ) : Component.empty();
        //TODO: proper testing
        return component
                .replaceText(b -> b.matchLiteral("%filler%").replacement(style.getStyledFiller()))
                .replaceText(b -> b.matchLiteral("%name%").replacement(helpPage.getCommand().getName()))
                .replaceText(b -> b.matchLiteral("%subcommands%")
                        .replacement(getComponentOrElse(messenger, CommandMessages.HELP_COMMAND_SUBCOMMANDS, locale, DEFAULT_SUBCOMMANDS)))
                .replaceText(b -> b.matchLiteral("%previous%").replacement(previousPage))
                .replaceText(b -> b.matchLiteral("%current%").replacement(currentPage))
                .replaceText(b -> b.matchLiteral("%next%").replacement(nextPage))
                ;
    }

    /**
     * Given a component, it attempts to subdivide it into multiple components for the given number of lines.
     * The last component will be truncated with {@link #truncate(String, Component)}.
     *
     * @param component the component
     * @param lines     the maximum number of lines to show
     * @return the components
     */
    static @NotNull List<@NotNull Component> truncateLines(@NotNull Component component, final int lines) {
        List<Component> components = new ArrayList<>();
        for (int i = 0; i < lines; i++) {
            String raw = PLAIN_SERIALIZER.serialize(component);
            if (raw.isEmpty()) break;
            else if (i == lines - 1) components.add(truncate("", component));
            else {
                int length = getMaxTruncationLength(raw);
                if (length == -1) {
                    components.add(component);
                    return components;
                } else {
                    length++;
                    components.add(ComponentUtils.subcomponent(component, 0, length));
                    component = ComponentUtils.subcomponent(component, length, raw.length());
                }
            }
        }
        return components;
    }

    /**
     * Checks if the given component needs to be truncated (a.k.a. it exceeds {@link #MAX_FONT_WIDTH}).
     * If it does, then a new cut component is returned that when hovering on will display the full text.
     *
     * @param prefix    the prefix to keep into account when checking for the total length
     * @param component the component
     * @return the (truncated) component
     */
    static @NotNull Component truncate(final @NotNull String prefix, final @NotNull Component component) {
        int length = getMaxTruncationLength(prefix + PLAIN_SERIALIZER.serialize(component));
        if (length == -1) return component;
        length -= prefix.length();
        if (length < 0) length = 0;
        return ComponentUtils.truncate(component, length).hoverEvent(HoverEvent.showText(component));
    }

    /**
     * Given the string, returns the index at which it should be <b>truncated</b>,
     * so that when rendered it does not exceed {@link #MAX_FONT_WIDTH} pixels.
     * <br>
     * Takes into account that the string should end with {@link ComponentUtils#TRUNCATE_SUFFIX}
     *
     * @param string the string
     * @return the index (or {@code -1} if it does not need to be extracted)
     */
    static int getMaxTruncationLength(final @NotNull String string) {
        if (getMaxLength(string) == -1) return -1;
        final String truncateSuffix = ComponentUtils.TRUNCATE_SUFFIX;
        final StringBuilder tmp = new StringBuilder();
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            tmp.append(c);
            if (MinecraftFontWidth.getWidth(tmp.append(truncateSuffix)) > MAX_FONT_WIDTH)
                return i - 1;
            else tmp.setLength(tmp.length() - truncateSuffix.length());
        }
        // this should be impossible
        return -1;
    }

    /**
     * Given the string, returns the index at which it should be <b>truncated</b>
     * so that when rendered it does not exceed {@link #MAX_FONT_WIDTH} pixels.
     *
     * @param string the string
     * @return the index (or {@code -1} if it does not need to be extracted)
     */
    static int getMaxLength(final @NotNull String string) {
        StringBuilder tmp = new StringBuilder();
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            tmp.append(c);
            if (MinecraftFontWidth.getWidth(tmp) > MAX_FONT_WIDTH)
                return i - 1;
        }
        return -1;
    }

    private static @NotNull Component getComponentOrEmpty(final @NotNull Messenger messenger,
                                                          final @NotNull String messageCode,
                                                          final @NotNull Locale locale) {
        return getComponentOrElse(messenger, messageCode, locale, "");
    }

    private static @NotNull Component getComponentOrElse(final @NotNull Messenger messenger,
                                                         final @NotNull String messageCode,
                                                         final @NotNull Locale locale,
                                                         final @NotNull String alternative) {
        Component component = messenger.getComponentOrNull(messageCode, locale);
        if (component == null) component = ComponentUtils.toComponent(alternative);
        return component;
    }

    private static @NotNull Component replacePagePlaceholders(final @NotNull Component component,
                                                              final int page,
                                                              final int pages) {
        return component
                .replaceText(b -> b.matchLiteral("%page%").replacement(String.valueOf(page)))
                .replaceText(b -> b.matchLiteral("%pages%").replacement(String.valueOf(pages)));
    }

    private static @NotNull String formatTitle(final @NotNull String title) {
        return title.isEmpty() ? title : String.format(" %s ", title);
    }

}
