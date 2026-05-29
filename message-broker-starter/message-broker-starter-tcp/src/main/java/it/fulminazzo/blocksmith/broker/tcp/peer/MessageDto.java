package it.fulminazzo.blocksmith.broker.tcp.peer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

/**
 * Base DTO for sharing messages across the network.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public final class MessageDto {
    @NotNull String channel;
    @NotNull String message;

}
