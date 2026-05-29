package it.fulminazzo.blocksmith.broker.tcp.peer;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * Represents a connection to a peer.
 */
public interface PeerConnection {

    /**
     * Writes a message to the peer.
     * <br>
     * The message is <b>not</b> guaranteed to be delivered
     * (if the peer is offline at the time of writing).
     *
     * @param message the message to write
     */
    void write(final @NotNull String message);

    /**
     * Sets the callback function to be executed when a message is received.
     *
     * @param callback the callback function
     * @return this object (for method chaining)
     */
    @NotNull PeerConnection onRead(final @NotNull Consumer<@NotNull String> callback);

}
