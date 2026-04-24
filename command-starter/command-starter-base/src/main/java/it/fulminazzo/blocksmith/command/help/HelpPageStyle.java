package it.fulminazzo.blocksmith.command.help;

import it.fulminazzo.blocksmith.util.StringUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds the universal styling for help pages.
 * <br>
 * Colors and styling follow the <a href="https://docs.papermc.io/adventure/minimessage/format/">MiniMessage format</a>.
 */
@Getter(AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HelpPageStyle {
    private static @NotNull HelpPageStyle instance = new HelpPageStyle();

    private @NotNull String filler = "-";
    private final @NotNull List<String> fillerStyles = new ArrayList<>(List.of("strikethrough", "gold"));

    private @NotNull String header = "%name%";
    private @NotNull String separatorText = "%subcommands%";
    private @NotNull String footer = ""; //TODO: compute pages distances

    /**
     * Gets the filler with the styling applied.
     *
     * @return the filler
     */
    @NotNull String getStyledFiller() {
        String filler = getFiller();
        for (String s : getFillerStyles())
            filler = StringUtils.tag(s, filler);
        return filler;
    }

    /**
     * Sets the filler for header, separator and footer in a help page.
     *
     * @param filler the filler
     * @return this object (for method chaining)
     */
    public @NotNull HelpPageStyle filler(final @NotNull String filler) {
        this.filler = filler;
        return this;
    }

    /**
     * Sets the styling for the filler of header, separator and footer in a help page.
     *
     * @param styling the styling (parsed through the
     *                <a href="https://docs.papermc.io/adventure/minimessage/format/">MiniMessage format</a>)
     * @return this object (for method chaining)
     */
    public @NotNull HelpPageStyle fillerStyling(final @NotNull String @NotNull ... styling) {
        this.fillerStyles.clear();
        this.fillerStyles.addAll(List.of(styling));
        return this;
    }

    /**
     * Sets the header of a help page.
     * After parsing, it will be wrapped by {@link #getFiller()} to fill the entire line.
     * <br>
     * It supports the following placeholders:
     * <ul>
     *     <li>{@code %filler%}: one character of the current {@link #getFiller()};</li>
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
     * @param header the header
     * @return this object (for method chaining)
     */
    public @NotNull HelpPageStyle header(final @NotNull String header) {
        this.header = header;
        return this;
    }

    /**
     * Sets the separator text of a help page.
     * After parsing, it will be wrapped by {@link #getFiller()} to fill the entire line.
     * <br>
     * It supports the following placeholders:
     * <ul>
     *     <li>{@code %filler%}: one character of the current {@link #getFiller()};</li>
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
     * @param separatorText the separator text
     * @return this object (for method chaining)
     */
    public @NotNull HelpPageStyle separatorText(final @NotNull String separatorText) {
        this.separatorText = separatorText;
        return this;
    }

    /**
     * Sets the footer of a help page.
     * After parsing, it will be wrapped by {@link #getFiller()} to fill the entire line.
     * <br>
     * It supports the following placeholders:
     * <ul>
     *     <li>{@code %filler%}: one character of the current {@link #getFiller()};</li>
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
     * @param footer the footer
     * @return this object (for method chaining)
     */
    public @NotNull HelpPageStyle footer(final @NotNull String footer) {
        this.footer = footer;
        return this;
    }

    /**
     * Resets the styling to its default values.
     *
     * @return this object (for method chaining)
     */
    public @NotNull HelpPageStyle defaults() {
        instance = new HelpPageStyle();
        return instance;
    }

    /**
     * Get help page style.
     *
     * @return the help page style
     */
    public static @NotNull HelpPageStyle get() {
        return instance;
    }

}
