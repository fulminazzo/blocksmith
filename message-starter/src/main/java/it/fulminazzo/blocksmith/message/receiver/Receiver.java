package it.fulminazzo.blocksmith.message.receiver;

import it.fulminazzo.blocksmith.message.util.ComponentUtils;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

/**
 * Represents a general message receiver.
 */
public interface Receiver {

    /**
     * Sends a title to this receiver.
     *
     * @param title    the title
     * @param subtitle the subtitle
     * @param times    the timings of the title
     * @return this object (for method chaining)
     */
    default @NotNull Receiver sendTitle(final @Nullable String title,
                                        final @Nullable String subtitle,
                                        final @NotNull Title.Times times) {
        return sendTitle(
                title == null ? null : ComponentUtils.toComponent(title),
                subtitle == null ? null : ComponentUtils.toComponent(subtitle),
                times
        );
    }

    /**
     * Sends a title to this receiver.
     *
     * @param title    the title
     * @param subtitle the subtitle
     * @param times    the timings of the title
     * @return this object (for method chaining)
     */
    default @NotNull Receiver sendTitle(final @Nullable String title,
                                        final @Nullable Component subtitle,
                                        final @NotNull Title.Times times) {
        return sendTitle(
                title == null ? null : ComponentUtils.toComponent(title),
                subtitle,
                times
        );
    }

    /**
     * Sends a title to this receiver.
     *
     * @param title    the title
     * @param subtitle the subtitle
     * @param times    the timings of the title
     * @return this object (for method chaining)
     */
    default @NotNull Receiver sendTitle(final @Nullable Component title,
                                        final @Nullable String subtitle,
                                        final @NotNull Title.Times times) {
        return sendTitle(
                title,
                subtitle == null ? null : ComponentUtils.toComponent(subtitle),
                times
        );
    }

    /**
     * Sends a title to this receiver.
     *
     * @param title    the title
     * @param subtitle the subtitle
     * @param times    the timings of the title
     * @return this object (for method chaining)
     */
    default @NotNull Receiver sendTitle(final @Nullable Component title,
                                        final @Nullable Component subtitle,
                                        final @NotNull Title.Times times) {
        Title adventureTitle = Title.title(
                title == null ? Component.empty() : title,
                subtitle == null ? Component.empty() : subtitle,
                times
        );
        audience().showTitle(adventureTitle);
        return this;
    }

    /**
     * Sends a message through the <b>action bar</b> to this receiver.
     *
     * @param message the message to send
     * @return this object (for method chaining)
     */
    default @NotNull Receiver sendActionBar(final @NotNull String message) {
        return sendActionBar(ComponentUtils.toComponent(message));
    }

    /**
     * Sends a message through the <b>action bar</b> to this receiver.
     *
     * @param component the message to send
     * @return this object (for method chaining)
     */
    default @NotNull Receiver sendActionBar(final @NotNull Component component) {
        audience().sendActionBar(component);
        return this;
    }

    /**
     * Sends a message in chat to this receiver.
     *
     * @param message the message to send
     * @return this object (for method chaining)
     */
    default @NotNull Receiver sendMessage(final @NotNull String message) {
        return sendMessage(ComponentUtils.toComponent(message));
    }

    /**
     * Sends a message in chat to this receiver.
     *
     * @param component the message to send
     * @return this object (for method chaining)
     */
    default @NotNull Receiver sendMessage(final @NotNull Component component) {
        audience().sendMessage(component);
        return this;
    }

    /**
     * Gets the locale of the receiver.
     *
     * @return the locale
     */
    @NotNull Locale getLocale();

    /**
     * Converts the given receiver to an Audience.
     *
     * @return the audience
     */
    @NotNull Audience audience();

    /**
     * Gets the internal wrapped receiver.
     *
     * @param <R> the type of the receiver
     * @return the receiver
     */
    <R> @NotNull R handle();

}
