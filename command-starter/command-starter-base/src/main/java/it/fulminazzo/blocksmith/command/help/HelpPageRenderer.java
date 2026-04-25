package it.fulminazzo.blocksmith.command.help;

import it.fulminazzo.blocksmith.command.CommandMessages;
import it.fulminazzo.blocksmith.command.CommandSenderWrapper;
import it.fulminazzo.blocksmith.command.visitor.InputVisitor;
import it.fulminazzo.blocksmith.message.Messenger;
import it.fulminazzo.blocksmith.message.argument.Placeholder;
import it.fulminazzo.blocksmith.message.util.ComponentUtils;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
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
import java.util.stream.Collectors;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class HelpPageRenderer {
    public static final String HELP_COMMAND_NAME = "help"; //TODO: configurable

    private static final @NotNull PlainTextComponentSerializer PLAIN_SERIALIZER = PlainTextComponentSerializer.plainText();

    private static final int MAX_FONT_WIDTH = 320;

    private static final int MAX_DESCRIPTION_LINES = 2;
    private static final int SUBCOMMANDS_LINES = 3;

    @NotNull HelpPage helpPage;
    @NotNull InputVisitor<?, ?> visitor;
    @NotNull List<Component> lines = new LinkedList<>();

    @NotNull Messenger messenger;
    @NotNull CommandSenderWrapper<?> sender;
    @NotNull Locale locale;

    @NotNull HelpPageStyle style;

    /**
     * Instantiates a new Help page renderer.
     *
     * @param helpPage the help page to render
     * @param visitor  the visitor requesting the rendering
     */
    public HelpPageRenderer(final @NotNull HelpPage helpPage, final @NotNull InputVisitor<?, ?> visitor) {
        this.helpPage = helpPage;
        this.visitor = visitor;
        this.messenger = visitor.getApplication().getMessenger();
        this.sender = visitor.getCommandSender();
        this.locale = sender.receiver().getLocale();
        this.style = new HelpPageStyle(messenger, locale);
        /*
         * The logic of the next code is the following:
         * the user requested the help page of a command. They can either have written
         * /clan help or /clan help 1
         * In either case, when it comes to the parsing of the arguments,
         * since the page number is an optional parameter, if not given
         * an artificial one will be added.
         * Therefore, to go back to the root command (while preserving the arguments)
         * we retrocede the cursor by 2.
         */
        visitor.getInput().retrocedeCursor().retrocedeCursor();
    }

    public @NotNull List<Component> render(int page) {
        final HelpPageStyleOld style = HelpPageStyleOld.get();

        final int pages = helpPage.getSubcommandsPages(sender, SUBCOMMANDS_LINES);
        // Imagine the sender requested the help page and got access to a next page.
        // Then, they lost some permissions, therefore the next page disappeared.
        // By using this calculation, we make sure that the renderer does not halt in this or similar scenarios
        page = Math.min(page, pages);
        // Header
        lines.add(formatAndFill(style.getHeader(), page, pages));
        renderDescription();
        renderPermission();
        renderUsage();
        // Separator
        lines.add(formatAndFill(style.getSeparatorText(), page, pages));
        // Subcommands
        renderSubcommands(page);
        // Footer
        lines.add(formatAndFill(style.getFooter(), page, pages));
        return lines;
    }

    /**
     * Renders the description component(s) for the given {@link Locale}.
     * <br>
     * If the description is missing, empty lines will be printed.
     */
    void renderDescription() {
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
     */
    void renderPermission() {
        final Component permissionComponent = style.getPermissionComponent();
        Component permission = ComponentUtils.toComponent(helpPage.getCommand().getPermission().getPermission());
        permission = truncate(PLAIN_SERIALIZER.serialize(permissionComponent), permission);
        lines.add(permissionComponent.append(permission));
    }

    /**
     * Renders the usage component for the given {@link Locale}.
     */
    void renderUsage() {
        final Component usageComponent = style.getUsageComponent();
        Component usage = ComponentUtils.toComponent(helpPage.getCommand().getUsage());
        usage = truncate(PLAIN_SERIALIZER.serialize(usageComponent), usage);
        lines.add(usageComponent.append(usage));
    }

    /**
     * Renders all the subcommands for the given page.
     *
     * @param page the requested page
     */
    void renderSubcommands(final @Range(from = 1, to = Integer.MAX_VALUE) int page) {
        int rendered = 0;
        if (page > 0) {
            List<HelpPage.CommandData> subcommands = helpPage.getSubcommandsPage(sender, page, SUBCOMMANDS_LINES);
            for (HelpPage.CommandData command : subcommands) {
                renderSubcommand(command);
                rendered++;
            }
        }
        if (rendered == 0)
            lines.add(style.getNoSubcommandsComponent());
        else
            while (rendered++ < SUBCOMMANDS_LINES) lines.add(Component.text(""));
    }

    /**
     * Renders the given subcommand information in the subcommands section.
     *
     * @param commandData the subcommand data
     */
    void renderSubcommand(final @NotNull HelpPage.CommandData commandData) {
        final String currentInput = visitor.getInput().getPartialRawInput();
        Component subcommandComponent = style.getSubcommandFormat(
                Placeholder.of("name", commandData.getName()),
                Placeholder.of("permission", commandData.getPermission().getPermission()),
                Placeholder.of("description", messenger.getComponentOrElse(commandData.getDescription(), locale, "")),
                Placeholder.of("usage", commandData.getUsage())
        ).clickEvent(ClickEvent.runCommand(currentInput + " " + commandData.getName() + " " + HELP_COMMAND_NAME));
        int length = getMaxTruncationLength(PLAIN_SERIALIZER.serialize(subcommandComponent));
        if (length != -1) subcommandComponent = ComponentUtils.truncate(subcommandComponent, length);
        lines.add(subcommandComponent);
    }

    /**
     * Formats the given string with {@link #format(Component, int, int)}.
     * Then, it fills it using {@link HelpPageStyleOld#getFiller()}.
     *
     * @param raw   the raw string
     * @param page  the page
     * @param pages the total pages
     * @return the formatted component
     */
    @NotNull Component formatAndFill(final @NotNull String raw,
                                     final @Range(from = 0, to = Integer.MAX_VALUE) int page,
                                     final @Range(from = 0, to = Integer.MAX_VALUE) int pages) {
        //TODO: implement
        throw new UnsupportedOperationException();
    }

    /**
     * Formats the given text with the following placeholders:
     * <ul>
     *     <li>{@code %filler%}: one character of the current {@link HelpPageStyleOld#getFiller()};</li>
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
     * @param page      the page
     * @param pages     the total pages
     * @return the formatted component
     */
    @NotNull Component format(final @NotNull Component component,
                              final @Range(from = 0, to = Integer.MAX_VALUE) int page,
                              final @Range(from = 0, to = Integer.MAX_VALUE) int pages) {
        //TODO: implement
        throw new UnsupportedOperationException();
    }

    /**
     * Given a component, it attempts to subdivide it into multiple components for the given number of lines.
     * The last component will be truncated with {@link #truncate(String, Component)}.
     *
     * @param component the component
     * @param lines     the maximum number of lines to show
     * @return the components
     */
    static @NotNull List<@NotNull Component> truncateLines(final @NotNull Component component, final int lines) {
        Component comp = component;
        List<Component> components = new ArrayList<>();
        for (int i = 0; i < lines; i++) {
            String raw = PLAIN_SERIALIZER.serialize(comp);
            if (raw.isEmpty()) break;
            else if (i == lines - 1) components.add(truncate("", comp));
            else {
                int length = getMaxTruncationLength(raw);
                if (length == -1) {
                    components.add(comp);
                    return components;
                } else {
                    length++;
                    components.add(ComponentUtils.subcomponent(comp, 0, length));
                    comp = ComponentUtils.subcomponent(comp, length, raw.length());
                }
            }
        }
        return components.stream()
                .map(c -> c.hoverEvent(HoverEvent.showText(component)))
                .collect(Collectors.toList());
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

}
