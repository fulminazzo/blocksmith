package it.fulminazzo.blocksmith.command.help;

import it.fulminazzo.blocksmith.command.CommandSenderWrapper;
import it.fulminazzo.blocksmith.command.visitor.InputVisitor;
import it.fulminazzo.blocksmith.message.Messenger;
import it.fulminazzo.blocksmith.message.util.ComponentUtils;
import it.fulminazzo.blocksmith.util.StringUtils;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

@RequiredArgsConstructor
public final class HelpPageRenderer {
    private static final @NotNull PlainTextComponentSerializer PLAIN_SERIALIZER = PlainTextComponentSerializer.plainText();

    private static final int MAX_FONT_WIDTH = 320;

    private static final int MAX_DESCRIPTION_LINES = 2;
    private static final int SUBCOMMANDS_LINES = 3;

    private final @NotNull HelpPage helpPage;
    private final @NotNull List<Component> lines = new LinkedList<>();

    public @NotNull List<Component> render(final @NotNull InputVisitor<?, ?> visitor, final int page) {
        final HelpPageStyle style = HelpPageStyle.get();
        final Messenger messenger = visitor.getApplication().getMessenger();
        final CommandSenderWrapper<?> sender = visitor.getCommandSender();
        final Locale locale = sender.receiver().getLocale();
        // Header
        lines.add(formatAndFill(style.getHeader(), messenger, locale));
        renderDescription(messenger, locale);
        renderPermission(messenger, locale);
        renderUsage(messenger, locale);
        // Separator
        lines.add(formatAndFill(style.getSeparatorText(), messenger, locale));
        // Subcommands
        renderSubcommands(messenger, sender, page);
        // Footer
        lines.add(formatAndFill(style.getFooter(), messenger, locale));
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
     *
     * @param messenger the messenger to get the general permission component from
     * @param locale    the locale
     */
    void renderPermission(final @NotNull Messenger messenger, final @NotNull Locale locale) {
        final Component permissionComponent = getComponentOrEmpty(messenger, "command.help.permission", locale);
        Component permission = ComponentUtils.toComponent(helpPage.getCommand().getPermission().getPermission());
        permission = truncate(PLAIN_SERIALIZER.serialize(permissionComponent), permission);
        lines.add(permissionComponent.append(permission));
    }

    /**
     * Renders the usage component for the given {@link Locale}.
     *
     * @param messenger the messenger to get the general usage component from
     * @param locale    the locale
     */
    void renderUsage(final @NotNull Messenger messenger, final @NotNull Locale locale) {
        final Component usageComponent = getComponentOrEmpty(messenger, "command.help.usage", locale);
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
                           final int page) {
        int rendered = 0;
        if (page > 0) {
            int pages = helpPage.getSubcommandsPages(sender, SUBCOMMANDS_LINES);
            if (page <= pages) {
                List<HelpPage.CommandData> subcommands = helpPage.getSubcommandsPage(sender, page, SUBCOMMANDS_LINES);
                for (HelpPage.CommandData command : subcommands) {
                    renderSubcommand(messenger, sender.receiver().getLocale(), command);
                    rendered++;
                }
            }
        }
        //TODO: if empty display message
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
        Component subcommandComponent = getComponentOrEmpty(messenger, "command.help.subcommand-format", locale)
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
        //TODO: hover event
        //TODO: click event
        lines.add(subcommandComponent);
    }

    /**
     * Formats the given string with {@link #format(Component, Messenger, Locale)}.
     * Then, it fills it using {@link HelpPageStyle#getFiller()}.
     *
     * @param raw       the raw string
     * @param messenger the messenger to get the messages from
     * @param locale    the locale
     * @return the formatted component
     */
    @NotNull Component formatAndFill(final @NotNull String raw,
                                     final @NotNull Messenger messenger,
                                     final @NotNull Locale locale) {
        Component baseComponent = format(ComponentUtils.toComponent(raw), messenger, locale);
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
     *     under "command.help.subcommands";</li> //TODO: constant class
     *     <li>{@code %previous%}: the title specified in the {@link it.fulminazzo.blocksmith.message.Messenger}
     *     under "command.help.previous-page" (only shown if necessary);</li> //TODO: option to disable
     *     <li>{@code %next%}: the title specified in the {@link it.fulminazzo.blocksmith.message.Messenger}
     *     under "command.help.next-page" (only shown if necessary);</li> //TODO: option to disable
     *     <li>{@code %current%}: the title specified in the {@link it.fulminazzo.blocksmith.message.Messenger}
     *     under "command.help.current-page".</li>
     * </ul>
     *
     * @param component the component to format
     * @param messenger the messenger to get the messages from
     * @param locale    the locale
     * @return the formatted component
     */
    @NotNull Component format(final @NotNull Component component,
                              final @NotNull Messenger messenger,
                              final @NotNull Locale locale) {
        HelpPageStyle style = HelpPageStyle.get();
        //TODO: proper testing
        return component
                .replaceText(b -> b.matchLiteral("%filler%").replacement(style.getStyledFiller()))
                .replaceText(b -> b.matchLiteral("%name%").replacement(helpPage.getCommand().getName()))
                .replaceText(b -> b.matchLiteral("%subcommands%")
                        .replacement(getComponentOrEmpty(messenger, "command.help.subcommands", locale)))
                //TODO: missing logic
                .replaceText(b -> b.matchLiteral("%previous%")
                        .replacement(getComponentOrEmpty(messenger, "command.help.previous-page", locale)))
                //TODO: missing logic
                .replaceText(b -> b.matchLiteral("%next%")
                        .replacement(getComponentOrEmpty(messenger, "command.help.next-page", locale)))
                //TODO: missing logic
                .replaceText(b -> b.matchLiteral("%current%")
                        .replacement(getComponentOrEmpty(messenger, "command.help.current-page", locale)));
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
        Component component = messenger.getComponentOrNull(messageCode, locale);
        if (component == null) component = Component.text("");
        return component;
    }

    private static @NotNull String formatTitle(final @NotNull String title) {
        return title.isEmpty() ? title : String.format(" %s ", title);
    }

}
