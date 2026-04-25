package it.fulminazzo.blocksmith.command.help;

import it.fulminazzo.blocksmith.command.CommandSenderWrapper;
import it.fulminazzo.blocksmith.command.visitor.CommandInput;
import it.fulminazzo.blocksmith.command.visitor.InputVisitor;
import it.fulminazzo.blocksmith.message.Messenger;
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

/**
 * A renderer for the help page of the requested command.
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class HelpPageRenderer {
    private static final @NotNull PlainTextComponentSerializer PLAIN_SERIALIZER = PlainTextComponentSerializer.plainText();
    private static final @NotNull String FILLER_PLACEHOLDER = "%filler%";

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

    @Range(from = 0, to = Integer.MAX_VALUE)
    int pages;
    @Range(from = 0, to = Integer.MAX_VALUE)
    int page;

    /**
     * Instantiates a new Help page renderer.
     *
     * @param helpPage the help page to render
     * @param visitor  the visitor requesting the rendering
     * @param page     the page to render
     */
    public HelpPageRenderer(final @NotNull HelpPage helpPage,
                            final @NotNull InputVisitor<?, ?> visitor,
                            final @Range(from = 0, to = Integer.MAX_VALUE) int page) {
        this.helpPage = helpPage;
        this.visitor = visitor;
        this.messenger = visitor.getApplication().getMessenger();
        this.sender = visitor.getCommandSender();
        this.locale = sender.getLocale();
        this.style = new HelpPageStyle(messenger, locale);
        this.pages = helpPage.getSubcommandsPages(sender, SUBCOMMANDS_LINES);
        /*
         * Imagine the sender requested the help page and got access to the next page.
         * Then, they lost some permissions; therefore, the next page disappeared.
         * By using this calculation, we make sure that the renderer does not halt in this or similar scenarios
         */
        this.page = Math.min(page, pages);
        /*
         * The logic of the next code is the following:
         * the user requested the help page of a command. They can either have written
         * /clan help or /clan help 1
         * In either case, when it comes to the parsing of the arguments,
         * since the page number is an optional parameter, if not given,
         * an artificial one will be added.
         * Therefore, to go back to the root command (while preserving the arguments),
         * we retreat the cursor by 2.
         */
        visitor.getInput().retreatCursor().retreatCursor();
    }

    /**
     * Renders the help page in the form of <b>lines</b> for the Minecraft chat.
     *
     * @return the lines
     */
    public @NotNull List<Component> render() {
        lines.clear();
        renderHeader();
        renderDescription();
        renderPermission();
        renderUsage();
        renderSeparator();
        renderSubcommands();
        renderFooter();
        return lines;
    }

    /**
     * Renders the header component.
     */
    void renderHeader() {
        lines.add(formatPageButtons(style.getHeaderComponent()));
    }

    /**
     * Renders the description component(s).
     * <br>
     * If the description is missing, empty lines will be printed.
     */
    void renderDescription() {
        Component description = messenger.getComponentOrNull(helpPage.getCommand().getDescription(), locale);
        List<Component> descriptionComponents = new ArrayList<>();
        if (description != null) {
            description = description.replaceText(r -> r.matchLiteral("\n").replacement(Component.text(" ")));
            descriptionComponents.addAll(truncateLines(description, MAX_DESCRIPTION_LINES));
        }
        while (descriptionComponents.size() < MAX_DESCRIPTION_LINES)
            descriptionComponents.add(Component.text(""));
        lines.addAll(descriptionComponents);
    }

    /**
     * Renders the permission component.
     */
    void renderPermission() {
        Component permissionComponent = format(style.getPermissionComponent());
        Component permission = ComponentUtils.toComponent(helpPage.getCommand().getPermission().getPermission());
        permission = truncate(PLAIN_SERIALIZER.serialize(permissionComponent), permission);
        lines.add(permissionComponent.append(permission));
    }

    /**
     * Renders the usage component.
     */
    void renderUsage() {
        Component usageComponent = format(style.getUsageComponent());
        Component usage = ComponentUtils.toComponent(helpPage.getCommand().getUsage());
        usage = truncate(PLAIN_SERIALIZER.serialize(usageComponent), usage);
        lines.add(usageComponent.append(usage));
    }

    /**
     * Renders the separator between the command description and subcommands list.
     */
    void renderSeparator() {
        lines.add(formatPageButtons(style.getSeparatorComponent()));
    }

    /**
     * Renders all the subcommands for the given page.
     */
    void renderSubcommands() {
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
        Component subcommandComponent = format(style.getSubcommandFormat(), commandData)
                .clickEvent(ClickEvent.runCommand(currentInput + " " + commandData.getName() + " " + commandData.getHelpCommandName()));
        int length = getMaxTruncationLength(PLAIN_SERIALIZER.serialize(subcommandComponent));
        if (length != -1) subcommandComponent = ComponentUtils.truncate(subcommandComponent, length);
        lines.add(subcommandComponent);
    }

    /**
     * Renders the footer component.
     */
    void renderFooter() {
        lines.add(formatPageButtons(style.getFooterComponent()));
    }

    /**
     * Formats the given text with the following placeholders:
     * <ul>
     *     <li>{@code %name%}: the name of the given command;</li>
     *     <li>{@code %permission%}: the permission of the given command;</li>
     *     <li>{@code %description%}: the description of the given command;</li>
     *     <li>{@code %usage%}: the usage of the given command;</li>
     *     <li>{@code %filler%}: the value of
     *     {@link it.fulminazzo.blocksmith.command.CommandMessages#HELP_COMMAND_FILLER}
     *     (falls back to {@link HelpPageStyle#DEFAULT_FILLER} if not found)
     *     repeated until the component exceeds length {@link #MAX_FONT_WIDTH};</li>
     *     <li>{@code %page%}: the current page;</li>
     *     <li>{@code %pages%}: the total number of pages;</li>
     *     <li>{@code %back%}: the button associated with
     *     {@link it.fulminazzo.blocksmith.command.CommandMessages#HELP_COMMAND_PREVIOUS_COMMAND};</li>
     *     <li>{@code %previous%}: the button associated with
     *     {@link it.fulminazzo.blocksmith.command.CommandMessages#HELP_COMMAND_PREVIOUS_PAGE};</li>
     *     <li>{@code %next%}: the button associated with
     *     {@link it.fulminazzo.blocksmith.command.CommandMessages#HELP_COMMAND_NEXT_PAGE}.</li>
     * </ul>
     *
     * @param component the component to format
     * @return the formatted component
     */
    @NotNull Component formatPageButtons(final @NotNull Component component) {
        final HelpPage.CommandData command = helpPage.getCommand();
        final CommandInput input = visitor.getInput();
        String helpCommand = input.getPartialRawInput() + " " + command.getHelpCommandName() + " ";
        String parentHelpCommand;
        if (command.getParentHelpCommandName() == null) parentHelpCommand = "";
        else {
            CommandInput snapshot = input.snapshot();
            for (int i = 0; i < command.getDepth(); i++) input.retreatCursor();
            parentHelpCommand = input.getPartialRawInput() + " " + command.getParentHelpCommandName();
            input.restore(snapshot);
        }
        Component back = getComponentOrFillers(
                !parentHelpCommand.isEmpty(),
                format(style.getPreviousCommandComponent()).clickEvent(ClickEvent.runCommand(parentHelpCommand))
        );
        Component previousPage = getComponentOrFillers(
                page > 1,
                format(style.getPreviousPageComponent()).clickEvent(ClickEvent.runCommand(helpCommand + (page - 1)))
        );
        Component nextPage = getComponentOrFillers(
                page < pages,
                format(style.getNextPageComponent()).clickEvent(ClickEvent.runCommand(helpCommand + (page + 1)))
        );
        return format(component
                .replaceText(r -> r.matchLiteral("%back%").replacement(back))
                .replaceText(r -> r.matchLiteral("%previous%").replacement(previousPage))
                .replaceText(r -> r.matchLiteral("%next%").replacement(nextPage))
        );
    }

    /**
     * Formats the given text with the following placeholders:
     * <ul>
     *     <li>{@code %name%}: the name of the given command;</li>
     *     <li>{@code %permission%}: the permission of the given command;</li>
     *     <li>{@code %description%}: the description of the given command;</li>
     *     <li>{@code %usage%}: the usage of the given command;</li>
     *     <li>{@code %filler%}: the value of
     *     {@link it.fulminazzo.blocksmith.command.CommandMessages#HELP_COMMAND_FILLER}
     *     (falls back to {@link HelpPageStyle#DEFAULT_FILLER} if not found)
     *     repeated until the component exceeds length {@link #MAX_FONT_WIDTH};</li>
     *     <li>{@code %page%}: the current page;</li>
     *     <li>{@code %pages%}: the total number of pages.</li>
     * </ul>
     *
     * @param component the component to format
     * @return the formatted component
     */
    @NotNull Component format(final @NotNull Component component) {
        return format(component, helpPage.getCommand());
    }

    /**
     * Formats the given text with the following placeholders:
     * <ul>
     *     <li>{@code %name%}: the name of the given command;</li>
     *     <li>{@code %permission%}: the permission of the given command;</li>
     *     <li>{@code %description%}: the description of the given command;</li>
     *     <li>{@code %usage%}: the usage of the given command;</li>
     *     <li>{@code %filler%}: the value of
     *     {@link it.fulminazzo.blocksmith.command.CommandMessages#HELP_COMMAND_FILLER}
     *     (falls back to {@link HelpPageStyle#DEFAULT_FILLER} if not found)
     *     repeated until the component exceeds length {@link #MAX_FONT_WIDTH};</li>
     *     <li>{@code %page%}: the current page;</li>
     *     <li>{@code %pages%}: the total number of pages.</li>
     * </ul>
     *
     * @param component   the component to format
     * @param commandData the container to get information from
     * @return the formatted component
     */
    @NotNull Component format(final @NotNull Component component,
                              final @NotNull HelpPage.CommandData commandData) {
        String description = ComponentUtils.toString(messenger.getComponentOrElse(commandData.getDescription(), locale, ""));
        final String serialized = ComponentUtils.toString(component)
                .replace("%name%", commandData.getName())
                .replace("%permission%", commandData.getPermission().getPermission())
                .replace("%description%", description)
                .replace("%usage%", commandData.getUsage())
                .replace("%page%", String.valueOf(page))
                .replace("%pages%", String.valueOf(pages));
        return parseFillerComponent(ComponentUtils.toComponent(serialized));
    }

    /**
     * It will replace any {@link #FILLER_PLACEHOLDER} in the given component
     * with the result of {@link HelpPageStyle#getFillerComponent()}
     * multiplied until the text does not reach {@link #MAX_FONT_WIDTH}.
     * <br>
     * Useful for generating centered text automatically.
     *
     * @param component the component to replace
     * @return the replaced component
     */
    @NotNull Component parseFillerComponent(final @NotNull Component component) {
        String text = PLAIN_SERIALIZER.serialize(component);
        if (!text.contains(FILLER_PLACEHOLDER)) return component;
        Component fillerComponent = style.getFillerComponent();
        final String filler = PLAIN_SERIALIZER.serialize(fillerComponent);
        int current = 1;
        while (MinecraftFontWidth.getWidth(text.replace(FILLER_PLACEHOLDER, filler.repeat(current + 1))) <= MAX_FONT_WIDTH)
            current++;
        Component replacement = repeat(fillerComponent, current);
        return component.replaceText(r -> r.matchLiteral(FILLER_PLACEHOLDER).replacement(replacement));
    }

    /**
     * Given a component, it will return the component if the condition is met;
     * otherwise it will return a component with the same text, but with the filler component repeated
     * until the component reaches the same length as the given one
     * (basically creating a positional text).
     *
     * @param condition the condition to check
     * @param component the component
     * @return the component or the filler component
     */
    @NotNull Component getComponentOrFillers(final boolean condition, final @NotNull Component component) {
        if (condition) return component;
        final String serialized = PLAIN_SERIALIZER.serialize(component);
        if (serialized.isEmpty()) return component;
        int length = MinecraftFontWidth.getWidth(serialized);
        Component fillerComponent = style.getFillerComponent();
        final String filler = PLAIN_SERIALIZER.serialize(fillerComponent);
        int current = 1;
        while (MinecraftFontWidth.getWidth(filler.repeat(current + 1)) <= length) current++;
        return repeat(fillerComponent, current);
    }

    /*
     * UTILITIES
     */

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

    private static @NotNull Component repeat(final @NotNull Component component, final int count) {
        String raw = ComponentUtils.toString(component);
        return ComponentUtils.toComponent(raw.repeat(count));
    }

}
