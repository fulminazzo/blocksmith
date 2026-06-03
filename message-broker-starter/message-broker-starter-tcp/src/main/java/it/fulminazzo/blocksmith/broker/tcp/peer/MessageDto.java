package it.fulminazzo.blocksmith.broker.tcp.peer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

/**
 * Base DTO for sharing messages across the network.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public final class MessageDto implements Serializable {
    private static final long serialVersionUID = -3323034308411965130L;

    @NotNull String channel;
    @NotNull String message;

}
