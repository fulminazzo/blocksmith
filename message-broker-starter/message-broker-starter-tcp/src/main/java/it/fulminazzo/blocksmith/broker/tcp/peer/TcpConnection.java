package it.fulminazzo.blocksmith.broker.tcp.peer;

import org.jetbrains.annotations.NotNull;

import java.io.Closeable;

/**
 * Represents a TCP connection.
 */
public interface TcpConnection extends Closeable {

    /**
     * Gets the host address.
     *
     * @return the host
     */
    @NotNull String getHost();

    /**
     * Gets the port.
     *
     * @return the port
     */
    int getPort();

    /**
     * Checks if the connection is closed.
     *
     * @return {@code true} if it is
     */
    boolean isClosed();

}
