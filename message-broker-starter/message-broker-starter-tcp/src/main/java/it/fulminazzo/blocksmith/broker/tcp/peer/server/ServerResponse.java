package it.fulminazzo.blocksmith.broker.tcp.peer.server;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

/**
 * Holds all the server responses.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ServerResponse {

    /**
     * Message sent to indicate a successful computation of the request.
     */
    public static final @NotNull String SUCCESS = "OK";

    /**
     * Message sent when the client sends an invalid request.
     * An invalid request is one with no command or an unknown command.
     */
    public static final @NotNull String INVALID_REQUEST = "INVALID_REQ";

    /**
     * Message sent when the client sends a command that is not recognized.
     */
    public static final @NotNull String UNKNOWN_COMMAND = "NO_COMMAND";

    /**
     * Message sent when the client did not specify enough arguments.
     */
    public static final @NotNull String NOT_ENOUGH_ARGUMENTS = "MISSING_ARGUMENTS";

}
