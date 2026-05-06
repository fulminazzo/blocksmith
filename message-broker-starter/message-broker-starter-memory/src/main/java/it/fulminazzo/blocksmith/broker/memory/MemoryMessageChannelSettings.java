package it.fulminazzo.blocksmith.broker.memory;

import it.fulminazzo.blocksmith.broker.MessageChannelSettings;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true, doNotUseGetters = true)
@ToString(callSuper = true, doNotUseGetters = true)
public final class MemoryMessageChannelSettings extends MessageChannelSettings<MemoryMessageChannelSettings> {

}
