package it.fulminazzo.blocksmith.broker.redis;

import it.fulminazzo.blocksmith.broker.MessageChannelSettings;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true, doNotUseGetters = true)
@ToString(callSuper = true, doNotUseGetters = true)
public final class RedisMessageChannelSettings extends MessageChannelSettings<RedisMessageChannelSettings> {

}
