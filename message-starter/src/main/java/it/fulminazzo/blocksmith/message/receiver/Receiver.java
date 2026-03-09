package it.fulminazzo.blocksmith.message.receiver;

import net.kyori.adventure.audience.Audience;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

/**
 * Represents a general message receiver.
 *
 * @param <R> the actual type of the receiver
 */
public interface Receiver<R> {

    /**
     * Converts the given receiver to an Audience.
     *
     * @param receiver the receiver of the message
     * @return the audience
     */
    @NotNull Audience toAudience(final @NotNull R receiver);

    /**
     * Gets the locale of the receiver.
     *
     * @param receiver the receiver of the message
     * @return the locale
     */
    @NotNull Locale getLocale(final @NotNull R receiver);

}
