package it.fulminazzo.blocksmith.broker.redis;

import it.fulminazzo.blocksmith.broker.MessageChannelSettings;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Message channel settings for Redis databases.
 *
 * @see RedisMessageChannel
 * @see RedisMessageBroker
 */
@EqualsAndHashCode(callSuper = true, doNotUseGetters = true)
@ToString(callSuper = true, doNotUseGetters = true)
public final class RedisMessageChannelSettings extends MessageChannelSettings<RedisMessageChannelSettings> {

}
