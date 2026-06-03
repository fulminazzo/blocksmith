package it.fulminazzo.blocksmith.broker.tcp;

import it.fulminazzo.blocksmith.broker.MessageChannelSettings;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Message channel settings for TCP connections.
 *
 * @see TcpMessageChannel
 * @see TcpMessageBroker
 */
@EqualsAndHashCode(callSuper = true, doNotUseGetters = true)
@ToString(callSuper = true, doNotUseGetters = true)
public final class TcpMessageChannelSettings extends MessageChannelSettings<TcpMessageChannelSettings> {

}
