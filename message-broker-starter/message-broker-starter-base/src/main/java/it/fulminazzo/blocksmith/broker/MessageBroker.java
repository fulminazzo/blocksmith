package it.fulminazzo.blocksmith.broker;

import org.jetbrains.annotations.NotNull;

import java.io.Closeable;

/**
 * Common interface for all message brokers.
 * <br>
 * Identifies all objects that can create message brokers.
 * This is a marker interface - each implementation varies in its methods
 * because each backend has different requirements.
 *
 * @param <S> the type of the message channel settings (to build new message channels)
 */
public interface MessageBroker<S extends MessageChannelSettings> extends Closeable {

    /**
     * Creates a new message channel.
     *
     * @param settings the settings to build the channel with
     * @return the channel
     */
    @NotNull MessageChannel newChannel(final @NotNull S settings);

}
