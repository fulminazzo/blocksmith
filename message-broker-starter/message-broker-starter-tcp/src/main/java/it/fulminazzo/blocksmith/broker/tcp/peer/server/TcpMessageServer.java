package it.fulminazzo.blocksmith.broker.tcp.peer.server;

import it.fulminazzo.blocksmith.broker.tcp.peer.Loggable;
import it.fulminazzo.blocksmith.broker.tcp.peer.TcpConnection;
import it.fulminazzo.blocksmith.broker.tcp.peer.client.TcpMessageClient;
import it.fulminazzo.blocksmith.data.mapper.Mapper;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;

/**
 * TCP server to handle incoming messages.
 * The message protocol is very basic as the main concern for this module is testing purposes.
 * In production environments a more sophisticated module should be used.
 *
 * @see TcpMessageClient
 * @see TcpMessageServerClient
 */
public final class TcpMessageServer extends Loggable implements TcpConnection, Runnable {
    private final @NotNull List<TcpMessageServerClient> clients = new CopyOnWriteArrayList<>();

    private final @NotNull ServerSocket serverSocket;

    private final @NotNull Mapper mapper;
    private final @NotNull ExecutorService executor;

    /**
     * Instantiates a new Tcp message server.
     *
     * @param logger   the logger used to display messages
     * @param mapper   the mapper used to serialize the messages
     * @param port     the port to listen to
     * @param executor the executor to handle clients with
     * @throws IOException in case the server cannot be started
     */
    public TcpMessageServer(
            final @NotNull Logger logger,
            final @NotNull Mapper mapper,
            final int port,
            final @NotNull ExecutorService executor
    ) throws IOException {
        super(logger);
        this.serverSocket = new ServerSocket(port);
        this.mapper = mapper;
        this.executor = executor;
    }

    /**
     * Sends the given payload to all connected clients.
     *
     * @param payload the payload to send
     */
    public void broadcast(final @NotNull String payload) {
        List<TcpMessageServerClient> clients = getClients();
        logger.debug(formatLog("Broadcasting to {} clients, message: {}"), clients.size(), payload);
        clients.forEach(c -> ServerCommand.MESSAGE.execute(c, payload));
    }

    /**
     * Returns the current list of connected clients.
     *
     * @return the connected clients
     */
    @NotNull List<TcpMessageServerClient> getClients() {
        clients.removeIf(TcpMessageServerClient::isClosed);
        return clients;
    }

    @Override
    public void run() {
        logger.info(formatLog("TCP server listening on port {}"), getPort());
        while (!isClosed())
            try {
                Socket socket = serverSocket.accept();
                logger.debug(
                        formatLog("New client connected on {}:{}"),
                        socket.getInetAddress().getHostAddress(),
                        socket.getPort()
                );
                TcpMessageServerClient client = new TcpMessageServerClient(this, logger, mapper, socket);
                clients.add(client);
                executor.execute(client);
            } catch (IOException e) {
                // client connection closed abruptly
            }
    }

    @Override
    public void close() {
        try {
            clients.forEach(TcpMessageServerClient::close);
            if (!isClosed()) {
                serverSocket.close();
                logger.info(formatLog("TCP server stopped"));
            }
        } catch (IOException e) {
            // do nothing
        }
    }

    @Override
    public @NotNull String getHost() {
        return serverSocket.getInetAddress().getHostAddress();
    }

    @Override
    public int getPort() {
        return serverSocket.getLocalPort();
    }

    @Override
    public boolean isClosed() {
        return serverSocket.isClosed();
    }

    @Override
    protected @NotNull String formatLog(@NotNull String message) {
        return String.format("|%s (%s)| %s", getClass().getSimpleName(), getPort(), message);
    }

}
