package it.fulminazzo.blocksmith.broker.tcp.server;

import it.fulminazzo.blocksmith.data.mapper.Mapper;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TCP server to handle incoming messages.
 * The message protocol is very basic as the main concern for this module is testing purposes.
 * In production environments a more sophisticated module should be used.
 *
 * @see TcpMessageServerClient
 */
@RequiredArgsConstructor
public final class TcpMessageServer implements Runnable, Closeable {
    private final @NotNull Map<String, List<TcpMessageServerClient>> clients = new ConcurrentHashMap<>();

    private final @NotNull Logger logger;
    private final @NotNull Mapper mapper;
    private final int port;

    private @Nullable ServerSocket socket;

    /**
     * Broadcasts a message to all the clients subscribed to the specified channel.
     *
     * @param channel the channel to which the message should be sent
     * @param message the message to send (without the leading {@code \n})
     */
    void broadcast(final @NotNull String channel, final @NotNull String message) {
        clients.computeIfPresent(channel, (c, l) -> {
            l.removeIf(TcpMessageServerClient::isClosed);
            l.forEach(t -> t.write(message));
            return l;
        });
    }

    private @NotNull String formatLog(final @NotNull String message) {
        return String.format("|%s (%s)|: %s", getClass().getSimpleName(), port, message);
    }

    private void registerHandler(final @NotNull String channel, final @NotNull TcpMessageServerClient handler) {
        clients.computeIfAbsent(channel, c -> new ArrayList<>()).add(handler);
        handler.write("OK");
    }

    @Override
    public void run() {
        try {
            socket = new ServerSocket(port);
            logger.info(formatLog("Server started on port {}"), port);
        } catch (IOException e) {
            /*
             * Either the port is already in use or the system refused to bind it.
             * Both cases are handled by the clients.
             */
        }
        while (socket != null && !socket.isClosed())
            try {
                Socket socket = this.socket.accept();
                logger.debug(
                        formatLog("New client connected on {}:{}"),
                        socket.getInetAddress().getHostAddress(),
                        socket.getPort()
                );
                new TcpMessageServerClient(logger, mapper, socket)
                        .onRead(this::broadcast)
                        .start(this::registerHandler);
            } catch (IOException e) {
                // client closed connection abruptly
            }
    }

    @Override
    public void close() {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                // do nothing
            }
            socket = null;
            logger.info(formatLog("TCP server stopped"));
        }
    }

}
