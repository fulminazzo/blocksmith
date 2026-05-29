package it.fulminazzo.blocksmith.broker.tcp.peer.server;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Simple DTO for sharing the channel name.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public final class ChannelDto {
    private String channelName;

}
