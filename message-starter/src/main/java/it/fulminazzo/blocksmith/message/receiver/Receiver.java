package it.fulminazzo.blocksmith.message.receiver;

import net.kyori.adventure.audience.Audience;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

/**
 * Represents a general message receiver.
 */
public interface Receiver {

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
